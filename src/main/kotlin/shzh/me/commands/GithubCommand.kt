package shzh.me.commands

import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.utils.asFlow
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.takeWhile
import shzh.me.services.*
import shzh.me.utils.MessageUtils
import java.util.*
import kotlin.collections.HashMap

val githubChannels = HashMap<Pair<Long, String>, Channel<Int>>()

private fun toOpenGraph(link: String): String {
    val regex = "(https://github.com/)".toRegex()

    return regex.replace(link, "https://opengraph.githubassets.com/1/")
}

suspend fun handleGithubLinks(call: ApplicationCall, link: String) {
    val openGraph = toOpenGraph(link)

    val reply = MessageUtils
        .builder()
        .image(openGraph)
        .content()
    replyMessage(call, reply)
}

suspend fun handleGithub(call: ApplicationCall, command: String, groupID: Long, messageID: Int) {
    val githubCmd = command.substringAfter(' ')

    // /github list
    if (githubCmd == "list") {
        handleGithubList(call, groupID)
        return
    }

    // For /github [subscribe | unsubscribe]
    val (op, username) = githubCmd.split(' ')
    when (op) {
        // /github subscribe <weibo_id>
        "subscribe" -> handleSubGithub(call, groupID, username, messageID)
        // /github unsubscribe <weibo_id>
        "unsubscribe" -> handleUnsubGithub(call, groupID, username, messageID)
    }
}

private suspend fun handleGithubList(call: ApplicationCall, groupID: Long) {
    val users = getGithubsByGID(groupID)

    val reply = if (users.isEmpty()) {
        "本群没有订阅Github任何用户！"
    } else {
        val usernames = users.map { it.username }
        "本群订阅的Github用户：\n" + usernames.joinToString(separator = "\n")
    }

    replyMessage(call, reply)
}

private suspend fun handleSubGithub(call: ApplicationCall, groupID: Long, username: String, messageID: Int) {
    val published = getLatestDynDate(username)
    insertGithubUser(groupID, username, published)

    val reply = MessageUtils
        .builder()
        .reply(messageID)
        .text("成功关注Github用户 $username", newline = false)
        .content()
    replyMessage(call, reply)

    val channel = Channel<Int>()
    val key = Pair(groupID, username)
    if (!githubChannels.containsKey(key)) {
        githubChannels[key] = channel
        poolingGithub(groupID, username, published, channel)
    }
}

private suspend fun handleUnsubGithub(call: ApplicationCall, groupID: Long, username: String, messageID: Int) {
    val user = deleteGithubUser(groupID, username)

    if (user != null) {
        val reply = MessageUtils
            .builder()
            .reply(messageID)
            .text("成功取消订阅Github用户 $username", newline = false)
            .content()
        replyMessage(call, reply)
    }

    val key = Pair(groupID, username)
    val channel = githubChannels[key]
    if (channel != null) {
        channel.send(0)
        channel.close()
        githubChannels.remove(key)
    }
}

private suspend fun poolingGithub(groupID: Long, username: String, lastParam: Date, channel: Channel<Int>) {
    val scheduler = buildSchedule { minutes { 0 every 1 } }
    val flow = scheduler.asFlow()

    var last = lastParam
    flow.takeWhile {
        !channel.tryReceive().isSuccess
    }.collect {
        val latest = getLatestDynDate(username)
        if (latest > last) {
            updateGithubUser(groupID, username, latest)

            val dynamics = fetchAllDynamics(username)

            val star = dynamics.find {
                it.publishedDate > last && Regex("(.*) starred (.*)") matches it.title
            }
            if (star != null) {
                val reply = MessageUtils
                    .builder()
                    .text("Github 用户 $username star了仓库")
                    .text(star.link)
                    .image(toOpenGraph(star.link), newline = false)
                    .content()
                sendGroupMessage(groupID, reply)
            }

            val fork = dynamics.find {
                it.publishedDate > last && Regex("(.*) forked (.*)") matches it.title
            }
            if (fork != null) {
                val reply = MessageUtils
                    .builder()
                    .text("Github 用户 $username fork了仓库")
                    .text(fork.link)
                    .image(toOpenGraph(fork.link), newline = false)
                    .content()
                sendGroupMessage(groupID, reply)
            }

            val repo = dynamics.find {
                it.publishedDate > last && Regex("(.*) created a repository (.*)") matches it.title
            }
            if (repo != null) {
                val reply = MessageUtils
                    .builder()
                    .text("Github 用户 创建了仓库")
                    .text(repo.link)
                    .image(toOpenGraph(repo.link), newline = false)
                    .content()
                sendGroupMessage(groupID, reply)
            }
        }
        last = latest
    }
}

suspend fun recoverPoolingGithub() {
    val users = getAllGithubUsers()
    users.forEach {
        val channel = Channel<Int>()
        githubChannels[Pair(it.groupID, it.username)] = channel
        val latest = getLatestDynDate(it.username)
        poolingGithub(it.groupID, it.username, latest, channel)
    }
}

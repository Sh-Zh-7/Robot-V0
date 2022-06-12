package shzh.me.commands

import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.utils.asFlow
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.takeWhile
import shzh.me.model.dto.MessageDTO
import shzh.me.services.impl.GithubServiceImpl
import shzh.me.services.impl.OneBotServiceImpl
import shzh.me.utils.MessageUtils
import java.util.*

val githubChannels = HashMap<Pair<Long, String>, Channel<Int>>()

object GithubLinkCommand {
    private val onebotService = OneBotServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        val link = message.message
        val openGraph = toOpenGraph(link)

        val reply = MessageUtils
            .builder()
            .image(openGraph)
            .content()
        onebotService.replyMessage(call, reply)
    }
}

object GithubCommand {
    private val onebotService = OneBotServiceImpl()
    private val githubService = GithubServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        val githubCmd = message.message.substringAfter(' ')

        // /github list
        if (githubCmd == "list") {
            list(call, message.groupID)
            return
        }

        // /github [subscribe | unsubscribe]
        val (op, username) = githubCmd.split(' ')
        when (op) {
            // /github subscribe <weibo_id>
            "subscribe" -> subscribe(call, message.groupID, username, message.messageID)
            // /github unsubscribe <weibo_id>
            "unsubscribe" -> unsubscribe(call, message.groupID, username, message.messageID)
        }
    }

    private suspend fun list(call: ApplicationCall, groupID: Long) {
        val users = githubService.getGithubsByGID(groupID)

        val reply = if (users.isEmpty()) {
            "本群没有订阅Github任何用户！"
        } else {
            val usernames = users.map { it.username }
            "本群订阅的Github用户：\n" + usernames.joinToString(separator = "\n")
        }

        onebotService.replyMessage(call, reply)
    }

    private suspend fun subscribe(call: ApplicationCall, groupID: Long, username: String, messageID: Int) {
        val published = githubService.getLatestDynDate(username)
        githubService.insertGithubUser(groupID, username, published)

        val reply = MessageUtils
            .builder()
            .reply(messageID)
            .text("成功关注Github用户 $username")
            .content()
        onebotService.replyMessage(call, reply)

        val channel = Channel<Int>()
        val key = Pair(groupID, username)
        if (!githubChannels.containsKey(key)) {
            githubChannels[key] = channel
            polling(groupID, username, published, channel)
        }
    }

    private suspend fun unsubscribe(call: ApplicationCall, groupID: Long, username: String, messageID: Int) {
        val user = githubService.deleteGithubUser(groupID, username)

        if (user != null) {
            val reply = MessageUtils
                .builder()
                .reply(messageID)
                .text("成功取消订阅Github用户 $username")
                .content()
            onebotService.replyMessage(call, reply)
        }

        val key = Pair(groupID, username)
        val channel = githubChannels[key]
        if (channel != null) {
            channel.send(0)
            channel.close()
            githubChannels.remove(key)
        }
    }

    private suspend fun polling(groupID: Long, username: String, lastParam: Date, channel: Channel<Int>) {
        val scheduler = buildSchedule { minutes { 0 every 5 } }
        val flow = scheduler.asFlow()

        var last = lastParam
        flow.takeWhile {
            !channel.tryReceive().isSuccess
        }.collect {
            val latest = githubService.getLatestDynDate(username)
            if (latest > last) {
                githubService.updateGithubUser(groupID, username, latest)

                val dynamics = githubService.fetchAllDynamics(username)

                val star = dynamics.find {
                    it.publishedDate > last && Regex("(.*) starred (.*)") matches it.title
                }
                if (star != null) {
                    val reply = MessageUtils
                        .builder()
                        .text("Github 用户 $username star了仓库")
                        .text(star.link)
                        .image(toOpenGraph(star.link))
                        .content()
                    onebotService.sendGroupMessage(groupID, reply)
                }

                val fork = dynamics.find {
                    it.publishedDate > last && Regex("(.*) forked (.*)") matches it.title
                }
                if (fork != null) {
                    val reply = MessageUtils
                        .builder()
                        .text("Github 用户 $username fork了仓库")
                        .text(fork.link)
                        .image(toOpenGraph(fork.link))
                        .content()
                    onebotService.sendGroupMessage(groupID, reply)
                }

                val repo = dynamics.find {
                    it.publishedDate > last && Regex("(.*) created a repository (.*)") matches it.title
                }
                if (repo != null) {
                    val reply = MessageUtils
                        .builder()
                        .text("Github 用户 $username 创建了仓库")
                        .text(repo.link)
                        .image(toOpenGraph(repo.link))
                        .content()
                    onebotService.sendGroupMessage(groupID, reply)
                }
            }
            last = latest
        }
    }

    suspend fun recover() {
        val users = githubService.getAllGithubUsers()
        users.forEach {
            val channel = Channel<Int>()
            githubChannels[Pair(it.groupID, it.username)] = channel
            val latest = githubService.getLatestDynDate(it.username)
            polling(it.groupID, it.username, latest, channel)
        }
    }

}

private fun toOpenGraph(link: String): String {
    val regex = "(https://github.com/)".toRegex()

    return regex.replace(link, "https://opengraph.githubassets.com/1/")
}

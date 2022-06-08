package shzh.me.commands

import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.utils.asFlow
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.takeWhile
import shzh.me.services.*
import shzh.me.utils.MessageUtils

val zhihuChannels = HashMap<Pair<Long, String>, Channel<Int>>()

suspend fun handleZhihu(call: ApplicationCall, command: String, groupID: Long, messageID: Int) {
    val zhihuCmd = command.substringAfter(' ')

    // /zhihu list
    if (zhihuCmd == "list") {
        handleZhihuList(call, groupID)
        return
    }

    // For /zhihu [subscribe | unsubscribe]
    val (op, username) = zhihuCmd.split(' ')
    when (op) {
        // /zhihu subscribe <weibo_id>
        "subscribe" -> handleSubZhihu(call, groupID, username, messageID)
        // /zhihu unsubscribe <weibo_id>
        "unsubscribe" -> handleUnsubZhihu(call, groupID, username, messageID)
    }
}

private suspend fun handleZhihuList(call: ApplicationCall, groupID: Long) {
    val users = getZhihuUsersByGID(groupID)

    val reply = if (users.isEmpty()) {
        "本群没有订阅知乎任何用户！"
    } else {
        val usernames = users.map { it.username }
        "本群订阅的知乎用户：\n" + usernames.joinToString(separator = "\n")
    }

    replyMessage(call, reply)
}

private suspend fun handleSubZhihu(call: ApplicationCall, groupID: Long, username: String, messageID: Int) {
    val status = getZhihuLatestDate(username)
    insertZhihuUser(groupID, username, status.answer, status.post, status.pin)

    val reply = MessageUtils
        .builder()
        .reply(messageID)
        .text("成功关注知乎用户 $username", newline = false)
        .content()
    replyMessage(call, reply)

    val channel = Channel<Int>()
    val key = Pair(groupID, username)
    if (!zhihuChannels.containsKey(key)) {
        zhihuChannels[key] = channel
        poolingZhihu(groupID, username, status, channel)
    }
}

private suspend fun handleUnsubZhihu(call: ApplicationCall, groupID: Long, username: String, messageID: Int) {
    val user = deleteZhihuUser(groupID, username)

    if (user != null) {
        val reply = MessageUtils
            .builder()
            .reply(messageID)
            .text("成功取消订阅知乎用户 $username", newline = false)
            .content()
        replyMessage(call, reply)
    }

    val key = Pair(groupID, username)
    val channel = zhihuChannels[key]
    if (channel != null) {
        channel.send(0)
        channel.close()
        zhihuChannels.remove(key)
    }
}

private suspend fun poolingZhihu(groupID: Long, username: String, lastStatusParam: ZhihuStatus, channel: Channel<Int>) {
    val scheduler = buildSchedule { minutes { 0 every 1 } }
    val flow = scheduler.asFlow()

    flow.takeWhile {
        !channel.tryReceive().isSuccess
    }.collect {
        // Answer
        val latestAnswer = getZhihuLatestAnswerDate(username)
        if (latestAnswer != null) {
            if (latestAnswer.publishedDate > lastStatusParam.answer) {
                updateZhihuAnswerDate(groupID, username, latestAnswer.publishedDate)

                val rely = MessageUtils
                    .builder()
                    .text("知乎用户 $username 回答了问题")
                    .text(latestAnswer.link, newline = false)
                    .content()
                sendGroupMessage(groupID, rely)
            }
            lastStatusParam.answer = latestAnswer.publishedDate

        }
        // Post
        val latestPost = getZhihuLatestPostDate(username)
        if (latestPost != null) {
            if (latestPost.publishedDate > lastStatusParam.post) {
                updateZhihuPostDate(groupID, username, latestPost.publishedDate)

                val rely = MessageUtils
                    .builder()
                    .text("知乎用户 $username 发表了文章")
                    .text(latestPost.link, newline = false)
                    .content()
                sendGroupMessage(groupID, rely)
            }
            lastStatusParam.post = latestPost.publishedDate
        }

        // Pin
        val latestPin = getZhihuLatestPinDate(username)
        if (latestPin != null) {
            if (latestPin.publishedDate > lastStatusParam.pin) {
                updateZhihuPinDate(groupID, username, latestPin.publishedDate)

                val rely = MessageUtils
                    .builder()
                    .text("知乎用户 $username 发表了想法：")
                    .text(latestPin.link)
                    .text(latestPin.title, newline = false)
                    .content()
                sendGroupMessage(groupID, rely)
            }
            lastStatusParam.pin = latestPin.publishedDate
        }
    }
}

suspend fun recoverPoolingZhihu() {
    val users = getAllZhihuUsers()
    users.forEach {
        val channel = Channel<Int>()
        zhihuChannels[Pair(it.groupID, it.username)] = channel
        val status = getZhihuLatestDate(it.username)
        poolingZhihu(it.groupID, it.username, status, channel)
    }
}

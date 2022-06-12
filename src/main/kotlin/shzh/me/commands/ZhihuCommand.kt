package shzh.me.commands

import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.utils.asFlow
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.takeWhile
import shzh.me.model.dto.MessageDTO
import shzh.me.services.ZhihuStatus
import shzh.me.services.impl.OneBotServiceImpl
import shzh.me.services.impl.ZhihuServiceImpl
import shzh.me.utils.MessageUtils

object ZhihuCommand {
    private val zhihuChannels = HashMap<Pair<Long, String>, Channel<Int>>()
    private val onebotService = OneBotServiceImpl()
    private val zhihuService = ZhihuServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        val zhihuCmd = message.message.substringAfter(' ')

        // /zhihu list
        if (zhihuCmd == "list") {
            list(call, message.groupID)
            return
        }

        // /zhihu [subscribe | unsubscribe]
        val (op, username) = zhihuCmd.split(' ')
        when (op) {
            // /zhihu subscribe <weibo_id>
            "subscribe" -> subscribe(call, message.groupID, username, message.messageID)
            // /zhihu unsubscribe <weibo_id>
            "unsubscribe" -> unsubscribe(call, message.groupID, username, message.messageID)
        }
    }

    private suspend fun list(call: ApplicationCall, groupID: Long) {
        val users = zhihuService.getZhihuUsersByGID(groupID)

        val reply = if (users.isEmpty()) {
            "本群没有订阅知乎任何用户！"
        } else {
            val usernames = users.map { it.username }
            "本群订阅的知乎用户：\n" + usernames.joinToString(separator = "\n")
        }

        onebotService.replyMessage(call, reply)
    }

    private suspend fun subscribe(call: ApplicationCall, groupID: Long, username: String, messageID: Int) {
        val status = zhihuService.getZhihuLatestDate(username)
        zhihuService.insertZhihuUser(groupID, username, status.answer, status.post, status.pin)

        val reply = MessageUtils
            .builder()
            .reply(messageID)
            .text("成功关注知乎用户 $username")
            .content()
        onebotService.replyMessage(call, reply)

        val channel = Channel<Int>()
        val key = Pair(groupID, username)
        if (!zhihuChannels.containsKey(key)) {
            zhihuChannels[key] = channel
            polling(groupID, username, status, channel)
        }
    }

    private suspend fun unsubscribe(call: ApplicationCall, groupID: Long, username: String, messageID: Int) {
        val user = zhihuService.deleteZhihuUser(groupID, username)

        if (user != null) {
            val reply = MessageUtils
                .builder()
                .reply(messageID)
                .text("成功取消订阅知乎用户 $username")
                .content()
            onebotService.replyMessage(call, reply)
        }

        val key = Pair(groupID, username)
        val channel = zhihuChannels[key]
        if (channel != null) {
            channel.send(0)
            channel.close()
            zhihuChannels.remove(key)
        }
    }

    private suspend fun polling(groupID: Long, username: String, lastStatusParam: ZhihuStatus, channel: Channel<Int>) {
        val scheduler = buildSchedule { minutes { 0 every 5 } }
        val flow = scheduler.asFlow()

        flow.takeWhile {
            !channel.tryReceive().isSuccess
        }.collect {
            // 用户回答
            val latestAnswer = zhihuService.getZhihuLatestAnswerDate(username)
            if (latestAnswer != null) {
                if (latestAnswer.publishedDate > lastStatusParam.answer) {
                    zhihuService.updateZhihuAnswerDate(groupID, username, latestAnswer.publishedDate)

                    val rely = MessageUtils
                        .builder()
                        .text("知乎用户 $username 回答了问题")
                        .text(latestAnswer.link)
                        .content()
                    onebotService.sendGroupMessage(groupID, rely)
                }
                lastStatusParam.answer = latestAnswer.publishedDate

            }
            // 用户文章
            val latestPost = zhihuService.getZhihuLatestPostDate(username)
            if (latestPost != null) {
                if (latestPost.publishedDate > lastStatusParam.post) {
                    zhihuService.updateZhihuPostDate(groupID, username, latestPost.publishedDate)

                    val rely = MessageUtils
                        .builder()
                        .text("知乎用户 $username 发表了文章")
                        .text(latestPost.link)
                        .content()
                    onebotService.sendGroupMessage(groupID, rely)
                }
                lastStatusParam.post = latestPost.publishedDate
            }
            // 用户想法
            val latestPin = zhihuService.getZhihuLatestPinDate(username)
            if (latestPin != null) {
                if (latestPin.publishedDate > lastStatusParam.pin) {
                    zhihuService.updateZhihuPinDate(groupID, username, latestPin.publishedDate)

                    val rely = MessageUtils
                        .builder()
                        .text("知乎用户 $username 发表了想法：")
                        .text(latestPin.link)
                        .text(latestPin.title)
                        .content()
                    onebotService.sendGroupMessage(groupID, rely)
                }
                lastStatusParam.pin = latestPin.publishedDate
            }
        }
    }

    suspend fun recover() {
        val users = zhihuService.getAllZhihuUsers()
        users.forEach {
            val channel = Channel<Int>()
            zhihuChannels[Pair(it.groupID, it.username)] = channel
            val status = zhihuService.getZhihuLatestDate(it.username)
            polling(it.groupID, it.username, status, channel)
        }
    }
}

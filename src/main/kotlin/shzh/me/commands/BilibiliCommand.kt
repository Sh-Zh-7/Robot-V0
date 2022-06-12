package shzh.me.commands

import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.utils.asFlow
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.takeWhile
import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import shzh.me.model.dto.MessageDTO
import shzh.me.services.impl.BiliBiliApiServiceImpl
import shzh.me.services.impl.BilibiliDynServiceImpl
import shzh.me.services.impl.BilibiliLiveServiceImpl
import shzh.me.services.impl.OneBotServiceImpl
import shzh.me.utils.BrowserUtils
import shzh.me.utils.MessageUtils
import java.io.File
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

object BilibiliVideoCommand {
    private val onebotService = OneBotServiceImpl()
    private val bilibiliApiService = BiliBiliApiServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        val regex = Regex("https://www\\.bilibili\\.com/video/BV(\\w{10})")
        val bv = regex.find(message.message)!!.groupValues[1]
        val data = bilibiliApiService.getBVData(bv)

        onebotService.replyMessage(call, data.toString())
    }
}

object BilibiliDynamicCommand {
    private val bUsersChannels = HashMap<Pair<Long, Long>, Channel<Int>>()
    private val bilibiliDynService = BilibiliDynServiceImpl()
    private val bilibiliApiService = BiliBiliApiServiceImpl()
    private val onebotService = OneBotServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        val bDynCmd = message.message.substringAfter(' ')

        // /bili list
        if (bDynCmd == "list") {
            list(call, message.groupID)
            return
        }

        val (op, userIDStr) = bDynCmd.split(' ')
        val userID = userIDStr.toLong()
        when (op) {
            // /bili subscribe <user_id>
            "subscribe" -> subscribe(call, message.groupID, userID, message.messageID)
            // /bili unsubscribe <user_id>
            "unsubscribe" -> unsubscribe(call, message.groupID, userID, message.messageID)
        }
    }

    private suspend fun list(call: ApplicationCall, groupID: Long) {
        val users = bilibiliDynService.getBDynUsersByGID(groupID)

        val reply = if (users.isEmpty()) {
            "本群没有关注B站任何UP主！"
        } else {
            val userIDs = users.map { it.userID }
            val usernames = userIDs.map { bilibiliApiService.getUsernameByUID(it) }
            "本群订阅的B站UP主：\n" + (userIDs zip usernames).joinToString(separator = "\n") {
                "${it.first}\t${it.second}"
            }
        }

        onebotService.replyMessage(call, reply)
    }

    private suspend fun subscribe(call: ApplicationCall, groupID: Long, userID: Long, messageID: Int) {
        val (newest, _) = bilibiliDynService.getNewestPublishTimestamp(userID)
        bilibiliDynService.insertBVUser(groupID, userID, newest)

        val username = bilibiliApiService.getUsernameByUID(userID)
        val reply = MessageUtils
            .builder()
            .reply(messageID)
            .text("成功订阅UP主 $username")
            .content()
        onebotService.replyMessage(call, reply)

        val channel = Channel<Int>()
        val key = Pair(groupID, userID)
        if (!bUsersChannels.containsKey(key)) {
            bUsersChannels[key] = channel
            polling(groupID, userID, newest, channel)
        }
    }

    private suspend fun unsubscribe(call: ApplicationCall, groupID: Long, userID: Long, messageID: Int) {
        val user = bilibiliDynService.deleteBVUser(groupID, userID)

        if (user != null) {
            val username = bilibiliApiService.getUsernameByUID(userID)
            val reply = MessageUtils
                .builder()
                .reply(messageID)
                .text("成功取消订阅UP主 $username")
                .content()
            onebotService.replyMessage(call, reply)
        }

        val key = Pair(groupID, userID)
        val channel = bUsersChannels[key]
        if (channel != null) {
            channel.send(0)
            channel.close()
            bUsersChannels.remove(key)
        }
    }

    private suspend fun polling(groupID: Long, userID: Long, lastParam: Long, channel: Channel<Int>) {
        val scheduler = buildSchedule { minutes { 0 every 5 } }
        val flow = scheduler.asFlow()

        var last = lastParam
        flow.takeWhile {
            !channel.tryReceive().isSuccess
        }.collect {
            val (latest, dynamicID) = bilibiliDynService.getNewestPublishTimestamp(userID)
            if (latest > last) {
                bilibiliDynService.updateBVUser(groupID, userID, latest)
                val username = bilibiliApiService.getUsernameByUID(userID)

                val screenshot = screenshot(dynamicID)
                val absolutePath = screenshot.canonicalPath

                val message = MessageUtils
                    .builder()
                    .image("file://$absolutePath")
                    .text("UP主 $username 有新动态")
                    .content()
                onebotService.sendGroupMessage(groupID, message)
            }
            last = latest
        }
    }

    suspend fun recover() {
        val users = bilibiliDynService.getAllBDynUsers()
        users.forEach {
            val channel = Channel<Int>()
            bUsersChannels[Pair(it.groupID, it.userID)] = channel
            val (latest, _) = bilibiliDynService.getNewestPublishTimestamp(it.userID)
            polling(it.groupID, it.userID, latest, channel)
        }
    }

    private fun screenshot(dynamicID: String): File {
        val driver = BrowserUtils.getDriver()

        // 跳转到截图页面
        driver.get("https://t.bilibili.com/$dynamicID")

        // 隐藏未登录的popup，防止遮挡目标元素
        val popup = driver.findElement(By.cssSelector("div.unlogin-popover.unlogin-popover-avatar"))
        driver.executeScript("arguments[0].style.display = 'none';", popup)

        // 选择要截图的元素对象
        val target = driver.findElement(By.cssSelector("#app > div > div.detail-content > div > div > div"))
        val screenshot = target.getScreenshotAs(OutputType.BYTES)

        // 获取子图的大小
        val content = driver.findElement(By.cssSelector("#app > div > div.detail-content > div > div > div > div.main-content"))
        val bufferedImage = ImageIO.read(screenshot.inputStream())
        val destImage = bufferedImage.getSubimage(0, 0, target.size.width, content.size.height)

        // 持久化截取的图片
        val filename = UUID.randomUUID().toString()
        val file = File("/tmp/images/$filename.png")
        ImageIO.write(destImage, "png", file)

        return file
    }
}

object BilibiliLiveCommand {
    private var bLiveChannels = HashMap<Pair<Long, Long>, Channel<Int>>()
    private val onebotService = OneBotServiceImpl()
    private val bilibiliApiService = BiliBiliApiServiceImpl()
    private val bilibiliLiveService = BilibiliLiveServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        val bLiveCmd = message.message.substringAfter(' ')

        // /blive list
        if (bLiveCmd == "list") {
            list(call, message.groupID)
            return
        }

        // /blive [subscribe | unsubscribe]
        val (op, liveIDStr) = bLiveCmd.split(' ')
        val liveID = liveIDStr.toLong()
        when (op) {
            // /blive subscribe <live_id>
            "subscribe" -> subscribe(call, message.groupID, liveID, message.messageID)
            // /blive unsubscribe <live_id>
            "unsubscribe" -> unsubscribe(call, message.groupID, liveID, message.messageID)
        }
    }

    private suspend fun list(call: ApplicationCall, groupID: Long) {
        val streamers = bilibiliLiveService.getBLiveSteamersByGID(groupID)

        val reply = if (streamers.isEmpty()) {
            "本群没有订阅B站任何主播！"
        } else {
            val userIDs = streamers.map { it.userID }
            val names = bilibiliApiService.getBLiveNamesByUIDs(userIDs)
            val liveIDs = streamers.map { it.liveID }.sorted()
            "本群订阅的B站直播：\n" + (liveIDs zip names).joinToString(separator = "\n") {
                "${it.first}\t${it.second}"
            }
        }

        onebotService.replyMessage(call, reply)
    }

    private suspend fun subscribe(call: ApplicationCall, groupID: Long, liveID: Long, messageID: Int) {
        val liveData = bilibiliApiService.getBLiveRoomData(liveID)
        bilibiliLiveService.upsertBVStreamer(groupID, liveData.uid, liveID)

        val (_, username) = bilibiliApiService.getBLiveDataByUID(liveData.uid)
        val reply = MessageUtils
            .builder()
            .reply(messageID)
            .text("成功关注主播 $username")
            .content()
        onebotService.replyMessage(call, reply)

        val channel = Channel<Int>()
        val key = Pair(groupID, liveID)
        if (!bLiveChannels.containsKey(key)) {
            bLiveChannels[key] = channel
            polling(groupID, liveID, 0, channel)
        }
    }

    private suspend fun unsubscribe(call: ApplicationCall, groupID: Long, liveID: Long, messageID: Int) {
        val streamer = bilibiliLiveService.deleteBVStreamer(groupID, liveID)

        if (streamer != null) {
            val (_, username) = bilibiliApiService.getBLiveDataByUID(streamer.userID)
            val reply = MessageUtils
                .builder()
                .reply(messageID)
                .text("成功取关主播 $username")
                .content()
            onebotService.replyMessage(call, reply)
        }

        val key = Pair(groupID, liveID)
        val channel = bLiveChannels[key]
        if (channel != null) {
            channel.send(0)
            channel.close()
            bLiveChannels.remove(key)
        }
    }

    private suspend fun polling(groupID: Long, liveID: Long, oldStatusParam: Int, channel: Channel<Int>) {
        val scheduler = buildSchedule { minutes { 0 every 1 } }
        val flow = scheduler.asFlow()

        var oldStatus = oldStatusParam
        flow.takeWhile {
            !channel.tryReceive().isSuccess
        }.collect {
            val liveData = bilibiliApiService.getBLiveRoomData(liveID)
            // 边缘触发：只有从未开播到开播才会响应
            if (oldStatus == 0 && liveData.liveStatus == 1) {
                val (cover, username) = bilibiliApiService.getBLiveDataByUID(liveData.uid)
                val message = MessageUtils
                    .builder()
                    .image(cover)
                    .text("主播 $username 开播啦！")
                    .text("https://live.bilibili.com/$liveID")
                    .content()
                onebotService.sendGroupMessage(groupID, message)
            }
            oldStatus = liveData.liveStatus
        }
    }

    suspend fun recover() {
        val streamers = bilibiliLiveService.getBLiveAllSteamers()
        streamers.forEach {
            val channel = Channel<Int>()
            bLiveChannels[Pair(it.groupID, it.liveID)] = channel
            val status = bilibiliApiService.getBLiveRoomData(it.liveID).liveStatus
            polling(it.groupID, it.liveID, status, channel)
        }
    }
}

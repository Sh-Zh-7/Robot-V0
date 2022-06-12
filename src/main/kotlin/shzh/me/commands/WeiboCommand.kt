package shzh.me.commands

import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.utils.asFlow
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.takeWhile
import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import shzh.me.model.dto.MessageDTO
import shzh.me.services.impl.OneBotServiceImpl
import shzh.me.services.impl.WeiboServiceImpl
import shzh.me.utils.BrowserUtils
import shzh.me.utils.MessageUtils
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

object WeiboCommand {
    private val weiboChannels = HashMap<Pair<Long, Long>, Channel<Int>>()
    private val onebotService = OneBotServiceImpl()
    private val weiboService = WeiboServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        val weiboCmd = message.message.substringAfter(' ')

        // /weibo list
        if (weiboCmd == "list") {
            list(call, message.groupID)
            return
        }

        // /weibo [subscribe | unsubscribe]
        val (op, weiboIDStr) = weiboCmd.split(' ')
        val weiboID = weiboIDStr.toLong()
        when (op) {
            // /weibo subscribe <weibo_id>
            "subscribe" -> subscribe(call, message.groupID, weiboID, message.messageID)
            // /weibo unsubscribe <weibo_id>
            "unsubscribe" -> unsubscribe(call, message.groupID, weiboID, message.messageID)
        }
    }

    private suspend fun list(call: ApplicationCall, groupID: Long) {
        val weibos = weiboService.getWeibosByGID(groupID)

        val reply = if (weibos.isEmpty()) {
            "本群没有订阅微博任何博主！"
        } else {
            val weiboIDs = weibos.map { it.weiboID }
            val usernames = weibos.map { it.username }
            "本群订阅的微博博主：\n" + (weiboIDs zip usernames).joinToString(separator = "\n") {
                "${it.first}\t${it.second}"
            }
        }

        onebotService.replyMessage(call, reply)
    }

    private suspend fun subscribe(call: ApplicationCall, groupID: Long, weiboID: Long, messageID: Int) {
        val username = weiboService.getUsernameByWeiboID(weiboID)
        val published = weiboService.getLatestWeiboByWeiboID(weiboID).publishedDate
        weiboService.insertWeiboUser(groupID, weiboID, username, published)

        val reply = MessageUtils
            .builder()
            .reply(messageID)
            .text("成功关注微博博主 $username")
            .content()
        onebotService.replyMessage(call, reply)

        val channel = Channel<Int>()
        val key = Pair(groupID, weiboID)
        if (!weiboChannels.containsKey(key)) {
            weiboChannels[key] = channel
            polling(groupID, weiboID, published, channel)
        }
    }

    private suspend fun unsubscribe(call: ApplicationCall, groupID: Long, weiboID: Long, messageID: Int) {
        val user = weiboService.deleteWeiboUser(groupID, weiboID)

        if (user != null) {
            val username = weiboService.getUsernameByWeiboID(weiboID)
            val reply = MessageUtils
                .builder()
                .reply(messageID)
                .text("成功取消订阅博主 $username")
                .content()
            onebotService.replyMessage(call, reply)
        }

        val key = Pair(groupID, weiboID)
        val channel = weiboChannels[key]
        if (channel != null) {
            channel.send(0)
            channel.close()
            weiboChannels.remove(key)
        }
    }

    private suspend fun polling(groupID: Long, weiboID: Long, lastParam: Date, channel: Channel<Int>) {
        val scheduler = buildSchedule { minutes { 0 every 5 } }
        val flow = scheduler.asFlow()

        var last = lastParam
        flow.takeWhile {
            !channel.tryReceive().isSuccess
        }.collect {
            val latest = weiboService.getLatestWeiboByWeiboID(weiboID)
            if (latest.publishedDate > last) {
                weiboService.updateWeiboUser(groupID, weiboID, latest.publishedDate)

                val screenshot = screenshotWeibo(latest.link)
                val absolutePath = screenshot.canonicalPath

                val rely = MessageUtils
                    .builder()
                    .image("file://$absolutePath")
                    .text("微博用户 ${latest.author} 有新动态")
                    .content()
                onebotService.sendGroupMessage(groupID, rely)
            }
            last = latest.publishedDate
        }
    }

    private fun screenshotWeibo(link: String): File {
        val driver = BrowserUtils.getDriver()

        driver.get(link)
        TimeUnit.SECONDS.sleep(5)   // 等待页面初始的跳转

        val target = driver.findElement(By.cssSelector("article"))
        val screenshot = target.getScreenshotAs(OutputType.BYTES)

        val image = ImageIO.read(screenshot.inputStream())

        val filename = UUID.randomUUID().toString()
        val file = File("/tmp/images/$filename.png")
        ImageIO.write(image, "png", file)

        return file
    }

    suspend fun recover() {
        val users = weiboService.getAllWeiboUsers()
        users.forEach {
            val channel = Channel<Int>()
            weiboChannels[Pair(it.groupID, it.weiboID)] = channel
            val latest = weiboService.getLatestWeiboByWeiboID(it.weiboID)
            polling(it.groupID, it.weiboID, latest.publishedDate, channel)
        }
    }

}
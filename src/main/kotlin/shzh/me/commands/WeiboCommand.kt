package shzh.me.commands

import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.utils.asFlow
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.takeWhile
import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import shzh.me.services.*
import shzh.me.utils.MessageUtils
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.collections.HashMap

val weiboChannels = HashMap<Pair<Long, Long>, Channel<Int>>()

suspend fun handleWeibo(call: ApplicationCall, command: String, groupID: Long, messageID: Int) {
    val weiboCmd = command.substringAfter(' ')

    // /weibo list
    if (weiboCmd == "list") {
        handleWeiboList(call, groupID)
        return
    }

    // For /weibo [subscribe | unsubscribe]
    val (op, weiboIDStr) = weiboCmd.split(' ')
    val weiboID = weiboIDStr.toLong()
    when (op) {
        // /weibo subscribe <weibo_id>
        "subscribe" -> handleSubWeibo(call, groupID, weiboID, messageID)
        // /weibo unsubscribe <weibo_id>
        "unsubscribe" -> handleUnsubWeibo(call, groupID, weiboID, messageID)
    }
}

private suspend fun handleWeiboList(call: ApplicationCall, groupID: Long) {
    val weibos = getWeibosByGID(groupID)

    val reply = if (weibos.isEmpty()) {
        "本群没有订阅微博任何博主！"
    } else {
        val weiboIDs = weibos.map { it.weiboID }
        val usernames = weibos.map { it.username }
        "本群订阅的微博博主：\n" + (weiboIDs zip usernames).joinToString(separator = "\n") {
            "${it.first}\t${it.second}"
        }
    }

    replyMessage(call, reply)
}

private suspend fun handleSubWeibo(call: ApplicationCall, groupID: Long, weiboID: Long, messageID: Int) {
    val username = getUsernameByWeiboID(weiboID)
    val published = getLatestWeiboByWeiboID(weiboID).publishedDate
    insertWeiboUser(groupID, weiboID, username, published)

    val reply = MessageUtils
        .builder()
        .reply(messageID)
        .text("成功关注微博博主 $username", newline = false)
        .content()
    replyMessage(call, reply)

    val channel = Channel<Int>()
    val key = Pair(groupID, weiboID)
    if (!weiboChannels.containsKey(key)) {
        weiboChannels[key] = channel
        poolingWeibo(groupID, weiboID, published, channel)
    }
}

private suspend fun handleUnsubWeibo(call: ApplicationCall, groupID: Long, weiboID: Long, messageID: Int) {
    val user = deleteWeiboUser(groupID, weiboID)

    if (user != null) {
        val username = getUsernameByWeiboID(weiboID)
        val reply = MessageUtils
            .builder()
            .reply(messageID)
            .text("成功取消订阅博主 $username", newline = false)
            .content()
        replyMessage(call, reply)
    }

    val key = Pair(groupID, weiboID)
    val channel = weiboChannels[key]
    if (channel != null) {
        channel.send(0)
        channel.close()
        weiboChannels.remove(key)
    }
}

private suspend fun poolingWeibo(groupID: Long, weiboID: Long, lastParam: Date, channel: Channel<Int>) {
    val scheduler = buildSchedule { minutes { 0 every 1 } }
    val flow = scheduler.asFlow()

    var last = lastParam
    flow.takeWhile {
        !channel.tryReceive().isSuccess
    }.collect {
        val latest = getLatestWeiboByWeiboID(weiboID)
        if (latest.publishedDate > last) {
            updateWeiboUser(groupID, weiboID, latest.publishedDate)

            val screenshot = screenshotWeibo(latest.link)
            val absolutePath = screenshot.canonicalPath

            val rely = MessageUtils
                .builder()
                .image("file://$absolutePath")
                .text("微博用户 ${latest.author} 有新动态", newline = false)
                .content()
            sendGroupMessage(groupID, rely)
        }
        last = latest.publishedDate
    }
}

private fun screenshotWeibo(link: String): File {
    val options = ChromeOptions()
    options.addArguments("--headless")
    options.addArguments("--disable-gpu");
    options.addArguments("--window-size=1980,960");
    val driver = ChromeDriver(options)

    driver.get(link)
    TimeUnit.SECONDS.sleep(5)   // Wait until redirect done

    val target = driver.findElement(By.cssSelector("article"))
    val screenshot = target.getScreenshotAs(OutputType.FILE)

    val image = ImageIO.read(screenshot.inputStream())

    val filename = UUID.randomUUID().toString()
    val file = File("./images/weibo/$filename.png")
    ImageIO.write(image, "png", file)

    driver.quit()

    return file
}

suspend fun recoverPoolingWeibo() {
    val users = getAllWeiboUsers()
    users.forEach {
        val channel = Channel<Int>()
        weiboChannels[Pair(it.groupID, it.weiboID)] = channel
        val latest = getLatestWeiboByWeiboID(it.weiboID)
        poolingWeibo(it.groupID, it.weiboID, latest.publishedDate, channel)
    }
}

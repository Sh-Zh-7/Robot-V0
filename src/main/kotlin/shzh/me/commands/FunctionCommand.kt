package shzh.me.commands

import io.ktor.server.application.*
import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import shzh.me.services.deleteMessage
import shzh.me.services.getMessage
import shzh.me.services.replyMessage
import shzh.me.services.sendGroupMessage
import shzh.me.utils.MessageUtils
import java.io.File
import java.io.FileWriter
import java.util.*
import javax.imageio.ImageIO
import kotlin.collections.HashMap

// groupID -> (lastMessage, count)
val lastMessages = HashMap<Long, Pair<String, Int>>()

suspend fun handleCallback(message: String, userID: Long) {
    val regex = Regex("\\[CQ:reply,id=(-?\\d+)]\\s*撤回")
    val mid = regex.find(message)!!.groupValues[1]

    val msg = getMessage(mid)
    if (msg.sender.userID == userID) {
        deleteMessage(mid)
    }
}

suspend fun handleRepeat(message: String, groupID: Long) {
    if (!lastMessages.containsKey(groupID)) {
        // First message of group, usually happen when server restart
        lastMessages[groupID] = Pair(message, 1)
        return
    }

    val (lastMessage, count) = lastMessages[groupID]!!
    val value = if (lastMessage == message) {
        // Repeat when the message is already repeat 3 times
        if (count + 1 == 3) {
            sendGroupMessage(groupID, message)
            Pair(lastMessage, 4)
        } else {
            // Same message, increase count
            Pair(lastMessage, count + 1)
        }
    } else {
        // Different message, reset to 1
        Pair(message, 1)
    }
    lastMessages[groupID] = value
}

suspend fun handleQuote(call: ApplicationCall, message: String) {
    val regex = Regex("\\[CQ:reply,id=(-?\\d+)]\\s*/quote")
    val mid = regex.find(message)!!.groupValues[1]

    val msg = getMessage(mid)
    val outPath = renderPipeline(msg.sender.userID, msg.message, msg.sender.nickname)

    val reply = MessageUtils
        .builder()
        .image("file://$outPath", newline = false)
        .content()
    replyMessage(call, reply)
}

private fun renderPipeline(userID: Long, quote: String, username: String): String {
    val template = "<style>body{margin:0}.container{display:flex;width:950px}.cover{position:relative;width:450px;height:450px}.cover::after{position:absolute;content:'';width:100%;height:100%;top:0;left:0;box-shadow:-60px 0 50px black inset}img{filter:grayscale(100%)}.quote{color:white;background:black;display:flex;flex-direction:column;align-items:center;justify-content:center;width:500px;height:450px;margin:0}blockquote{font-family:serif;font-size:2.5rem}figcaption{font-size:1.5rem;font-style:italic}</style><body><div class=\"container\"><div class=\"cover\"><img src=\"http://q1.qlogo.cn/g?b=qq&s=640&nk=$userID\"alt=\"avatar\"width=\"450px\"height=\"450px\"></div><figure class=\"quote\"><blockquote>$quote</blockquote><figcaption>——$username</figcaption></figure></div></body>"

    val basename = UUID.randomUUID()
    val filename = "temp/$basename.html"
    val html = File(filename)
    html.writeText(template)

    val options = ChromeOptions()
    options.addArguments("--headless")
    options.addArguments("--disable-gpu");
    options.addArguments("--window-size=1980,960");
    val driver = ChromeDriver(options)

    // Switch to Bilibili dynamic page
    driver.get("file://${html.canonicalPath}")
    val target = driver.findElement(By.cssSelector(".container"))
    val screenshot = target.getScreenshotAs(OutputType.FILE)
    val image = ImageIO.read(screenshot.inputStream())

    val file = File("images/$basename.png")
    ImageIO.write(image, "png", file)

    driver.quit()

    return file.canonicalPath
}
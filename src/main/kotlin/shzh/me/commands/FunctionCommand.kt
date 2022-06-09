package shzh.me.commands

import io.ktor.server.application.*
import org.openqa.selenium.By
import org.openqa.selenium.OutputType
import shzh.me.model.dto.MessageDTO
import shzh.me.services.impl.OneBotServiceImpl
import shzh.me.utils.BrowserUtils
import shzh.me.utils.CQCodeUtils
import shzh.me.utils.MessageUtils
import java.io.File
import java.util.*
import javax.imageio.ImageIO

object CallbackCommand {
    private val onebotService = OneBotServiceImpl()

    suspend fun handle(message: MessageDTO) {
        val regex = Regex("${CQCodeUtils.replyPattern}\\s*撤回")
        val mid = regex.find(message.message)!!.groupValues[1]

        val msg = onebotService.getMessage(mid)
        if (msg.sender.userID == message.sender.userID) {
            onebotService.deleteMessage(mid)
        }
    }
}

object RepeatCommand {
    // groupID -> (lastMessage, count)
    private val lastMessages = HashMap<Long, Pair<String, Int>>()
    private val onebotService = OneBotServiceImpl()

    suspend fun handle(message: MessageDTO) {
        if (!lastMessages.containsKey(message.groupID)) {
            // First message of group, usually happen when server restart
            lastMessages[message.groupID] = Pair(message.message, 1)
            return
        }

        val (lastMessage, count) = lastMessages[message.groupID]!!
        val value = if (lastMessage == message.message) {
            // Repeat when the message is already repeat 3 times
            if (count + 1 == 3) {
                onebotService.sendGroupMessage(message.groupID, message.message)
                Pair(lastMessage, 4)
            } else {
                // Same message, increase count
                Pair(lastMessage, count + 1)
            }
        } else {
            // Different message, reset to 1
            Pair(message.message, 1)
        }
        lastMessages[message.groupID] = value
    }
}

object QuoteCommand {
    private val onebotService = OneBotServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        val regex = Regex("${CQCodeUtils.replyPattern}\\s*/quote")
        val mid = regex.find(message.message)!!.groupValues[1]

        val msg = onebotService.getMessage(mid)
        val outPath = renderPipeline(msg.sender.userID, msg.message, msg.sender.nickname)

        val reply = MessageUtils
            .builder()
            .image("file://$outPath")
            .content()
        onebotService.replyMessage(call, reply)
    }

    private fun renderPipeline(userID: Long, quote: String, username: String): String {
        val template = """
<style>body{margin:0}.container{display:flex;width:950px}.cover{position:relative;width:450px;height:450px}
.cover::after{position:absolute;content:'';width:100%;height:100%;top:0;left:0;box-shadow:-60px 0 50px black inset}
img{filter:grayscale(100%)}.quote{color:white;background:black;display:flex;flex-direction:column;align-items:center;
justify-content:center;width:500px;height:450px;margin:0}blockquote{font-family:serif;font-size:2.5rem}
figcaption{font-size:1.5rem;font-style:italic}</style><body><div class="container"><div class="cover">
<img src="http://q1.qlogo.cn/g?b=qq&s=640&nk=$userID"alt="avatar"width="450px"height="450px"></div>
<figure class="quote"><blockquote>$quote</blockquote><figcaption>——$username</figcaption></figure></div></body>
        """

        val basename = UUID.randomUUID()
        val filename = "temp/$basename.html"
        val html = File(filename)
        html.writeText(template)

        val driver = BrowserUtils.getDriver()

        // Switch to Bilibili dynamic page
        driver.get("file://${html.canonicalPath}")
        val target = driver.findElement(By.cssSelector(".container"))
        val screenshot = target.getScreenshotAs(OutputType.BYTES)
        val image = ImageIO.read(screenshot.inputStream())

        val file = File("images/quotes/$basename.png")
        ImageIO.write(image, "png", file)

        return file.canonicalPath
    }
}

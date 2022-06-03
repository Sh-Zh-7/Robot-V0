package shzh.me.commands

import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.unbescape.html.HtmlEscape
import shzh.me.services.replyMessage
import shzh.me.utils.MessageUtils
import java.util.concurrent.TimeUnit

suspend fun handleMath(call: ApplicationCall, command: String, messageID: Int) {
    val expr = command.substringAfter(' ')
    val escaped = HtmlEscape.unescapeHtml(expr)
    val script = listOf("wolframscript", "-code", escaped)

    val proc: Process
    withContext(Dispatchers.IO) {
        proc = ProcessBuilder(script)
            .redirectOutput(ProcessBuilder.Redirect.PIPE)
            .redirectError(ProcessBuilder.Redirect.PIPE)
            .start()

        proc.waitFor(10000, TimeUnit.MILLISECONDS)
    }
    val result = proc.inputStream.bufferedReader().readText()

    val reply = MessageUtils
        .builder()
        .reply(messageID)
        .text("结果为：${result.trim()}")
        .text("由Wolfram强力驱动", newline = false)
        .content()
    replyMessage(call, reply)
}
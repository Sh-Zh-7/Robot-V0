package shzh.me.commands

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.unbescape.html.HtmlEscape
import shzh.me.model.vo.GroupReplyVO
import java.util.concurrent.TimeUnit

suspend fun handleMath(call: ApplicationCall, command: String) {
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

    val reply = "结果为：${result.trim()}\\nWolfram强力驱动"
    val res = Json.encodeToString(GroupReplyVO(reply))
    call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
}
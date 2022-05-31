package shzh.me.commands

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import shzh.me.services.getVideoInfo

suspend fun handleBvInfo(call: ApplicationCall, command: String, type: String) {
    val av = command.substringAfter(' ')
    val info = getVideoInfo(av, type)

    val reply = "{\"reply\": \"$info\"}"
    call.respondText(reply, ContentType.Application.Json, HttpStatusCode.OK)
}
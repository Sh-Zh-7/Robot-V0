package shzh.me.commands

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import shzh.me.services.getVideoInfo

suspend fun handleBvInfo(call: ApplicationCall, command: String, type: String) {
    val av = command.substringAfter(' ')
    val info = getVideoInfo(av, type)

    val res = Json.encodeToString(info.toString())
    call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
}
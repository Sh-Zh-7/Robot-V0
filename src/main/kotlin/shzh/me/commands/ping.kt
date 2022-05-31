package shzh.me.commands

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun handlePing(call: ApplicationCall) {
    val res = "{\"reply\": \"pong!\"}"
    call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
}
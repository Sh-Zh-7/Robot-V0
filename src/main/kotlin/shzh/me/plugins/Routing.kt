package shzh.me.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun Application.configureRouting() {
    routing {
        post("/") {
            val body = call.receiveText()
            val bodyJson = Json.parseToJsonElement(body)
            val postType = bodyJson.jsonObject["post_type"]!!.jsonPrimitive.content

            when (postType) {
                "message" -> {
                    val msg = bodyJson.jsonObject["message"]!!.jsonPrimitive.content
                    if (msg == "/ping") {
                        val res = "{\"reply\": \"pong!\"}"
                        call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
                    }
                }
                "meta_event" -> println("Heartbeat package received!")
                else -> println("Unknown package type received!!")
           }
        }
    }
}

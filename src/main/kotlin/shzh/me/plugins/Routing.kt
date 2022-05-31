package shzh.me.plugins

import io.ktor.http.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import shzh.me.commands.handleDice
import shzh.me.commands.handleMath
import shzh.me.commands.handlePing

fun Application.configureRouting() {
    routing {
        post("/") {
            val body = call.receiveText()
            val bodyJson = Json.parseToJsonElement(body)
            val postType = bodyJson.jsonObject["post_type"]!!.jsonPrimitive.content

            when (postType) {
                "message" -> {
                    val msg = bodyJson.jsonObject["message"]!!.jsonPrimitive.content

                    when {
                        msg == "/ping" -> handlePing(call)
                        "/dice" in msg -> handleDice(call, msg)
                        "/math" in msg -> handleMath(call, msg)
                    }
                }
                "meta_event" -> println("Heartbeat package received!")
                else -> println("Unknown package type received!!")
           }
        }
    }
}

package shzh.me.plugins

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import shzh.me.commands.*
import shzh.me.model.dto.MessageDTO

val format = Json { ignoreUnknownKeys = true }

fun Application.configureRouting() {
    routing {
        post("/") {
            val body = call.receiveText()
            val bodyJson = Json.parseToJsonElement(body)
            val postType = bodyJson.jsonObject["post_type"]!!.jsonPrimitive.content

            when (postType) {
                "message" -> {
                    val msg = format.decodeFromString<MessageDTO>(body)

                    when {
                        msg.message == "/ping" -> handlePing(call, msg.messageID)
                        "/av" in msg.message -> handleBvInfo(call, msg.message, "aid")
                        "/bv" in msg.message -> handleBvInfo(call, msg.message, "bvid")
                        "/dice" in msg.message -> handleDice(call, msg.message, msg.messageID)
                        "/math" in msg.message -> handleMath(call, msg.message, msg.messageID)
                        "https://github.com/" in msg.message -> handleGithub(call, msg.message)
                         msg.message matches Regex("\\[CQ:reply,id=(-?\\d+)]\\s*撤回") -> handleCallback(msg.message)
                    }
                }
                "meta_event" -> println("Heartbeat package received!")
                else -> println("Unknown package type received!!")
           }
        }
    }
}

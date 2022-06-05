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
                        "/ping" == msg.message -> handlePing(call, msg.messageID)
                        "/dice" in msg.message -> handleDice(call, msg.message, msg.messageID)
                        "/math" in msg.message -> handleMath(call, msg.message, msg.messageID)
                        "/bili" in msg.message -> handleBDyn(call, msg.message, msg.groupID, msg.messageID)
                        "/blive" in msg.message -> handleBLive(call, msg.message, msg.groupID, msg.messageID)
                        "/music" in msg.message -> handleMusic(call, msg.message)
                        "https://github.com/" in msg.message -> handleGithub(call, msg.message)
                        "https://www.bilibili.com/video/" in msg.message -> handleBvInfo(call, msg.message)
                        Regex("\\[CQ:reply,id=(-?\\d+)]\\s*撤回") matches msg.message -> handleCallback(msg.message, msg.sender.userID)
                    }

                    handleRepeat(msg.message, msg.groupID)
                    recordMessage(msg.groupID, msg.sender.userID, msg.sender.nickname, msg.message)
                }
                "meta_event" -> println("Heartbeat package received!")
                else -> println("Unknown package type received!!")
           }
        }
    }
}

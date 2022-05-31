package shzh.me.plugins

import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import shzh.me.commands.*

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
                        "/av" in msg -> handleBvInfo(call, msg, "aid")
                        "/bv" in msg -> handleBvInfo(call, msg, "bvid")
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

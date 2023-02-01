package shzh.me.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import shzh.me.commands.*
import shzh.me.format
import shzh.me.model.dto.MessageDTO
import shzh.me.utils.CQCodeUtils

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
                        "/ping" == msg.message -> PingCommand.handle(call, msg)
                        "/diy" == msg.message -> DiyCommand.handle(call, msg)
                        "/progress" == msg.message -> ProgressCommand.handle(call, msg)
                        "/dice" in msg.message -> DiceCommand.handle(call, msg)
                        "/math" in msg.message -> MathCommand.handle(call, msg)
                        "/music" in msg.message -> NeteaseCommand.handle(call, msg)
                        "/find" in msg.message -> HistoryCommand.handle(call, msg)
                        "/bili" in msg.message -> BilibiliDynamicCommand.handle(call, msg)
                        "/blive" in msg.message -> BilibiliLiveCommand.handle(call, msg)
                        "/weibo" in msg.message -> WeiboCommand.handle(call, msg)
                        "/zhihu" in msg.message -> ZhihuCommand.handle(call, msg)
                        "/github" in msg.message -> GithubCommand.handle(call, msg)
                        "https://github.com/" in msg.message -> GithubLinkCommand.handle(call, msg)
                        "https://www.bilibili.com/video/" in msg.message -> BilibiliVideoCommand.handle(call, msg)
                        Regex("${CQCodeUtils.replyPattern}\\s*撤回") matches msg.message
                        -> CallbackCommand.handle(msg)

                        Regex("${CQCodeUtils.replyPattern}\\s*/quote") matches msg.message
                        -> QuoteCommand.handle(call, msg)
                    }

                    RepeatCommand.handle(msg)
                    HistoryCommand.recordMessage(msg)
                }

                "meta_event" -> {}
                else -> println("Unknown package type received!!")
            }
        }
    }
}

package shzh.me.commands

import io.ktor.server.application.*
import shzh.me.services.replyMessage
import shzh.me.utils.MessageUtils

suspend fun handleGithub(call: ApplicationCall, link: String) {
    val regex = "(https://github.com/)".toRegex()
    val openGraph = regex.replace(link, "https://opengraph.githubassets.com/1/")

    val reply = MessageUtils
        .builder()
        .image(openGraph)
        .content()
    replyMessage(call, reply)
}
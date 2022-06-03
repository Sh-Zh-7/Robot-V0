package shzh.me.commands

import io.ktor.server.application.*
import shzh.me.services.replyMessage
import shzh.me.services.searchMusicByKeyword
import shzh.me.utils.MessageUtils

suspend fun handleMusic(call: ApplicationCall, message: String) {
    val name = message.substringAfter(' ')

    val song = searchMusicByKeyword(name)

    val reply = MessageUtils
        .builder()
        .music("163", song.id)
        .content()
    replyMessage(call, reply)
}
package shzh.me.commands

import io.ktor.server.application.*
import shzh.me.model.dto.MessageDTO
import shzh.me.services.impl.NeteaseServiceImpl
import shzh.me.services.impl.OneBotServiceImpl
import shzh.me.utils.MessageUtils

object NeteaseCommand {
    private val onebotService = OneBotServiceImpl()
    private val neteaseService = NeteaseServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        val name = message.message.substringAfter(' ')

        val song = neteaseService.searchMusicByKeyword(name)

        val reply = MessageUtils
            .builder()
            .music("163", song.id)
            .content()
        onebotService.replyMessage(call, reply)
    }
}

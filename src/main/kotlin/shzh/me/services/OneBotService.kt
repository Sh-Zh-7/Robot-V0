package shzh.me.services

import io.ktor.server.application.*
import shzh.me.model.dto.MessageDTO


interface OneBotService {
    suspend fun deleteMessage(id: String)

    suspend fun getMessage(id: String): MessageDTO

    suspend fun sendGroupMessage(gid: Long, message: String)

    // OneBot wrapper
    suspend fun replyMessage(call: ApplicationCall, message: String)
}

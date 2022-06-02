package shzh.me.services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import shzh.me.model.DataWrapper
import shzh.me.model.dto.MessageDTO
import shzh.me.model.vo.GroupMessageVo

val client = HttpClient(CIO)

suspend fun deleteMessage(id: String) {
    client.post("http://127.0.0.1:5700/delete_msg") {
        contentType(ContentType.Application.Json)
        setBody("{\"message_id\":$id}")
    }
}

suspend fun getMessage(id: String): MessageDTO {
    val res = client.post("http://127.0.0.1:5700/get_msg") {
        contentType(ContentType.Application.Json)
        setBody("{\"message_id\":$id}")
    }

    val ret = format.decodeFromString<DataWrapper<MessageDTO>>(res.bodyAsText())
    return ret.data
}

suspend fun sendGroupMessage(gid: Long, message: String) {
    val body = Json.encodeToString(GroupMessageVo(gid, message))

    client.post("http://127.0.0.1:5700/send_group_msg") {
        contentType(ContentType.Application.Json)
        setBody(body)
    }
}
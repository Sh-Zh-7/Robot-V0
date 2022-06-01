package shzh.me.services

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*

suspend fun deleteMessage(id: String) {
    val client = HttpClient(CIO)

    client.post("http://127.0.0.1:5700/delete_msg") {
        contentType(ContentType.Application.Json)
        setBody("{\"message_id\":$id}")
    }
}
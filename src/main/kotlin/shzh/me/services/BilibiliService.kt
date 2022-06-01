package shzh.me.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import shzh.me.model.bo.BvInfo

val format = Json { ignoreUnknownKeys = true }

suspend fun getVideoInfo(bv: String): BvInfo {
    val client = HttpClient(CIO) {
        defaultRequest {
            url {
                host = "api.bilibili.com"
                path("x/web-interface/")
            }
        }
    }

    val response = client.get("view") {
        url { parameters.append("bvid", bv) }
    }.body<String>()

    return format.decodeFromString(response)
}
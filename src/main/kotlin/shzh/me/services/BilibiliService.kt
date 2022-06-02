package shzh.me.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import shzh.me.model.DataWrapper
import shzh.me.model.bo.BLiveInfo
import shzh.me.model.bo.BvData

val format = Json { ignoreUnknownKeys = true }

suspend fun getVideoInfo(bv: String): BvData {
    val client = HttpClient(CIO) {
        defaultRequest { url { host = "api.bilibili.com" } }
    }

    val response = client.get("x/web-interface/view") {
        url { parameters.append("bvid", bv) }
    }.body<String>()

    return format.decodeFromString<DataWrapper<BvData>>(response).data
}

suspend fun getBLiveInfo(liveID: String): BLiveInfo {
    val client = HttpClient(CIO) {
        defaultRequest { url { host = "api.live.bilibili.com" } }
    }

    val response = client.get("room/v1/Room/room_init") {
        url { parameters.append("id", liveID) }
    }.body<String>()

    return format.decodeFromString<DataWrapper<BLiveInfo>>(response).data
}
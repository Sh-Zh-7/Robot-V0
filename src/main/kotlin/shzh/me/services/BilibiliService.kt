package shzh.me.services

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.ktorm.dsl.*
import shzh.me.model.DataWrapper
import shzh.me.model.bo.BLiveInfo
import shzh.me.model.bo.BvData
import shzh.me.model.dao.GroupSubBVStreamer
import shzh.me.model.dao.db

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

suspend fun getBLiveCoverByUID(userID: Long): Pair<String, String> {
    val userIDStr = userID.toString()

    val client = HttpClient(CIO) {
        defaultRequest { url { host = "api.live.bilibili.com" } }
    }

    val response = client.get("room/v1/Room/get_status_info_by_uids") {
        url { parameters.append("uids[]", userIDStr) }
    }.body<String>()

    val bLiveJson = Json.parseToJsonElement(response)
        .jsonObject["data"]!!
        .jsonObject[userIDStr]!!

    val cover = bLiveJson.jsonObject["cover_from_user"]!!.jsonPrimitive.content
    val username = bLiveJson.jsonObject["uname"]!!.jsonPrimitive.content

    return Pair(cover, username)
}

fun listBVStreamer(groupID: Long) {
    db.from(GroupSubBVStreamer).select()
}

fun subscribeBVStreamer(groupID: Long, liveID: Long) {
    db.insert(GroupSubBVStreamer) {
        set(it.groupID, groupID)
        set(it.liveID, liveID)
    }
}

fun unsubscribeBVStreamer(groupID: Long, liveID: Long) {
    db.delete(GroupSubBVStreamer) {
        (it.groupID eq groupID) and (it.liveID eq liveID)
    }
}
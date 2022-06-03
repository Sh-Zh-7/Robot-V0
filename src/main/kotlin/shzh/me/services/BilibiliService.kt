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
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import shzh.me.model.DataWrapper
import shzh.me.model.bo.BLiveInfo
import shzh.me.model.bo.BvData
import shzh.me.model.dao.GroupSubBVStreamer
import shzh.me.model.dao.db
import shzh.me.model.dao.groupSubBVStreamers

val format = Json { ignoreUnknownKeys = true }

suspend fun getBVData(bv: String): BvData {
    val client = HttpClient(CIO) {
        defaultRequest { url { host = "api.bilibili.com" } }
    }

    val response = client.get("x/web-interface/view") {
        url { parameters.append("bvid", bv) }
    }.body<String>()

    return format.decodeFromString<DataWrapper<BvData>>(response).data
}

suspend fun getBLiveRoomData(liveID: Long): BLiveInfo {
    val client = HttpClient(CIO) {
        defaultRequest { url { host = "api.live.bilibili.com" } }
    }

    val response = client.get("room/v1/Room/room_init") {
        url { parameters.append("id", liveID.toString()) }
    }.body<String>()

    return format.decodeFromString<DataWrapper<BLiveInfo>>(response).data
}

suspend fun getBLiveNamesByUIDs(userIDs: List<Long>): List<String> {
    val client = HttpClient(CIO) {
        defaultRequest { url { host = "api.live.bilibili.com" } }
    }

    val param = userIDs.joinToString(prefix = "[", postfix = "]")
    val response = client.post("room/v1/Room/get_status_info_by_uids") {
        setBody("{\"uids\":$param}")
    }.body<String>()

    val data = Json.parseToJsonElement(response).jsonObject["data"]!!
    return userIDs.map {
        data.jsonObject[it.toString()]!!.jsonObject["uname"]!!.jsonPrimitive.content
    }
}

suspend fun getBLiveDataByUID(userID: Long): Pair<String, String> {
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

fun getBLiveAllSteamers(): List<GroupSubBVStreamer> {
    return db.groupSubBVStreamers.toList()
}

fun getBLiveSteamersByGID(groupID: Long): List<GroupSubBVStreamer> {
    return db.groupSubBVStreamers
        .filter { it.groupID eq groupID }
        .toList()
}

fun upsertBVStreamer(groupID: Long, userID: Long, liveID: Long) {
    db.groupSubBVStreamers.find {
        (it.groupID eq groupID) and (it.liveID eq liveID)
    } ?: run {
        val entity = GroupSubBVStreamer {
            this.groupID = groupID
            this.userID = userID
            this.liveID = liveID
        }
        db.groupSubBVStreamers.add(entity)
    }
}

fun deleteBVStreamer(groupID: Long, liveID: Long) {
    val entity = db.groupSubBVStreamers.find {
        (it.groupID eq groupID) and (it.liveID eq liveID)
    } ?: return
    entity.delete()
}
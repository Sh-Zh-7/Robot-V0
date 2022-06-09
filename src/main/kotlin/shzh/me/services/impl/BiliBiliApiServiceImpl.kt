package shzh.me.services.impl

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import shzh.me.format
import shzh.me.model.DataWrapper
import shzh.me.model.bo.BLiveInfo
import shzh.me.model.bo.BvData
import shzh.me.services.BilibiliApiService

class BiliBiliApiServiceImpl: BilibiliApiService {
    private val biliClient = HttpClient(CIO) {
        defaultRequest { url { host = "api.bilibili.com" } }
    }

    private val biliLiveClient = HttpClient(CIO) {
        defaultRequest { url { host = "api.live.bilibili.com" } }
    }

    override suspend fun getBVData(bv: String): BvData {
        val response = biliClient.get("x/web-interface/view") {
            url { parameters.append("bvid", bv) }
        }.body<String>()

        return format.decodeFromString<DataWrapper<BvData>>(response).data
    }

    override suspend fun getBLiveRoomData(liveID: Long): BLiveInfo {
        val response = biliLiveClient.get("room/v1/Room/room_init") {
            url { parameters.append("id", liveID.toString()) }
        }.body<String>()

        return format.decodeFromString<DataWrapper<BLiveInfo>>(response).data
    }

    override suspend fun getBLiveNamesByUIDs(userIDs: List<Long>): List<String> {
        val param = userIDs.joinToString(prefix = "[", postfix = "]")
        val response = biliLiveClient.post("room/v1/Room/get_status_info_by_uids") {
            setBody("{\"uids\":$param}")
        }.body<String>()

        val data = Json.parseToJsonElement(response).jsonObject["data"]!!
        return userIDs.map {
            data.jsonObject[it.toString()]!!.jsonObject["uname"]!!.jsonPrimitive.content
        }
    }

    override suspend fun getBLiveDataByUID(userID: Long): Pair<String, String> {
        val userIDStr = userID.toString()

        val response = biliLiveClient.get("room/v1/Room/get_status_info_by_uids") {
            url { parameters.append("uids[]", userIDStr) }
        }.body<String>()

        val bLiveJson = Json.parseToJsonElement(response)
            .jsonObject["data"]!!
            .jsonObject[userIDStr]!!
        val cover = bLiveJson.jsonObject["cover_from_user"]!!.jsonPrimitive.content
        val username = bLiveJson.jsonObject["uname"]!!.jsonPrimitive.content

        return Pair(cover, username)
    }

    override suspend fun getUsernameByUID(userID: Long): String {
        val (_, username) = getBLiveDataByUID(userID)

        return username
    }
}
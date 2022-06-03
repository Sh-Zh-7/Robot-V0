package shzh.me.commands

import dev.inmo.krontab.doInfinity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import shzh.me.model.bo.BLiveInfo
import shzh.me.model.vo.GroupReplyVO
import shzh.me.services.*

var bLivePooling = true

suspend fun handleBvInfo(call: ApplicationCall, command: String) {
    val regex = Regex("https://www\\.bilibili\\.com/video/BV(\\w{10})")
    val bv = regex.find(command)!!.groupValues[1]
    val data = getBVData(bv)

    val res = Json.encodeToString(GroupReplyVO(data.toString()))
    call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
}

suspend fun handleBLive(call: ApplicationCall, command: String, groupID: Long) {
    val bLiveCmd = command.substringAfter(' ')

    // /blive list
    if (bLiveCmd == "list") {
        handleBLiveList(call, groupID)
        return
    }

    // For /blive [subscribe | unsubscribe]
    val (op, liveIDStr) = bLiveCmd.split(' ')
    val liveID = liveIDStr.toLong()
    when (op) {
        // /blive subscribe <live_id>
        "subscribe" -> handleSubBLive(call, liveID, groupID)
        // /blive unsubscribe <live_id>
        "unsubscribe" -> handleUnsubBLive(call, liveID, groupID)
    }
}

private suspend fun handleBLiveList(call: ApplicationCall, groupID: Long) {
    val streamers = getBLiveSteamersByGID(groupID)

    val reply = if (streamers.isEmpty()) {
        "本群没有订阅B站任何主播！"
    } else {
        // Get streamers' names
        val userIDs = streamers.map { it.userID }
        val names = getBLiveNamesByUIDs(userIDs)
        // Ascend streamers' live IDs
        val liveIDs = streamers.map { it.liveID }.sorted()
        "本群订阅的B站直播：\n" + (liveIDs zip names).joinToString(separator = "\n") {
            "${it.first}\t${it.second}"
        }
    }

    val response = Json.encodeToString(GroupReplyVO(reply))
    call.respondText(response, ContentType.Application.Json, HttpStatusCode.OK)
}

private suspend fun handleSubBLive(call: ApplicationCall, liveID: Long, groupID: Long) {
    call.respondText("")    // No reply

    // Persistent
    val liveData = getBLiveRoomData(liveID)
    upsertBVStreamer(groupID, liveData.uid, liveID)

    // Start pooling
    var oldStatus = 0
    var liveInfo: BLiveInfo
    doInfinity("/10 * * * *") {
        if (bLivePooling) {
            liveInfo = getBLiveRoomData(liveID)
            if (oldStatus == 0 && liveInfo.liveStatus == 1) {
                val (cover, username) = getBLiveDataByUID(liveInfo.uid)
                sendGroupMessage(groupID, "[CQ:image,file=$cover]\n主播 $username 开播啦！\nhttps://live.bilibili.com/$liveID")
            }
            oldStatus = liveInfo.liveStatus
        }
    }
}

private suspend fun handleUnsubBLive(call: ApplicationCall, liveID: Long, groupID: Long) {
    call.respondText("")    // No reply

    // Delete from database
    deleteBVStreamer(groupID, liveID)

    // Disable pooling
    bLivePooling = false
}

package shzh.me.commands

import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.doInfinity
import dev.inmo.krontab.utils.asFlow
import dev.inmo.krontab.utils.asTzFlow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.subscribe
import kotlinx.coroutines.flow.takeWhile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import shzh.me.model.bo.BLiveInfo
import shzh.me.model.vo.GroupReplyVO
import shzh.me.services.*

var bLivePooling = true

suspend fun handleBvInfo(call: ApplicationCall, command: String) {
    val regex = Regex("https://www\\.bilibili\\.com/video/BV(\\w{10})")
    val match = regex.find(command)!!
    val bv = match.groupValues[1]
    val info = getVideoInfo(bv)

    val res = Json.encodeToString(GroupReplyVO(info.toString()))
    call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
}

suspend fun handleBLive(call: ApplicationCall, command: String, groupID: Long) {
    val bLiveCmd = command.substringAfter(' ')
    val (op, liveID) = bLiveCmd.split(' ')

    when (op) {
        "subscribe" -> handleBLiveSub(call, liveID, groupID)
        "unsubscribe" -> handleBLiveUnsub(liveID, groupID)
    }
}

private suspend fun handleBLiveSub(call: ApplicationCall, liveID: String, groupID: Long) {
    call.respondText("")    // No reply

    // Persistent
    subscribeBVStreamer(liveID.toLong(), groupID)
    // Pooling
    var oldStatus = 0
    var liveInfo: BLiveInfo
    doInfinity("/10 * * * *") {
        if (bLivePooling) {
            liveInfo = getBLiveInfo(liveID)
            if (oldStatus == 0 && liveInfo.liveStatus == 1) {
                val (cover, username) = getBLiveCoverByUID(liveInfo.uid)
                sendGroupMessage(groupID, "[CQ:image,file=$cover]\n主播 $username 开播啦！")
            }
            oldStatus = liveInfo.liveStatus
        }
    }
}

private fun handleBLiveUnsub(liveID: String, groupID: Long) {
    // delete from database
    unsubscribeBVStreamer(liveID.toLong(), groupID)
    // Disable pooling
    bLivePooling = false
}

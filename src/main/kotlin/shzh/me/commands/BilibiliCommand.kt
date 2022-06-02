package shzh.me.commands

import dev.inmo.krontab.doInfinity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import shzh.me.model.bo.BLiveInfo
import shzh.me.model.vo.GroupReplyVO
import shzh.me.services.getBLiveInfo
import shzh.me.services.getVideoInfo
import shzh.me.services.sendGroupMessage

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
        "unsubscribe" -> handleBLiveUnsub(call, liveID, groupID)
    }
}

private suspend fun handleBLiveSub(call: ApplicationCall, liveID: String, groupID: Long) {
    call.respondText("")    // No reply
    var oldStatus = 0
    var liveInfo: BLiveInfo

    doInfinity("/10 * * * *") {
        if (bLivePooling) {
            liveInfo = getBLiveInfo(liveID)
            if (oldStatus == 0 && liveInfo.liveStatus == 1) {
                sendGroupMessage(groupID, "开播啦！")
            }
            oldStatus = liveInfo.liveStatus   
        }
    }
}

private suspend fun handleBLiveUnsub(call: ApplicationCall, liveID: String, groupID: Long) {
    bLivePooling = false
}

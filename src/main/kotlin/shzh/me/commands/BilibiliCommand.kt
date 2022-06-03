package shzh.me.commands

import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.utils.asFlow
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.takeWhile
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import shzh.me.model.vo.GroupReplyVO
import shzh.me.services.*
import shzh.me.utils.MessageBuilder

var channelsMap = HashMap<Pair<Long, Long>, Channel<Int>>()

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
        "subscribe" -> handleSubBLive(call, groupID, liveID)
        // /blive unsubscribe <live_id>
        "unsubscribe" -> handleUnsubBLive(call, groupID, liveID)
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

private suspend fun handleSubBLive(call: ApplicationCall, groupID: Long, liveID: Long) {
    call.respondText("")    // No reply

    // Persistent
    val liveData = getBLiveRoomData(liveID)
    upsertBVStreamer(groupID, liveData.uid, liveID)

    // Start pooling
    val channel = Channel<Int>()
    val key = Pair(groupID, liveID)
    if (!channelsMap.containsKey(key)) {
        channelsMap[key] = channel
        poolingLiveRoom(groupID, liveID, 0, channel)
    }
}

private suspend fun handleUnsubBLive(call: ApplicationCall, groupID: Long, liveID: Long) {
    call.respondText("")    // No reply

    // Delete from database
    deleteBVStreamer(groupID, liveID)

    // End pooling
    val key = Pair(groupID, liveID)
    val channel = channelsMap[key]
    if (channel != null) {
        channel.send(0)
        channel.close()
        channelsMap.remove(key)
    }
}

suspend fun poolingLiveRoom(groupID: Long, liveID: Long, oldStatusParam: Int, channel: Channel<Int>) {
    val scheduler = buildSchedule { seconds { 0 every 10 } }
    val flow = scheduler.asFlow()

    var oldStatus = oldStatusParam
    // Report when subscribing a streaming user
    flow.takeWhile {
        // Take until channel send token
        !channel.tryReceive().isSuccess
    }.collect {
        val liveData = getBLiveRoomData(liveID)
        // Edge trigger: from not living to living
        if (oldStatus == 0 && liveData.liveStatus == 1) {
            val (cover, username) = getBLiveDataByUID(liveData.uid)
            val message = MessageBuilder
                .image(cover)
                .text("主播 $username 开播啦！")
                .text("https://live.bilibili.com/$liveID", newline = false)
                .content()
            sendGroupMessage(groupID, message)
        }
        oldStatus = liveData.liveStatus
    }
}

suspend fun recoverPoolingJobs() {
    val streamers = getBLiveAllSteamers()
    streamers.forEach {
        // Create channel and put it into hash map
        val channel = Channel<Int>()
        channelsMap[Pair(it.groupID, it.liveID)] = channel
        // Get current live status as oldStatus
        val status = getBLiveRoomData(it.liveID).liveStatus
        // Start Pooling
        poolingLiveRoom(it.groupID, it.liveID, status, channel)
    }
}

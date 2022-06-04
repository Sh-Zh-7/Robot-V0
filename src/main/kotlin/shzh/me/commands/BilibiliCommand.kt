package shzh.me.commands

import dev.inmo.krontab.builder.buildSchedule
import dev.inmo.krontab.utils.asFlow
import io.ktor.server.application.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.takeWhile
import shzh.me.services.*
import shzh.me.utils.MessageUtils
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach
import kotlin.collections.joinToString
import kotlin.collections.map
import kotlin.collections.set
import kotlin.collections.sorted
import kotlin.collections.zip

val bUsersChannels = HashMap<Pair<Long, Long>, Channel<Int>>()
var bliveChannels = HashMap<Pair<Long, Long>, Channel<Int>>()

suspend fun handleBvInfo(call: ApplicationCall, command: String) {
    val regex = Regex("https://www\\.bilibili\\.com/video/BV(\\w{10})")
    val bv = regex.find(command)!!.groupValues[1]
    val data = getBVData(bv)

    replyMessage(call, data.toString())
}

suspend fun handleBLive(call: ApplicationCall, command: String, groupID: Long, messageID: Int) {
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
        "subscribe" -> handleSubBLive(call, groupID, liveID, messageID)
        // /blive unsubscribe <live_id>
        "unsubscribe" -> handleUnsubBLive(call, groupID, liveID, messageID)
    }
}

suspend fun handleBDyn(call: ApplicationCall, command: String, groupID: Long, messageID: Int) {
    val bDynCmd = command.substringAfter(' ')

    // /bili list
    if (bDynCmd == "list") {
        handleBDynList(call, groupID)
        return
    }

    val (op, userIDStr) = bDynCmd.split(' ')
    val userID = userIDStr.toLong()
    when (op) {
        // /bili subscribe <user_id>
        "subscribe" -> handleSubBDyn(call, groupID, userID, messageID)
        // /bili unsubscribe <user_id>
        "unsubscribe" -> handleUnsubBDyn(call, groupID, userID, messageID)
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

    replyMessage(call, reply)
}

private suspend fun handleSubBLive(call: ApplicationCall, groupID: Long, liveID: Long, messageID: Int) {
    // Persistent
    val liveData = getBLiveRoomData(liveID)
    upsertBVStreamer(groupID, liveData.uid, liveID)

    // Send back success message
    val (_, username) = getBLiveDataByUID(liveData.uid)
    val reply = MessageUtils
        .builder()
        .reply(messageID)
        .text("成功关注主播 $username", newline = false)
        .content()
    replyMessage(call, reply)

    // Start pooling
    val channel = Channel<Int>()
    val key = Pair(groupID, liveID)
    if (!bliveChannels.containsKey(key)) {
        bliveChannels[key] = channel
        poolingLiveRoom(groupID, liveID, 0, channel)
    }
}

private suspend fun handleUnsubBLive(call: ApplicationCall, groupID: Long, liveID: Long, messageID: Int) {
    // Delete from database
    val streamer = deleteBVStreamer(groupID, liveID)

    // Send back success message when steamer existed
    if (streamer != null) {
        val (_, username) = getBLiveDataByUID(streamer.userID)
        val reply = MessageUtils
            .builder()
            .reply(messageID)
            .text("成功取关主播 $username", newline = false)
            .content()
        replyMessage(call, reply)
    }

    // End pooling
    val key = Pair(groupID, liveID)
    val channel = bliveChannels[key]
    if (channel != null) {
        channel.send(0)
        channel.close()
        bliveChannels.remove(key)
    }
}

private suspend fun handleBDynList(call: ApplicationCall, groupID: Long) {
    val users = getBDynUsersByGID(groupID)

    val reply = if (users.isEmpty()) {
        "本群没有关注B站任何UP主！"
    } else {
        val userIDs = users.map { it.userID }
        val usernames = userIDs.map { getUsernameByUID(it) }
        "本群订阅的B站UP主：\n" + (userIDs zip usernames).joinToString(separator = "\n") {
            "${it.first}\t${it.second}"
        }
    }

    replyMessage(call, reply)
}

private suspend fun handleSubBDyn(call: ApplicationCall, groupID: Long, userID: Long, messageID: Int) {
    // Persistent
    val newest = getNewestPublishTimestamp(userID)
    insertBVUser(groupID, userID, newest)

    // Send back success message
    val username = getUsernameByUID(userID)
    val reply = MessageUtils
        .builder()
        .reply(messageID)
        .text("成功订阅UP主 $username", newline = false)
        .content()
    replyMessage(call, reply)

    // Start pooling
    val channel = Channel<Int>()
    val key = Pair(groupID, userID)
    if (!bUsersChannels.containsKey(key)) {
        bUsersChannels[key] = channel
        poolingDynamic(groupID, userID, newest, channel)
    }
}

private suspend fun handleUnsubBDyn(call: ApplicationCall, groupID: Long, userID: Long, messageID: Int) {
    // Delete from database
    val user = deleteBVUser(groupID, userID)

    // Send back success message when user existed
    if (user != null) {
        val username = getUsernameByUID(userID)
        val reply = MessageUtils
            .builder()
            .reply(messageID)
            .text("成功取消订阅UP主 $username", newline = false)
            .content()
        replyMessage(call, reply)
    }

    // End pooling
    val key = Pair(groupID, userID)
    val channel = bUsersChannels[key]
    if (channel != null) {
        channel.send(0)
        channel.close()
        bUsersChannels.remove(key)
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
            val message = MessageUtils
                .builder()
                .image(cover)
                .text("主播 $username 开播啦！")
                .text("https://live.bilibili.com/$liveID", newline = false)
                .content()
            sendGroupMessage(groupID, message)
        }
        oldStatus = liveData.liveStatus
    }
}

suspend fun poolingDynamic(groupID: Long, userID: Long, lastParam: Long, channel: Channel<Int>) {
    val scheduler = buildSchedule { seconds { 0 every 10 } }
    val flow = scheduler.asFlow()

    var last = lastParam
    flow.takeWhile {
        // Take until channel send token
        !channel.tryReceive().isSuccess
    }.collect {
        val latest = getNewestPublishTimestamp(userID)
        println(latest)
        // Level trigger: latest is newer
        if (latest > last) {
            updateBVUser(groupID, userID, latest)

            val message = MessageUtils
                .builder()
                .text("$userID 有新动态", newline = false)
                .content()
            sendGroupMessage(groupID, message)
        }
        last = latest
    }
}

suspend fun recoverPoolingBLive() {
    val streamers = getBLiveAllSteamers()
    streamers.forEach {
        // Create channel and put it into hash map
        val channel = Channel<Int>()
        bliveChannels[Pair(it.groupID, it.liveID)] = channel
        // Get current live status as oldStatus
        val status = getBLiveRoomData(it.liveID).liveStatus
        // Start Pooling
        poolingLiveRoom(it.groupID, it.liveID, status, channel)
    }
}

suspend fun recoverPoolingBDyn() {
    val users = getAllBDynUsers()
    users.forEach {
        val channel = Channel<Int>()
        bUsersChannels[Pair(it.groupID, it.userID)] = channel
        val latest = getNewestPublishTimestamp(it.userID)
        poolingDynamic(it.groupID, it.userID, latest, channel)
    }
}

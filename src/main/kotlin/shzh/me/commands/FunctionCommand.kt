package shzh.me.commands

import shzh.me.services.deleteMessage
import shzh.me.services.getMessage
import shzh.me.services.sendGroupMessage

// groupID -> (lastMessage, count)
val lastMessages = HashMap<Long, Pair<String, Int>>()

suspend fun handleCallback(message: String, userID: Long) {
    val regex = Regex("\\[CQ:reply,id=(-?\\d+)]\\s*撤回")
    val mid = regex.find(message)!!.groupValues[1]

    val msg = getMessage(mid)
    if (msg.sender.userID == userID) {
        deleteMessage(mid)
    }
}

suspend fun handleRepeat(message: String, groupID: Long) {
    if (!lastMessages.containsKey(groupID)) {
        // First message of group, usually happen when server restart
        lastMessages[groupID] = Pair(message, 1)
        return
    }

    val (lastMessage, count) = lastMessages[groupID]!!
    val value = if (lastMessage == message) {
        // Repeat when the message is already repeat 3 times
        if (count + 1 == 3) {
            sendGroupMessage(groupID, message)
            Pair(lastMessage, 4)
        } else {
            // Same message, increase count
            Pair(lastMessage, count + 1)
        }
    } else {
        // Different message, reset to 1
        Pair(message, 1)
    }
    println(value.second)
    lastMessages[groupID] = value
}
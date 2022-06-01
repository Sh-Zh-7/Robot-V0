package shzh.me.commands

import shzh.me.services.deleteMessage
import shzh.me.services.getMessage

suspend fun handleCallback(message: String, userID: Long) {
    val regex = Regex("\\[CQ:reply,id=(-?\\d+)]\\s*撤回")
    val mid = regex.find(message)!!.groupValues[1]

    val msg = getMessage(mid)
    if (msg.sender.userID == userID) {
        deleteMessage(mid)
    }
}
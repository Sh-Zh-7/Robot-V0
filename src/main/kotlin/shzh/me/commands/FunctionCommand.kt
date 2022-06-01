package shzh.me.commands

import shzh.me.services.deleteMessage

suspend fun handleCallback(message: String) {
    val regex = Regex("\\[CQ:reply,id=(-?\\d+)]\\s*撤回")
    val match = regex.find(message)!!
    deleteMessage(match.groupValues[1])
}
package shzh.me.commands

import shzh.me.services.insertHistoryMessage


fun recordMessage(groupID: Long, userID: Long, nickname: String, message: String) {
    insertHistoryMessage(groupID, userID, nickname, message)
}
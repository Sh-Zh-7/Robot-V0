package shzh.me.commands

import io.ktor.server.application.*
import org.ktorm.dsl.forEachIndexed
import shzh.me.model.dao.GroupMessages.message
import shzh.me.model.dao.GroupMessages.username
import shzh.me.services.getHistoryMessageCount
import shzh.me.services.insertHistoryMessage
import shzh.me.services.replyMessage
import shzh.me.services.searchHistoryMessage
import shzh.me.utils.MessageUtils

suspend fun handleFindHistory(call: ApplicationCall, command: String, groupID: Long, messageID: Int) {
    val historyCmd = command.substringAfter(' ')

    val count = getHistoryMessageCount(groupID, historyCmd)
    val query = searchHistoryMessage(groupID, historyCmd)

    var result = ""
    query.forEachIndexed{ index, row ->
        result += "[${index + 1}] ${row[username]}: ${row[message]}\n"
    }
    val reply = MessageUtils
        .builder()
        .reply(messageID)
        .text("共查询到${count}条数据")
        .text(result, newline = false)
        .text("（如超过5行数据，仅显示5行）", newline = false)
        .content()
    replyMessage(call, reply)
}


fun recordMessage(groupID: Long, userID: Long, nickname: String, message: String) {
    insertHistoryMessage(groupID, userID, nickname, message)
}

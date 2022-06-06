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
    val params = historyCmd.split(' ')

    var text: String? = null
    var userID: Long? = null
    params.forEach { param ->
        val pair = param.split(':')
        if (pair.size == 2) {
            val (key, value) = pair

            when (key) {
                "u" -> userID = value.toLong()
            }
        } else {
            text = param
        }
    }

    val count = getHistoryMessageCount(groupID, userID, text)
    val query = searchHistoryMessage(groupID, userID, text)

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

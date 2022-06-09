package shzh.me.commands

import io.ktor.server.application.*
import org.ktorm.dsl.forEachIndexed
import shzh.me.model.dao.GroupMessages.message
import shzh.me.model.dao.GroupMessages.username
import shzh.me.model.dto.MessageDTO
import shzh.me.services.impl.HistoryServiceImpl
import shzh.me.services.impl.OneBotServiceImpl
import shzh.me.utils.MessageUtils

object HistoryCommand {
    private val onebotService = OneBotServiceImpl()
    private val historyService = HistoryServiceImpl()

    suspend fun handle(call: ApplicationCall, msg: MessageDTO) {
        val historyCmd = msg.message.substringAfter(' ')
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

        val count = historyService.getHistoryMessageCount(msg.groupID, userID, text)
        val query = historyService.searchHistoryMessage(msg.groupID, userID, text)

        var result = ""
        query.forEachIndexed{ index, row ->
            result += "[${index + 1}] ${row[username]}: ${row[message]}\n"
        }
        val reply = MessageUtils
            .builder()
            .reply(msg.messageID)
            .text("共查询到${count}条数据")
            .text(result, newline = false)
            .text("（如超过5行数据，仅显示5行）")
            .content()
        onebotService.replyMessage(call, reply)
    }

    fun recordMessage(message: MessageDTO) {
        historyService.insertHistoryMessage(
            message.groupID,
            message.sender.userID,
            message.sender.nickname,
            message.message
        )
    }
}

package shzh.me.services

import org.ktorm.dsl.*

interface HistoryService {
    fun getHistoryMessageCount(groupID: Long, userID: Long?, text: String?): Int

    fun searchHistoryMessage(groupID: Long, userID: Long?, text: String?): Query

    fun insertHistoryMessage(groupID: Long, userID: Long, username: String, message: String)
}

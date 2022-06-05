package shzh.me.services

import org.ktorm.dsl.*
import org.ktorm.entity.add
import shzh.me.model.dao.GroupMessage
import shzh.me.model.dao.GroupMessages
import shzh.me.model.dao.db
import shzh.me.model.dao.groupMessages

fun getHistoryMessageCount(groupID: Long, message: String): Int {
    return db
        .from(GroupMessages)
        .select(count(GroupMessages.id))
        .where { (GroupMessages.groupID eq groupID) and (GroupMessages.message like "%$message%") }
        .map { row -> row.getInt(1) }[0]
}

fun searchHistoryMessage(groupID: Long, message: String): Query {
    return db
        .from(GroupMessages)
        .select(GroupMessages.username, GroupMessages.message)
        .where { (GroupMessages.groupID eq groupID) and (GroupMessages.message like "%$message%") }
        .limit(5)
}

fun insertHistoryMessage(groupID: Long, userID: Long, username: String, message: String) {
    val entity = GroupMessage {
        this.groupID = groupID
        this.userID = userID
        this.username = username
        this.message = message
    }
    db.groupMessages.add(entity)
}
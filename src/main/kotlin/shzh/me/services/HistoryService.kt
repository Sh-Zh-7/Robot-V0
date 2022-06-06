package shzh.me.services

import org.ktorm.dsl.*
import org.ktorm.entity.add
import org.ktorm.expression.ArgumentExpression
import org.ktorm.expression.BinaryExpression
import org.ktorm.expression.ScalarExpression
import org.ktorm.schema.BooleanSqlType
import shzh.me.model.dao.GroupMessage
import shzh.me.model.dao.GroupMessages
import shzh.me.model.dao.db
import shzh.me.model.dao.groupMessages

fun getHistoryMessageCount(groupID: Long, userID: Long?, text: String?): Int {
    return db
        .from(GroupMessages)
        .select(count(GroupMessages.id))
        .where { generatePredicate(groupID, userID, text) }
        .map { row -> row.getInt(1) }[0]
}

fun searchHistoryMessage(groupID: Long, userID: Long?, text: String?): Query {
    return db
        .from(GroupMessages)
        .select(GroupMessages.username, GroupMessages.message)
        .where { generatePredicate(groupID, userID, text) }
        .orderBy(GroupMessages.id.desc())
        .limit(5)
}

private fun generatePredicate(groupID: Long, userID: Long?, text: String?): BinaryExpression<Boolean> {
    val byUserID = if (userID == null) {
        ArgumentExpression(true, BooleanSqlType)
    } else {
        GroupMessages.userID eq userID
    }
    val byText = if (text == null) {
        ArgumentExpression(true, BooleanSqlType)
    } else {
        GroupMessages.message like "%$text%"
    }

    return (GroupMessages.groupID eq groupID) and byUserID and byText
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
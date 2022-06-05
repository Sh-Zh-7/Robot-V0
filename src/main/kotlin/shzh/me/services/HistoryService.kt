package shzh.me.services

import org.ktorm.entity.add
import shzh.me.model.dao.GroupMessage
import shzh.me.model.dao.db
import shzh.me.model.dao.groupMessages

fun insertHistoryMessage(groupID: Long, userID: Long, username: String, message: String) {
    val entity = GroupMessage {
        this.groupID = groupID
        this.userID = userID
        this.username = username
        this.message = message
    }
    db.groupMessages.add(entity)
}
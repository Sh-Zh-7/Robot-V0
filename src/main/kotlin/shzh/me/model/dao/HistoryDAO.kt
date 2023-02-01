package shzh.me.model.dao

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.text
import org.ktorm.schema.varchar

interface GroupMessage : Entity<GroupMessage> {
    companion object : Entity.Factory<GroupMessage>()

    val id: Long
    var groupID: Long
    var userID: Long
    var username: String
    var message: String
}

object GroupMessages : Table<GroupMessage>("group_messages") {
    val id = long("id").primaryKey().bindTo { it.id }
    val groupID = long("group_id").bindTo { it.groupID }
    val userID = long("user_id").bindTo { it.userID }
    val username = varchar("username").bindTo { it.username }
    val message = text("message").bindTo { it.message }
}

val Database.groupMessages get() = this.sequenceOf(GroupMessages)
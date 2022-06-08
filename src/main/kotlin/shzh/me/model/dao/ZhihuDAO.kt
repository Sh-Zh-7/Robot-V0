package shzh.me.model.dao

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.LocalDate

interface GroupSubZhihu: Entity<GroupSubZhihu> {
    companion object: Entity.Factory<GroupSubZhihu>()
    val id: Int
    var groupID: Long
    var username: String
    var answer: LocalDate?
    var post: LocalDate?
    var pin: LocalDate?
}

object GroupSubZhihus: Table<GroupSubZhihu>("group_subscribed_zhihu") {
    val id = int("id").primaryKey().bindTo { it.id }
    val groupID = long("group_id").bindTo { it.groupID }
    val username = varchar("username").bindTo { it.username }
    val answer = date("answer").bindTo { it.answer }
    val post = date("post").bindTo { it.post }
    val pin = date("pin").bindTo { it.pin }
}

val Database.groupSubZhihus get() = this.sequenceOf(GroupSubZhihus)
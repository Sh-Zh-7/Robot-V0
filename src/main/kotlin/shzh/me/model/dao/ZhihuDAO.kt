package shzh.me.model.dao

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import shzh.me.utils.TimeUtils
import java.util.*

interface GroupSubZhihu: Entity<GroupSubZhihu> {
    companion object: Entity.Factory<GroupSubZhihu>()
    val id: Int
    var groupID: Long
    var username: String
    var answer: Date?
    var post: Date?
    var pin: Date?
}

object GroupSubZhihus: Table<GroupSubZhihu>("group_subscribed_zhihu") {
    val id = int("id").primaryKey().bindTo { it.id }
    val groupID = long("group_id").bindTo { it.groupID }
    val username = varchar("username").bindTo { it.username }
    val answer = date("answer").bindTo { it.answer?.let { date -> TimeUtils.dateToLocalDate(date) } }
    val post = date("post").bindTo { it.post?.let { date -> TimeUtils.dateToLocalDate(date) } }
    val pin = date("pin").bindTo { it.pin?.let { date -> TimeUtils.dateToLocalDate(date) } }
}

val Database.groupSubZhihus get() = this.sequenceOf(GroupSubZhihus)
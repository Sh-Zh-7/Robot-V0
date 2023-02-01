package shzh.me.model.dao

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long

interface GroupSubBVStreamer : Entity<GroupSubBVStreamer> {
    companion object : Entity.Factory<GroupSubBVStreamer>()

    val id: Int
    var groupID: Long
    var userID: Long
    var liveID: Long
}

object GroupSubBVStreamers : Table<GroupSubBVStreamer>("group_subscribed_bv_streamer") {
    val id = int("id").primaryKey().bindTo { it.id }
    val groupID = long("group_id").bindTo { it.groupID }
    val userID = long("user_id").bindTo { it.userID }
    val liveID = long("live_id").bindTo { it.liveID }
}

val Database.groupSubBVStreamers get() = this.sequenceOf(GroupSubBVStreamers)

interface GroupSubBVUser : Entity<GroupSubBVUser> {
    companion object : Entity.Factory<GroupSubBVUser>()

    val id: Int
    var groupID: Long
    var userID: Long
    var published: Long
}

object GroupSubBVUsers : Table<GroupSubBVUser>("group_subscribed_bv_user") {
    val id = int("id").primaryKey().bindTo { it.id }
    val groupID = long("group_id").bindTo { it.groupID }
    val userID = long("user_id").bindTo { it.userID }
    val published = long("published").bindTo { it.published }
}

val Database.groupSubBVUsers get() = this.sequenceOf(GroupSubBVUsers)

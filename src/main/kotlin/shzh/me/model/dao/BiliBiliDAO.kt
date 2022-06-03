package shzh.me.model.dao

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long

val db = Database.connect(
    url = "jdbc:postgresql://localhost:5432/robot",
    user = "admin",
    password = "admin",
)

interface GroupSubBVStreamer: Entity<GroupSubBVStreamer> {
    companion object: Entity.Factory<GroupSubBVStreamer>()

    val id: Int
    var groupID: Long
    var userID: Long
    var liveID: Long
}

object GroupSubBVStreamers: Table<GroupSubBVStreamer>("group_subscribed_bv_streamer") {
    val id = int("id").primaryKey().bindTo { it.id }
    val groupID = long("group_id").bindTo { it.groupID }
    val userID = long("user_id").bindTo { it.userID }
    val liveID = long("live_id").bindTo { it.liveID }
}

val Database.groupSubBVStreamers get() = this.sequenceOf(GroupSubBVStreamers)

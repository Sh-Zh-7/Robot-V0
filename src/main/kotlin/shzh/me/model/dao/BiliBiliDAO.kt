package shzh.me.model.dao

import org.ktorm.database.Database
import org.ktorm.dsl.*
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long

val db = Database.connect("jdbc:postgresql://localhost:5432/robot", user = "admin", password = "admin")

object GroupSubBVStreamer: Table<Nothing>("group_subscribed_bv_streamer") {
    val id = int("id").primaryKey()
    val groupID = long("group_id")
    val liveID = long("live_id")
}

package shzh.me.model.dao

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.Instant

interface GroupSubWeibo: Entity<GroupSubWeibo> {
    companion object: Entity.Factory<GroupSubWeibo>()
    val id: Int
    var groupID: Long
    var weiboID: Long
    var username: String
    var published: Instant?
}

object GroupSubWeibos: Table<GroupSubWeibo>("group_subscribed_weibo") {
    val id = int("id").primaryKey().bindTo { it.id }
    val groupID = long("group_id").bindTo { it.groupID }
    val weiboID = long("weibo_id").bindTo { it.weiboID }
    val username = varchar("username").bindTo { it.username }
    val published = timestamp("published").bindTo { it.published }
}

val Database.groupSubWeibos get() = this.sequenceOf(GroupSubWeibos)
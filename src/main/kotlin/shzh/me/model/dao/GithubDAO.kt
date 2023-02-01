package shzh.me.model.dao

import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import java.time.Instant

interface GroupSubGithub : Entity<GroupSubGithub> {
    companion object : Entity.Factory<GroupSubGithub>()

    val id: Int
    var groupID: Long
    var username: String
    var published: Instant
}

object GroupSubGithubs: Table<GroupSubGithub>("group_subscribed_github") {
    val id = int("id").primaryKey().bindTo { it.id }
    val groupID = long("group_id").bindTo { it.groupID }
    val username = varchar("username").bindTo { it.username }
    val published = timestamp("published").bindTo { it.published }
}

val Database.groupSubGithubs get() = this.sequenceOf(GroupSubGithubs)
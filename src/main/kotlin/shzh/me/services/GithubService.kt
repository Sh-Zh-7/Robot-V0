package shzh.me.services

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import shzh.me.model.dao.*
import java.net.URL
import java.util.*

fun getAllGithubUsers(): List<GroupSubGithub> {
    return db.groupSubGithubs.toList()
}

fun getGithubsByGID(groupID: Long): List<GroupSubGithub> {
    return db.groupSubGithubs
        .filter { it.groupID eq groupID }
        .toList()
}

fun insertGithubUser(groupID: Long, username: String, published: Date) {
    db.groupSubGithubs.find {
        (it.groupID eq groupID) and (it.username eq username)
    } ?: run {
        val entity = GroupSubGithub {
            this.groupID = groupID
            this.username = username
            this.published = java.sql.Date(published.time).toLocalDate()
        }
        db.groupSubGithubs.add(entity)
    }
}

fun updateGithubUser(groupID: Long, username: String, newDate: Date) {
    val entity = db.groupSubGithubs.find {
        (it.groupID eq groupID) and (it.username eq username)
    } ?: return
    entity.published = java.sql.Date(newDate.time).toLocalDate()
    entity.flushChanges()
}

fun deleteGithubUser(groupID: Long, username: String): GroupSubGithub? {
    val entity = db.groupSubGithubs.find {
        (it.groupID eq groupID) and (it.username eq username)
    } ?: return null
    entity.delete()

    return entity
}

fun getLatestDynDate(username: String): Date {
    val url = "https://github.com/$username.atom"
    val feed = SyndFeedInput().build(XmlReader(URL(url)))

    return feed.entries[0].publishedDate!!
}

fun fetchAllDynamics(username: String): MutableList<SyndEntry> {
    val url = "https://github.com/$username.atom"
    val feed = SyndFeedInput().build(XmlReader(URL(url)))

    return feed.entries!!
}
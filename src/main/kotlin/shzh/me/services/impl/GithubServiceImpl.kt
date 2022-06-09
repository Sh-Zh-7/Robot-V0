package shzh.me.services.impl

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import shzh.me.db
import shzh.me.model.dao.GroupSubGithub
import shzh.me.model.dao.groupSubGithubs
import shzh.me.services.GithubService
import java.net.URL
import java.util.*

class GithubServiceImpl: GithubService {
    override fun getAllGithubUsers(): List<GroupSubGithub> {
        return db.groupSubGithubs.toList()
    }

    override fun getGithubsByGID(groupID: Long): List<GroupSubGithub> {
        return db.groupSubGithubs
            .filter { it.groupID eq groupID }
            .toList()
    }

    override fun insertGithubUser(groupID: Long, username: String, published: Date) {
        db.groupSubGithubs.find {
            (it.groupID eq groupID) and (it.username eq username)
        } ?: run {
            val entity = GroupSubGithub {
                this.groupID = groupID
                this.username = username
                this.published = published
            }
            db.groupSubGithubs.add(entity)
        }
    }

    override fun updateGithubUser(groupID: Long, username: String, newDate: Date) {
        val entity = db.groupSubGithubs.find {
            (it.groupID eq groupID) and (it.username eq username)
        } ?: return
        entity.published = newDate
        entity.flushChanges()
    }

    override fun deleteGithubUser(groupID: Long, username: String): GroupSubGithub? {
        val entity = db.groupSubGithubs.find {
            (it.groupID eq groupID) and (it.username eq username)
        } ?: return null
        entity.delete()

        return entity
    }

    override fun getLatestDynDate(username: String): Date {
        val url = "https://github.com/$username.atom"
        val feed = SyndFeedInput().build(XmlReader(URL(url)))

        return feed.entries[0].publishedDate!!
    }

    override fun fetchAllDynamics(username: String): MutableList<SyndEntry> {
        val url = "https://github.com/$username.atom"
        val feed = SyndFeedInput().build(XmlReader(URL(url)))

        return feed.entries!!
    }
}
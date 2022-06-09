package shzh.me.services.impl

import com.rometools.rome.feed.synd.SyndEntry
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import shzh.me.db
import shzh.me.model.dao.GroupSubZhihu
import shzh.me.model.dao.groupSubZhihus
import shzh.me.services.ZhihuService
import shzh.me.services.ZhihuStatus
import shzh.me.utils.RssUtils
import java.io.FileNotFoundException
import java.util.*

class ZhihuServiceImpl: ZhihuService {
    override fun getAllZhihuUsers(): List<GroupSubZhihu> {
        return db.groupSubZhihus.toList()
    }

    override fun getZhihuUsersByGID(groupID: Long): List<GroupSubZhihu> {
        return db.groupSubZhihus
            .filter { it.groupID eq groupID }
            .toList()
    }

    override fun insertZhihuUser(groupID: Long, username: String, answer: Date?, post: Date?, pin: Date?) {
        db.groupSubZhihus.find {
            (it.groupID eq groupID) and (it.username eq username)
        } ?: run {
            val entity = GroupSubZhihu {
                this.groupID = groupID
                this.username = username
                this.answer = answer
                this.post = post
                this.pin = pin
            }
            db.groupSubZhihus.add(entity)
        }
    }

    override fun updateZhihuAnswerDate(groupID: Long, username: String, answer: Date) {
        val entity = db.groupSubZhihus.find {
            (it.groupID eq groupID) and (it.username eq username)
        } ?: return
        entity.flushChanges()
    }

    override fun updateZhihuPostDate(groupID: Long, username: String, post: Date) {
        val entity = db.groupSubZhihus.find {
            (it.groupID eq groupID) and (it.username eq username)
        } ?: return
        entity.flushChanges()
    }

    override fun updateZhihuPinDate(groupID: Long, username: String, pin: Date) {
        val entity = db.groupSubZhihus.find {
            (it.groupID eq groupID) and (it.username eq username)
        } ?: return
        entity.flushChanges()
    }

    override fun deleteZhihuUser(groupID: Long, username: String): GroupSubZhihu? {
        val entity = db.groupSubZhihus.find {
            (it.groupID eq groupID) and (it.username eq username)
        } ?: return null
        entity.delete()

        return entity
    }

    override fun getZhihuLatestAnswerDate(username: String): SyndEntry? {
        return try {
            val url = "http://localhost:1200/zhihu/people/answers/$username"
            RssUtils.fetchLatestEntry(url)
        } catch (e: FileNotFoundException) {
            null
        }
    }

    override fun getZhihuLatestPostDate(username: String): SyndEntry? {
        return try {
            val url = "http://localhost:1200/zhihu/posts/people/$username"
            RssUtils.fetchLatestEntry(url)
        } catch (e: FileNotFoundException) {
            null
        }
    }

    override fun getZhihuLatestPinDate(username: String): SyndEntry? {
        return try {
            val url = "http://localhost:1200/zhihu/people/pins/$username"
            RssUtils.fetchLatestEntry(url)
        } catch (e: FileNotFoundException) {
            null
        }
    }

    override fun getZhihuLatestDate(username: String): ZhihuStatus {
        val answer = getZhihuLatestAnswerDate(username)?.publishedDate
        val post = getZhihuLatestPostDate(username)?.publishedDate
        val pin = getZhihuLatestPinDate(username)?.publishedDate

        return ZhihuStatus(answer, post, pin)
    }
}
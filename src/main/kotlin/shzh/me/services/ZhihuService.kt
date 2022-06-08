package shzh.me.services

import com.rometools.rome.feed.synd.SyndEntry
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import shzh.me.model.dao.GroupSubZhihu
import shzh.me.model.dao.db
import shzh.me.model.dao.groupSubZhihus
import shzh.me.utils.dateToLocalDate
import java.io.FileNotFoundException
import java.util.Date

class ZhihuStatus(
    var answer: Date?,
    var post: Date?,
    var pin: Date?,
)

fun getAllZhihuUsers(): List<GroupSubZhihu> {
    return db.groupSubZhihus.toList()
}

fun getZhihuUsersByGID(groupID: Long): List<GroupSubZhihu> {
    return db.groupSubZhihus
        .filter { it.groupID eq groupID }
        .toList()
}

fun insertZhihuUser(groupID: Long, username: String, answer: Date?, post: Date?, pin: Date?) {
    db.groupSubZhihus.find {
        (it.groupID eq groupID) and (it.username eq username)
    } ?: run {
        val entity = GroupSubZhihu {
            this.groupID = groupID
            this.username = username
            this.answer = answer?.let { dateToLocalDate(it) }
            this.post = post?.let { dateToLocalDate(it) }
            this.pin = pin?.let { dateToLocalDate(it) }
        }
        db.groupSubZhihus.add(entity)
    }
}

fun updateZhihuAnswerDate(groupID: Long, username: String, answer: Date) {
    val entity = db.groupSubZhihus.find {
        (it.groupID eq groupID) and (it.username eq username)
    } ?: return
    entity.answer = dateToLocalDate(answer)
    entity.flushChanges()
}

fun updateZhihuPostDate(groupID: Long, username: String, post: Date) {
    val entity = db.groupSubZhihus.find {
        (it.groupID eq groupID) and (it.username eq username)
    } ?: return
    entity.post = dateToLocalDate(post)
    entity.flushChanges()
}

fun updateZhihuPinDate(groupID: Long, username: String, pin: Date) {
    val entity = db.groupSubZhihus.find {
        (it.groupID eq groupID) and (it.username eq username)
    } ?: return
    entity.pin = dateToLocalDate(pin)
    entity.flushChanges()
}

fun deleteZhihuUser(groupID: Long, username: String): GroupSubZhihu? {
    val entity = db.groupSubZhihus.find {
        (it.groupID eq groupID) and (it.username eq username)
    } ?: return null
    entity.delete()

    return entity
}

fun getZhihuLatestAnswerDate(username: String): SyndEntry? {
    return try {
        val url = "http://localhost:1200/zhihu/people/answers/$username"
        fetchLatestEntry(url)
    } catch (e: FileNotFoundException) {
        null
    }
}

fun getZhihuLatestPostDate(username: String): SyndEntry? {
    return try {
        val url = "http://localhost:1200/zhihu/posts/people/$username"
        fetchLatestEntry(url)
    } catch (e: FileNotFoundException) {
        null
    }
}

fun getZhihuLatestPinDate(username: String): SyndEntry? {
    return try {
        val url = "http://localhost:1200/zhihu/people/pins/$username"
        fetchLatestEntry(url)
    } catch (e: FileNotFoundException) {
        null
    }
}

fun getZhihuLatestDate(username: String): ZhihuStatus {
    val answer = getZhihuLatestAnswerDate(username)?.publishedDate
    val post = getZhihuLatestPostDate(username)?.publishedDate
    val pin = getZhihuLatestPinDate(username)?.publishedDate

    return ZhihuStatus(answer, post, pin)
}

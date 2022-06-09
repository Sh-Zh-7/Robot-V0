package shzh.me.services

import com.rometools.rome.feed.synd.SyndEntry
import org.ktorm.entity.toList
import shzh.me.db
import shzh.me.model.dao.GroupSubZhihu
import shzh.me.model.dao.groupSubZhihus
import java.util.*

data class ZhihuStatus(
    var answer: Date?,
    var post: Date?,
    var pin: Date?,
)

interface ZhihuService {
    fun getAllZhihuUsers(): List<GroupSubZhihu> {
        return db.groupSubZhihus.toList()
    }

    fun getZhihuUsersByGID(groupID: Long): List<GroupSubZhihu>

    fun insertZhihuUser(groupID: Long, username: String, answer: Date?, post: Date?, pin: Date?)

    fun updateZhihuAnswerDate(groupID: Long, username: String, answer: Date)

    fun updateZhihuPostDate(groupID: Long, username: String, post: Date)

    fun updateZhihuPinDate(groupID: Long, username: String, pin: Date)

    fun deleteZhihuUser(groupID: Long, username: String): GroupSubZhihu?

    fun getZhihuLatestAnswerDate(username: String): SyndEntry?

    fun getZhihuLatestPostDate(username: String): SyndEntry?

    fun getZhihuLatestPinDate(username: String): SyndEntry?

    fun getZhihuLatestDate(username: String): ZhihuStatus
}

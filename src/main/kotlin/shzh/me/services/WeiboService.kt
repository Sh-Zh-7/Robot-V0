package shzh.me.services

import com.rometools.rome.feed.synd.SyndEntry
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import shzh.me.model.dao.GroupSubWeibo
import shzh.me.model.dao.db
import shzh.me.model.dao.groupSubWeibos
import java.util.*

fun getAllWeiboUsers(): List<GroupSubWeibo> {
    return db.groupSubWeibos.toList()
}

fun getWeibosByGID(groupID: Long): List<GroupSubWeibo> {
    return db.groupSubWeibos
        .filter { it.groupID eq groupID }
        .toList()
}

fun insertWeiboUser(groupID: Long, weiboID: Long, username: String, published: Date) {
    db.groupSubWeibos.find {
        (it.groupID eq groupID) and (it.weiboID eq weiboID)
    } ?: run {
        val entity = GroupSubWeibo {
            this.groupID = groupID
            this.weiboID = weiboID
            this.username = username
            this.published = java.sql.Date(published.time).toLocalDate()
        }
        db.groupSubWeibos.add(entity)
    }
}

fun updateWeiboUser(groupID: Long, weiboID: Long, newDate: Date) {
    val entity = db.groupSubWeibos.find {
        (it.groupID eq groupID) and (it.weiboID eq weiboID)
    } ?: return
    entity.published = java.sql.Date(newDate.time).toLocalDate()
    entity.flushChanges()
}

fun deleteWeiboUser(groupID: Long, weiboID: Long): GroupSubWeibo? {
    val entity = db.groupSubWeibos.find {
        (it.groupID eq groupID) and (it.weiboID eq weiboID)
    } ?: return null
    entity.delete()

    return entity
}

fun getUsernameByWeiboID(weiboID: Long): String {
    val url = "http://localhost:1200/weibo/user/$weiboID"
    val title = fetchTitle(url)

    return title.substringBefore("的微博")
}

fun getLatestWeiboByWeiboID(weiboID: Long): SyndEntry {
    val url = "http://localhost:1200/weibo/user/$weiboID"

    return fetchLatestEntry(url)
}
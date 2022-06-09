package shzh.me.services.impl

import com.rometools.rome.feed.synd.SyndEntry
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import shzh.me.db
import shzh.me.model.dao.GroupSubWeibo
import shzh.me.model.dao.groupSubWeibos
import shzh.me.services.WeiboService
import shzh.me.utils.RssUtils
import java.util.*

class WeiboServiceImpl: WeiboService {
    override fun getAllWeiboUsers(): List<GroupSubWeibo> {
        return db.groupSubWeibos.toList()
    }

    override fun getWeibosByGID(groupID: Long): List<GroupSubWeibo> {
        return db.groupSubWeibos
            .filter { it.groupID eq groupID }
            .toList()
    }

    override fun insertWeiboUser(groupID: Long, weiboID: Long, username: String, published: Date) {
        db.groupSubWeibos.find {
            (it.groupID eq groupID) and (it.weiboID eq weiboID)
        } ?: run {
            val entity = GroupSubWeibo {
                this.groupID = groupID
                this.weiboID = weiboID
                this.username = username
                this.published = published
            }
            db.groupSubWeibos.add(entity)
        }
    }

    override fun updateWeiboUser(groupID: Long, weiboID: Long, newDate: Date) {
        val entity = db.groupSubWeibos.find {
            (it.groupID eq groupID) and (it.weiboID eq weiboID)
        } ?: return
        entity.published = newDate
        entity.flushChanges()
    }

    override fun deleteWeiboUser(groupID: Long, weiboID: Long): GroupSubWeibo? {
        val entity = db.groupSubWeibos.find {
            (it.groupID eq groupID) and (it.weiboID eq weiboID)
        } ?: return null
        entity.delete()

        return entity
    }

    override fun getUsernameByWeiboID(weiboID: Long): String {
        val url = "http://localhost:1200/weibo/user/$weiboID"
        val title = RssUtils.fetchTitle(url)

        return title.substringBefore("的微博")
    }

    override fun getLatestWeiboByWeiboID(weiboID: Long): SyndEntry {
        val url = "http://localhost:1200/weibo/user/$weiboID"

        return RssUtils.fetchLatestEntry(url)
    }
}
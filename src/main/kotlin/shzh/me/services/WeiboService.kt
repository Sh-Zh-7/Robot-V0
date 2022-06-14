package shzh.me.services

import com.rometools.rome.feed.synd.SyndEntry
import shzh.me.model.dao.GroupSubWeibo
import java.util.*

interface WeiboService {
    fun getAllWeiboUsers(): List<GroupSubWeibo>

    fun getWeibosByGID(groupID: Long): List<GroupSubWeibo>

    fun insertWeiboUser(groupID: Long, weiboID: Long, username: String, published: Date?)

    fun updateWeiboUser(groupID: Long, weiboID: Long, newDate: Date)

    fun deleteWeiboUser(groupID: Long, weiboID: Long): GroupSubWeibo?

    fun getUsernameByWeiboID(weiboID: Long): String

    fun getLatestWeiboByWeiboID(weiboID: Long): SyndEntry?
}

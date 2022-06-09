package shzh.me.services

import com.rometools.rome.feed.synd.SyndEntry
import shzh.me.model.dao.GroupSubGithub
import java.util.*

interface GithubService {
    fun getAllGithubUsers(): List<GroupSubGithub>

    fun getGithubsByGID(groupID: Long): List<GroupSubGithub>

    fun insertGithubUser(groupID: Long, username: String, published: Date)

    fun updateGithubUser(groupID: Long, username: String, newDate: Date)

    fun deleteGithubUser(groupID: Long, username: String): GroupSubGithub?

    fun getLatestDynDate(username: String): Date

    fun fetchAllDynamics(username: String): MutableList<SyndEntry>
}

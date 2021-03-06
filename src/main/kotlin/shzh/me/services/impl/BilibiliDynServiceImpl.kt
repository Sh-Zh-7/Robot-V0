package shzh.me.services.impl

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.request.*
import kotlinx.serialization.json.*
import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import shzh.me.db
import shzh.me.model.dao.GroupSubBVUser
import shzh.me.model.dao.groupSubBVUsers
import shzh.me.services.BilibiliDynService

class BilibiliDynServiceImpl: BilibiliDynService {
    private val client = HttpClient(CIO) {
        defaultRequest { url { host = "api.vc.bilibili.com" } }
    }

    override suspend fun getNewestPublishTimestamp(userID: Long): Pair<Long, String> {
        val response = client.get("dynamic_svr/v1/dynamic_svr/space_history") {
            url {
                parameters.append("host_uid", userID.toString())
                parameters.append("platform", "web")
            }
        }.body<String>()

        val desc = Json.parseToJsonElement(response)
            .jsonObject["data"]!!
            .jsonObject["cards"]!!
            .jsonArray[0]
            .jsonObject["desc"]!!

        val timestamp = desc.jsonObject["timestamp"]!!.jsonPrimitive.long
        val dynamicID = desc.jsonObject["dynamic_id"]!!.jsonPrimitive.long.toString()

        return Pair(timestamp, dynamicID)
    }

    override fun getAllBDynUsers(): List<GroupSubBVUser> {
        return db.groupSubBVUsers.toList()
    }

    override fun getBDynUsersByGID(groupID: Long): List<GroupSubBVUser> {
        return db.groupSubBVUsers
            .filter { it.groupID eq groupID }
            .toList()
    }

    override fun insertBVUser(groupID: Long, userID: Long, published: Long) {
        db.groupSubBVUsers.find {
            (it.groupID eq groupID) and (it.userID eq userID)
        } ?: run {
            val entity = GroupSubBVUser {
                this.groupID = groupID
                this.userID = userID
                this.published = published
            }
            db.groupSubBVUsers.add(entity)
        }
    }

    override fun updateBVUser(groupID: Long, userID: Long, newDate: Long) {
        val entity = db.groupSubBVUsers.find {
            (it.groupID eq groupID) and (it.userID eq userID)
        } ?: return
        entity.published = newDate
        entity.flushChanges()
    }

    override fun deleteBVUser(groupID: Long, userID: Long): GroupSubBVUser? {
        val entity = db.groupSubBVUsers.find {
            (it.groupID eq groupID) and (it.userID eq userID)
        } ?: return null
        entity.delete()

        return entity
    }
}
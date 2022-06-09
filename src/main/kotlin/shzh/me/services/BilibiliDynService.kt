package shzh.me.services

import shzh.me.model.dao.GroupSubBVUser

interface BilibiliDynService {
    suspend fun getNewestPublishTimestamp(userID: Long): Pair<Long, String>

    fun getAllBDynUsers(): List<GroupSubBVUser>

    fun getBDynUsersByGID(groupID: Long): List<GroupSubBVUser>

    fun insertBVUser(groupID: Long, userID: Long, published: Long)

    fun updateBVUser(groupID: Long, userID: Long, newDate: Long)

    fun deleteBVUser(groupID: Long, userID: Long): GroupSubBVUser?
}

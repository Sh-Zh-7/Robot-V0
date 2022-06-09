package shzh.me.services

import shzh.me.model.bo.BLiveInfo
import shzh.me.model.bo.BvData

interface BilibiliApiService {
    suspend fun getBVData(bv: String): BvData

    suspend fun getBLiveRoomData(liveID: Long): BLiveInfo

    suspend fun getBLiveNamesByUIDs(userIDs: List<Long>): List<String>

    suspend fun getBLiveDataByUID(userID: Long): Pair<String, String>

    suspend fun getUsernameByUID(userID: Long): String
}

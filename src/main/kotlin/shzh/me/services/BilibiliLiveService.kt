package shzh.me.services

import shzh.me.model.dao.GroupSubBVStreamer

interface BilibiliLiveService {
    fun getBLiveAllSteamers(): List<GroupSubBVStreamer>

    fun getBLiveSteamersByGID(groupID: Long): List<GroupSubBVStreamer>

    fun upsertBVStreamer(groupID: Long, userID: Long, liveID: Long)

    fun deleteBVStreamer(groupID: Long, liveID: Long): GroupSubBVStreamer?
}

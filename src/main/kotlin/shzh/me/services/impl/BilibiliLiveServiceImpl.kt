package shzh.me.services.impl

import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import shzh.me.db
import shzh.me.model.dao.GroupSubBVStreamer
import shzh.me.model.dao.groupSubBVStreamers
import shzh.me.services.BilibiliLiveService

class BilibiliLiveServiceImpl: BilibiliLiveService {
    override fun getBLiveAllSteamers(): List<GroupSubBVStreamer> {
        return db.groupSubBVStreamers.toList()
    }

    override fun getBLiveSteamersByGID(groupID: Long): List<GroupSubBVStreamer> {
        return db.groupSubBVStreamers
            .filter { it.groupID eq groupID }
            .toList()
    }

    override fun upsertBVStreamer(groupID: Long, userID: Long, liveID: Long) {
        db.groupSubBVStreamers.find {
            (it.groupID eq groupID) and (it.liveID eq liveID)
        } ?: run {
            val entity = GroupSubBVStreamer {
                this.groupID = groupID
                this.userID = userID
                this.liveID = liveID
            }
            db.groupSubBVStreamers.add(entity)
        }
    }

    override fun deleteBVStreamer(groupID: Long, liveID: Long): GroupSubBVStreamer? {
        val entity = db.groupSubBVStreamers.find {
            (it.groupID eq groupID) and (it.liveID eq liveID)
        } ?: return null
        entity.delete()

        return entity
    }
}
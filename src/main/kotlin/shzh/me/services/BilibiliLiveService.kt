package shzh.me.services

import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.entity.add
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import shzh.me.model.dao.GroupSubBVStreamer
import shzh.me.model.dao.db
import shzh.me.model.dao.groupSubBVStreamers


fun getBLiveAllSteamers(): List<GroupSubBVStreamer> {
    return db.groupSubBVStreamers.toList()
}

fun getBLiveSteamersByGID(groupID: Long): List<GroupSubBVStreamer> {
    return db.groupSubBVStreamers
        .filter { it.groupID eq groupID }
        .toList()
}

fun upsertBVStreamer(groupID: Long, userID: Long, liveID: Long) {
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

fun deleteBVStreamer(groupID: Long, liveID: Long): GroupSubBVStreamer? {
    val entity = db.groupSubBVStreamers.find {
        (it.groupID eq groupID) and (it.liveID eq liveID)
    } ?: return null
    entity.delete()

    return entity
}
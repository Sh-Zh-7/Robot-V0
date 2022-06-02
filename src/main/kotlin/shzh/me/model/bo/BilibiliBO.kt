package shzh.me.model.bo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BvData(
    val pic: String,
    val bvid: String,
    val aid: Long,
    val title: String,
    val desc: String,
    val owner: BvOwner
) {
    override fun toString(): String = """[CQ:image,file=$pic]
        |传送门：bilibili.com/video/$bvid
        |BV号：$bvid
        |AV号：$aid
        |标题：$title
        |简介：$desc
        |UP主：$owner""".trimMargin()
}

@Serializable
data class BvOwner(val name: String) {
    override fun toString(): String = name
}

@Serializable
data class BLiveInfo(
    @SerialName("live_status")
    val liveStatus: Int,
)

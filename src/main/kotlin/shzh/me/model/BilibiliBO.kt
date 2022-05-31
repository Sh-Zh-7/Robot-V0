package shzh.me.model

import kotlinx.serialization.Serializable

@Serializable
data class BvInfo(val data: BvData) {
    override fun toString(): String = data.toString()
}

@Serializable
data class BvData(
    val bvid: String,
    val aid: Long,
    val title: String,
    val desc: String,
    val owner: BvOwner
) {
    override fun toString(): String = """传送门：bilibili.com/av$aid
        |BV号：$bvid
        |AV号：$aid
        |标题：$title
        |简介：$desc
        |UP主：$owner""".trimMargin().replace("\n", "\\n")
}

@Serializable
data class BvOwner(val name: String) {
    override fun toString(): String = name
}

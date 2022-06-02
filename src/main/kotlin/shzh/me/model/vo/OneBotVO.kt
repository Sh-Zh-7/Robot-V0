package shzh.me.model.vo

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GroupReplyVO(val reply: String)

@Serializable
data class GroupMessageVo(
    @SerialName("group_id")
    val groupID: Long,

    val message: String,
)

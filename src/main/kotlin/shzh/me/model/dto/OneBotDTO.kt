package shzh.me.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDTO(
    val message: String,

    @SerialName("message_id")
    val messageID: Int,

    @SerialName("group_id")
    val groupID: Long,

    val sender: Sender,
)

@Serializable
data class Sender(
    val nickname: String,

    @SerialName("user_id")
    val userID: Long,
)

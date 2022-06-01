package shzh.me.model.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MessageDTO(
    val message: String,

    @SerialName("message_id")
    val messageID: Int,
)

package shzh.me.model

import kotlinx.serialization.Serializable

@Serializable
data class DataWrapper<T>(
    val data: T
)
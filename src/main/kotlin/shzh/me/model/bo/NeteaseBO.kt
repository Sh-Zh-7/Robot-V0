package shzh.me.model.bo

import kotlinx.serialization.Serializable

@Serializable
data class Songs(
    var songs: List<Song>
)

@Serializable
data class Song(
    var id: Long
)

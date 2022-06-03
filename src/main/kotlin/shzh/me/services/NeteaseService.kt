package shzh.me.services

import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import shzh.me.model.ResultWrapper
import shzh.me.model.bo.Song
import shzh.me.model.bo.Songs

suspend fun searchMusicByKeyword(keyword: String): Song {
    val response = client.get("http://127.0.0.1:3000/search") {
        url { parameters.append("keywords", keyword) }
    }.body<String>()

    val songs = format.decodeFromString<ResultWrapper<Songs>>(response).result.songs
    return songs[0]
}
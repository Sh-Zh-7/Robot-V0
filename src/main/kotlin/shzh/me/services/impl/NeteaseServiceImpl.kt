package shzh.me.services.impl

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import kotlinx.serialization.decodeFromString
import shzh.me.format
import shzh.me.model.ResultWrapper
import shzh.me.model.bo.Song
import shzh.me.model.bo.Songs
import shzh.me.services.NeteaseService

class NeteaseServiceImpl: NeteaseService {
    private val client = HttpClient(CIO)

    override suspend fun searchMusicByKeyword(keyword: String): Song {
        val response = client.get("http://127.0.0.1:3000/search") {
            url { parameters.append("keywords", keyword) }
        }.body<String>()

        val songs = format.decodeFromString<ResultWrapper<Songs>>(response).result.songs
        return songs[0]
    }
}
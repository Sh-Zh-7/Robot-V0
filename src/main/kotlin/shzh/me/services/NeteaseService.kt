package shzh.me.services

import shzh.me.model.bo.Song

interface NeteaseService {
    suspend fun searchMusicByKeyword(keyword: String): Song
}

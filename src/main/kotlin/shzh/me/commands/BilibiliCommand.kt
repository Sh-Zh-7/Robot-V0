package shzh.me.commands

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import shzh.me.model.vo.GroupReplyVO
import shzh.me.services.getVideoInfo

suspend fun handleBvInfo(call: ApplicationCall, command: String) {
    val regex = Regex("https://www\\.bilibili\\.com/video/BV(\\w{10})")
    val match = regex.find(command)!!
    val bv = match.groupValues[1]
    val info = getVideoInfo(bv)

    val res = Json.encodeToString(GroupReplyVO(info.toString()))
    call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
}
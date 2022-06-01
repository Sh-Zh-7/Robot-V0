package shzh.me.commands

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import shzh.me.model.vo.GroupReplyVO

suspend fun handleGithub(call: ApplicationCall, link: String) {
    val regex = "(https://github.com/)".toRegex()
    val openGraph = regex.replace(link, "https://opengraph.githubassets.com/1/")

    val res = Json.encodeToString(GroupReplyVO("[CQ:image,file=$openGraph]"))
    call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
}
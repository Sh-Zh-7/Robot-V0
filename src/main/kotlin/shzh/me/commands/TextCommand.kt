package shzh.me.commands

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import shzh.me.model.vo.GroupReplyVO

suspend fun handlePing(call: ApplicationCall) {
    val ret = GroupReplyVO("pong!")
    val res = Json.encodeToString(ret)
    call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
}

suspend fun handleDice(call: ApplicationCall, command: String) {
    val (_, rule) = command.split(' ')
    val (number, count) = rule.split('d').map { str -> Integer.parseInt(str) }

    val dices  = (0 until number).map { (0..count).random() }
    val sum = dices.sum()
    val diceStr = dices.map { dice -> dice.toString() }.reduce { acc, s -> "$acc $s" }

    val reply = "您的点数为: $diceStr；\\n总计: $sum"
    val res = Json.encodeToString(reply)
    call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
}
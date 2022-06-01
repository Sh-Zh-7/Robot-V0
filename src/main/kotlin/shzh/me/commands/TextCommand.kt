package shzh.me.commands

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import shzh.me.model.vo.GroupReplyVO

suspend fun handlePing(call: ApplicationCall, messageID: Int) {
    val res = Json.encodeToString(GroupReplyVO("[CQ:reply,id=$messageID]pong!"))
    call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
}

suspend fun handleDice(call: ApplicationCall, command: String, messageID: Int) {
    val (_, rule) = command.split(' ')
    val (number, count) = rule.split('d').map { str -> Integer.parseInt(str) }

    if (number in 1..10 && count in 1..100) {
        val dices  = (0 until number).map { (1..count).random() }
        val sum = dices.sum()
        val diceStr = dices.map { dice -> dice.toString() }.reduce { acc, s -> "$acc $s" }

        val reply = "[CQ:reply,id=$messageID]您的点数为: $diceStr；\n总计: $sum"
        val res = Json.encodeToString(GroupReplyVO(reply))
        call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
    }
}
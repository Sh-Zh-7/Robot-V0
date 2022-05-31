package shzh.me.commands

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*

suspend fun handlePing(call: ApplicationCall) {
    val res = "{\"reply\": \"pong!\"}"
    call.respondText(res, ContentType.Application.Json, HttpStatusCode.OK)
}

suspend fun handleDice(call: ApplicationCall, command: String) {
    val (_, rule) = command.split(' ')
    val (number, count) = rule.split('d').map { str -> Integer.parseInt(str) }

    val dices  = (0 until number).map { _ -> (0..count).random() }
    val sum = dices.sum()
    val diceStr = dices.map { dice -> dice.toString() }.reduce { acc, s -> "$acc $s" }

    val reply = "您的点数为: $diceStr；\\n总计: $sum";
    val result = "{\"reply\": \"$reply\"}"
    call.respondText(result, ContentType.Application.Json, HttpStatusCode.OK)
}
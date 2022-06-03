package shzh.me.commands

import io.ktor.server.application.*
import shzh.me.services.replyMessage
import shzh.me.utils.MessageBuilder

suspend fun handlePing(call: ApplicationCall, messageID: Int) {
    val reply = MessageBuilder
        .reply(messageID)
        .text("pong!", newline = false)
        .content()
    replyMessage(call, reply)
}

suspend fun handleDice(call: ApplicationCall, command: String, messageID: Int) {
    val (_, rule) = command.split(' ')
    val (number, count) = rule.split('d').map { str -> Integer.parseInt(str) }

    if (number in 1..10 && count in 1..100) {
        val dices  = (0 until number).map { (1..count).random() }
        val sum = dices.sum()
        val diceStr = dices.map { dice -> dice.toString() }.reduce { acc, s -> "$acc $s" }

        val reply = MessageBuilder
            .reply(messageID)
            .text("您的点数为: $diceStr")
            .text("总计: $sum", newline = false)
            .content()
        replyMessage(call, reply)
    }
}
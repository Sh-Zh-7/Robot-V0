package shzh.me.commands

import io.ktor.server.application.*
import shzh.me.model.dto.MessageDTO
import shzh.me.services.impl.OneBotServiceImpl
import shzh.me.utils.MessageUtils

object PingCommand {
    private val onebotService = OneBotServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        val reply = MessageUtils
            .builder()
            .reply(message.messageID)
            .text("pong!")
            .content()
        onebotService.replyMessage(call, reply)
    }
}

object DiceCommand {
    private val onebotService = OneBotServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        val (_, rule) = message.message.split(' ')
        val (number, count) = rule.split('d').map { str -> Integer.parseInt(str) }

        if (number in 1..10 && count in 1..100) {
            val dices  = (0 until number).map { (1..count).random() }
            val sum = dices.sum()
            val diceStr = dices.map { dice -> dice.toString() }.reduce { acc, s -> "$acc $s" }

            val reply = MessageUtils
                .builder()
                .reply(message.messageID)
                .text("您的点数为: $diceStr")
                .text("总计: $sum")
                .content()
            onebotService.replyMessage(call, reply)
        }
    }
}

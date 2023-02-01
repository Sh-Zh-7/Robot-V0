package shzh.me.commands

import io.ktor.server.application.*
import shzh.me.model.dto.MessageDTO
import shzh.me.services.impl.OneBotServiceImpl
import shzh.me.utils.MessageUtils
import shzh.me.utils.TimeUtils
import java.util.*

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
            val dices = (0 until number).map { (1..count).random() }
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

object ProgressCommand {
    private val onebotService = OneBotServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_YEAR)
        val totalDays = TimeUtils.getTotalDaysInYear(calendar)

        val progress = day.toDouble() / totalDays
        val percent = (progress * 100).toUInt()

        val solid = (progress * 15).toInt()
        val remain = 15 - solid
        val bar = "▓".repeat(solid) + "░".repeat(remain)

        val reply = MessageUtils
            .builder()
            .text(bar)
            .text("${calendar.get(Calendar.YEAR)}年进度：$percent%")
            .content()
        onebotService.replyMessage(call, reply)
    }
}

object DiyCommand {
    private val onebotService = OneBotServiceImpl()

    suspend fun handle(call: ApplicationCall, message: MessageDTO) {
        onebotService.replyMessage(call, "你写就有了")
    }
}

object HelpCommand {

}

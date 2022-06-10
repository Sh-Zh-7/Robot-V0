package shzh.me

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch
import shzh.me.commands.KfcCommand
import shzh.me.plugins.configureRouting

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        launch {
            KfcCommand.polling()
        }
//        launch {
//            recoverPoolingBDyn()
//            recoverPoolingBLive()
//        }
    }.start(wait = true)
}

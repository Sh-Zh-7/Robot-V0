package shzh.me

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch
import shzh.me.commands.recoverPoolingJobs
import shzh.me.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
        launch {
            recoverPoolingJobs()
        }
    }.start(wait = true)
}

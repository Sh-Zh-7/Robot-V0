package shzh.me

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import shzh.me.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureRouting()
//        launch {
//            recoverPoolingBDyn()
//            recoverPoolingBLive()
//        }
    }.start(wait = true)
}

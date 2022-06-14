package shzh.me

import io.ktor.server.application.*
import io.ktor.server.netty.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import shzh.me.commands.*
import shzh.me.plugins.configureRouting

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    configureRouting()

    GlobalScope.launch {
        launch { KfcCommand.polling() }
        launch { BilibiliLiveCommand.recover() }
        launch { BilibiliDynamicCommand.recover() }
        launch { WeiboCommand.recover() }
        launch { ZhihuCommand.recover() }
        launch { GithubCommand.recover() }
    }
}

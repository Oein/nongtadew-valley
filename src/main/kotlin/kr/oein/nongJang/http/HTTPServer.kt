package kr.oein.nongJang.http

import io.javalin.Javalin
import kr.oein.nongJang.NongJang

class HTTPServer(val nj: NongJang) {
    private val app: Javalin = Javalin.create { config ->
        config.showJavalinBanner = false
    }


    val fm = Farmland(nj, app)

    init {
        Money(nj, app)
        Player(nj, app)
    }

    fun start(port: Int = 19682) {
        fm.calculateGrownStateForAllPlayers()
        fm.scheduleUpdate()

        app.start(port)
        nj.logger.info("Running NJ HTTP server on port $port")
    }

    fun stop() {
        app.stop()
    }
}
package kr.oein.nongJang.http

import io.javalin.Javalin
import kr.oein.nongJang.NongJang

class Player(val nj: NongJang, val app: Javalin) {
    init {
        app.get("/player/count") { ctx ->
            val count = nj.server.onlinePlayers.size
            ctx.result("SUC:$count")
        }
        app.get("/player/list") { ctx ->
            val playerList = nj.server.onlinePlayers.joinToString(",") { "${it.name}|${it.uniqueId}" }
            ctx.result("SUC:$playerList")
        }
        app.get("/player/namelist") { ctx ->
            val playerList = nj.server.onlinePlayers.joinToString(",") { it.name }
            ctx.result("SUC:$playerList")
        }
        app.get("/player/uuidlist") { ctx ->
            val uuidList = nj.server.onlinePlayers.joinToString(",") { it.uniqueId.toString() }
            ctx.result("SUC:$uuidList")
        }
        app.get("/player/name/{uuid}") { ctx ->
            val uuid = ctx.pathParam("uuid")
            val player = nj.server.getPlayer(java.util.UUID.fromString(uuid))
            if (player != null) {
                ctx.result("SUC:${player.name}")
            } else {
                ctx.status(404).result("PNF:Player Not Found")
            }
        }
        app.get("/player/uuid/{name}") { ctx ->
            val name = ctx.pathParam("name")
            val player = nj.server.getPlayerExact(name)
            if (player != null) {
                ctx.result("SUC:${player.uniqueId}")
            } else {
                ctx.status(404).result("PNF:Player Not Found")
            }
        }
    }
}
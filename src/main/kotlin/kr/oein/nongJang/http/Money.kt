package kr.oein.nongJang.http

import io.javalin.Javalin
import kr.oein.nongJang.NongJang
import java.util.UUID

class Money(val nj: NongJang, val app: Javalin) {
    init {
        app.get("/money/{uuid}") { ctx ->
            val uuid = ctx.pathParam("uuid")
            val player = nj.server.getPlayer(UUID.fromString(uuid))
            if (player != null) {
                val money = nj.moneyManager.getMoney(player)
                ctx.result("SUC:$money")
            } else {
                ctx.status(404).result("PNF:Player Not Found")
            }
        }
        app.get("/money/{uuid}/add") { ctx ->
            val uuid = ctx.pathParam("uuid")
            val amount = ctx.queryParam("amount")?.toLongOrNull()
            if (amount == null) {
                ctx.status(400).result("ERR:Invalid Amount")
                return@get
            }
            val player = nj.server.getPlayer(UUID.fromString(uuid))
            if (player != null) {
                nj.moneyManager.addMoney(player, amount)
                val newMoney = nj.moneyManager.getMoney(player)
                ctx.result("SUC:$newMoney")
            } else {
                ctx.status(404).result("PNF:Player Not Found")
            }
        }
        app.get("/money/{uuid}/remove") { ctx ->
            val uuid = ctx.pathParam("uuid")
            val amount = ctx.queryParam("amount")?.toLongOrNull()
            if (amount == null) {
                ctx.status(400).result("ERR:Invalid Amount")
                return@get
            }
            val player = nj.server.getPlayer(UUID.fromString(uuid))
            if (player != null) {
                nj.moneyManager.removeMoney(player, amount)
                val newMoney = nj.moneyManager.getMoney(player)
                ctx.result("SUC:$newMoney")
            } else {
                ctx.status(404).result("PNF:Player Not Found")
            }
        }
    }
}
package kr.oein.nongJang.utils

import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class InitialMoney(val nj: NongJang): Listener {
    val initialMoney = 1000000L
    val scope = nj.kvdb.loadScope("initialMoney")

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player
        val key = player.uniqueId.toString()
        if (!scope.has(key)) {
            scope.set(key, 1)
            nj.moneyManager.addMoney(player, initialMoney)
            player.sendMessage(Component.text("농장에 오신 것을 환영합니다! 초기 자금 ${initialMoney}원을 지급받았습니다.", NamedTextColor.GREEN))
        }
    }
}
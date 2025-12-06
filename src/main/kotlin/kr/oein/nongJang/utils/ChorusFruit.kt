package kr.oein.nongJang.utils

import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent

class ChorusFruitDisable(val nj: NongJang): Listener {
    @EventHandler
    fun onChorusFruit(event: PlayerItemConsumeEvent) {
        val material = event.item.type
        val player = event.player
        val world = player.world
        if (material == Material.CHORUS_FRUIT && world == nj.njCommands.nongjangWorld) {
            event.isCancelled = true
            player.sendMessage(Component.text("후렴과 열매는 먹을 수 없습니다!", NamedTextColor.RED))
        }
    }
}
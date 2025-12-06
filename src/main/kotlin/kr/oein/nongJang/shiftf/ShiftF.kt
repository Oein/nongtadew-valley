package kr.oein.nongJang.shiftf

import kr.oein.nongJang.NongJang
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class ShiftF(val nj: NongJang): Listener {

    @EventHandler
    fun onSwapItem(event: org.bukkit.event.player.PlayerSwapHandItemsEvent) {
        if (event.isCancelled) return
        if (event.player.isSneaking) {
            event.isCancelled = true
            nj.guiManager.openGUI(ShiftFGUI(nj), event.player)
        }
    }
}
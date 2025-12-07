package kr.oein.nongJang.starforce

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class Starforce: Listener {
    @EventHandler
    fun onBlockRightClick(event: org.bukkit.event.player.PlayerInteractEvent) {
        if (event.isCancelled) return
        val player = event.player
        val item = event.item ?: return
        val block = event.clickedBlock ?: return

        // check if the clicked block is a smithing table
        if (block.type != org.bukkit.Material.SMITHING_TABLE) return

        // open starforce GUI
        event.isCancelled = true
//        StarforceGUI.openStarforceGUI(player, item)
    }
}
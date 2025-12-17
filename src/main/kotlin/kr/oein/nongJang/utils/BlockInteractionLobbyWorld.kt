package kr.oein.nongJang.utils

import kr.oein.nongJang.NongJang
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent

class BlockInteractionLobbyWorld(val nj: NongJang): Listener {
    var sessionData = mutableMapOf<String, PlayerSessionData>()
    var nonProcessedUsers = mutableSetOf<Player>()

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player;
        nonProcessedUsers.add(player);
//        sessionData[player.uniqueId.toString()] = PlayerSessionData(
//            player = player,
//            isNewbie = true,
//            password = null,
//            discordId = null,
//            discordOauthToken = null
//        );
        player.teleport(nj.njCommands.lobbyWorld!!.spawnLocation.toCenterLocation().add(0.0, -0.5, 0.0));
        nj.guiManager.openGUI(PasswordDialogGUI(nj), player);

//        player.playSound(
//            player.location,
//            "minecraft:custom.bgm_intro",
//            0.5f, // volume
//            1.0f  // pitch
//        )
//        // run after 0.5 seconds
//        nj.server.scheduler.runTaskLater(nj, { ->
//            player.playSound(
//                player.location,
//                "minecraft:custom.bgm_loop",
//                0.5f, // volume
//                1.0f  // pitch
//            )
//        }, 10L)
    }

    @EventHandler
    fun onPlayerLeave(event: PlayerQuitEvent) {
        val player = event.player;
        if (sessionData.containsKey(player.uniqueId.toString())) {
            sessionData.remove(player.uniqueId.toString());
        }
        if (nonProcessedUsers.contains(player)) {
            nonProcessedUsers.remove(player);
        }
    }

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if(sessionData[player.uniqueId.toString()] != null) return
        if(player.isOp) return

        event.isCancelled = true
    }

    @EventHandler
    fun onBlockInteract(event: PlayerInteractEvent) {
        if (event.isCancelled) return
        val player = event.player
        if(player.isOp) return
        val world = player.world
        val block = event.clickedBlock ?: return
        if (world != nj.njCommands.lobbyWorld) return
        // Prevent block interaction in the lobby world
        event.isCancelled = true
    }


}
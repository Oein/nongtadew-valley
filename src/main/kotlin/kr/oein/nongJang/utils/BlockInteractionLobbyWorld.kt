package kr.oein.nongJang.utils

import io.papermc.paper.event.player.PlayerPickItemEvent
import kr.oein.nongJang.NongJang
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPlaceEvent
import org.bukkit.event.entity.FoodLevelChangeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerBedEnterEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketEntityEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
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
        player.teleport(nj.njCommands.lobbyWorld!!.spawnLocation.toCenterLocation().add(0.0, -0.5, 0.0));
        nj.guiManager.openGUI(PasswordDialogGUI(nj), player);
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
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return

        event.isCancelled = true
    }

    @EventHandler
    fun onBlockInteract(event: PlayerInteractEvent) {
        if (event.isCancelled) return
        val player = event.player
        if(player.isOp) return
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }

    @EventHandler
    fun onChorusFruit(event: PlayerItemConsumeEvent) {
        if (event.isCancelled) return
        val player = event.player
        if(player.isOp) return
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }

    @EventHandler
    fun e1(event: EntityPlaceEvent) {
        if (event.isCancelled) return
        val player = event.player ?: return
        if(player.isOp) return
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }

    @EventHandler
    fun e2(event: PlayerBedEnterEvent) {
        if (event.isCancelled) return
        val player = event.player
        if(player.isOp) return
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }

    @EventHandler
    fun e4(event: PlayerDropItemEvent) {
        if (event.isCancelled) return
        val player = event.player
        if(player.isOp) return
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }

    @EventHandler
    fun e5(event: PlayerPickItemEvent) {
        if (event.isCancelled) return
        val player = event.player
        if(player.isOp) return
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }

    @EventHandler
    fun e6(event: ProjectileLaunchEvent) {
        if (event.isCancelled) return
        val shooter = event.entity.shooter
        if (shooter !is Player) return
        if(shooter.isOp) return
        val world = shooter.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }

    @EventHandler
    fun e7(event: PlayerDeathEvent) {
        if (event.isCancelled) return
        val player = event.entity
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }

    @EventHandler
    fun e8(event: EntityDamageEvent) {
        if (event.isCancelled) return
        val player = event.entity
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }



    @EventHandler
    fun e9(event: FoodLevelChangeEvent) {
        if (event.isCancelled) return
        val player = event.entity
        if(player !is Player) return
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }

    @EventHandler
    fun e10(event: PlayerBucketFillEvent) {
        val player = event.player
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }


    @EventHandler
    fun e11(event: PlayerBucketEmptyEvent) {
        val player = event.player
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }

    @EventHandler
    fun e12(event: PlayerBucketEntityEvent) {
        val player = event.player
        val world = player.world
        if (world != nj.njCommands.lobbyWorld) return
        event.isCancelled = true
    }
}
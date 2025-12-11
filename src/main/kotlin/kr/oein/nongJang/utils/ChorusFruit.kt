package kr.oein.nongJang.utils

import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.AreaEffectCloudApplyEvent
import org.bukkit.event.entity.PotionSplashEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerMoveEvent

class BlockInteraction(val nj: NongJang): Listener {
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

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        val player = event.player
        if(player.isOp) return
        val world = player.world
        if (world == nj.njCommands.nongjangWorld) {
            val pos = player.location
            val chunkX = pos.x.toInt() shr 4
            val chunkZ = pos.z.toInt() shr 4

            val chkOwner = nj.chunkManager.getOwner(chunkX, chunkZ)
            if(chkOwner == null || chkOwner != player) {
                val from = event.from
                val to = event.to
                if (from.blockX != to.blockX || from.blockY != to.blockY || from.blockZ != to.blockZ) {
                    // teleport back
                    event.isCancelled = true
                    player.teleport(from)
                    player.sendMessage(Component.text("다른 사람의 땅에 들어갈 수 없습니다!", NamedTextColor.RED))
                }
            }
        }
    }

    @EventHandler
    fun onPlayerOpenChest(event: org.bukkit.event.player.PlayerInteractEvent) {
        if (event.isCancelled) return
        val player = event.player
        if(player.isOp) return
        val world = player.world
        val block = event.clickedBlock ?: return
        if (world == nj.njCommands.nongjangWorld) {
            val pos = block.location
            val chunkX = pos.x.toInt() shr 4
            val chunkZ = pos.z.toInt() shr 4

            val chkOwner = nj.chunkManager.getOwner(chunkX, chunkZ)
            if(chkOwner == null || chkOwner != player) {
                event.isCancelled = true
                player.sendMessage(Component.text("다른 사람의 땅에 물건과 상호작용 할 수 없습니다!", NamedTextColor.RED))
            }
        }
    }


    @EventHandler
    fun onPlayerItemConsumeEvent(event: PlayerItemConsumeEvent) {
        val material = event.item.type
        val player = event.player
        val world = player.world
        if ((material == Material.POTION || material == Material.SPLASH_POTION || material == Material.LINGERING_POTION) && world == nj.njCommands.nongjangWorld) {
            event.isCancelled = true
            player.sendMessage(Component.text("농장 내에서는 포션을 사용할 수 없습니다!", NamedTextColor.RED))
        }
    }

    @EventHandler
    fun onPotionSplashEvent(event: PotionSplashEvent) {
        val potion = event.entity
        val world = potion.world
        nj.logger.info { "EVENT2" }
        if (world == nj.njCommands.nongjangWorld) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun areaEffectCloudEvent(event: AreaEffectCloudApplyEvent) {
        val cloud = event.entity
        val world = cloud.world
        nj.logger.info { "EVENT3" }
        if (world == nj.njCommands.nongjangWorld) {
            event.isCancelled = true
        }
    }
}
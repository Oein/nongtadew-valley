package kr.oein.nongJang.farm

import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.components.CustomModelDataComponent
import org.bukkit.persistence.PersistentDataType
import kotlin.math.min

enum class GrowingLevel {
    SEED,
    GROWING,
    GROWN,
    SHIT
}

class Grow(val nj: NongJang): Listener {
    fun broadcast(message: String) {
        nj.server.broadcastMessage(
            message
        )
    }
    fun handleChunk(x: Int, z: Int) {
        broadcast("  # Handle chunk at ($x, $z)")
        for(xApd in 0..15) {
            for(zApd in 0..15) {
                val blockX = (x shl 4) + xApd
                val blockZ = (z shl 4) + zApd
                val highestBlock = getHeighestBlock(blockX, blockZ) ?: continue
                if(highestBlock.type != Material.VOID_AIR) continue

                // get nearest item frame
                val nearbyItemFrame = highestBlock.location.toCenterLocation().getNearbyEntitiesByType(
                    org.bukkit.entity.ItemFrame::class.java,
                    0.5
                )

                // print nearby item frames
                if(nearbyItemFrame.isEmpty()) continue

                var grownLevel = nearbyItemFrame.first().item.persistentDataContainer.get(namedkeys.grownLevel, PersistentDataType.DOUBLE) ?: continue
                var shitLevel = nearbyItemFrame.first().item.persistentDataContainer.get(namedkeys.shitLevel, PersistentDataType.DOUBLE) ?: continue
                val productType = nearbyItemFrame.first().item.persistentDataContainer.get(namedkeys.productType, PersistentDataType.STRING) ?: continue

                grownLevel += 25.0

                grownLevel = min(grownLevel, 100.0)
                shitLevel = min(shitLevel, 100.0)

                val newGrowState = when {
                    grownLevel >= 100.0 -> {
                        GrowingLevel.GROWN
                    }
                    grownLevel >= 50.0 -> {
                        GrowingLevel.GROWING
                    }
                    else -> {
                        GrowingLevel.SEED
                    }
                }

                broadcast("   Found farmable block at (${highestBlock.x}, ${highestBlock.y}, ${highestBlock.z})")
                broadcast("    - Grown level: $grownLevel")
                broadcast("    - Shit Level: $shitLevel")
                broadcast("    - Grown State: $newGrowState")

                nearbyItemFrame.first().setItem(
                    createCBDItem(
                        productType,
                        newGrowState,
                        grownLevel,
                        shitLevel
                    )
                )
            }
        }
    }
    fun handlePlayerGrowth(player: Player) {
        broadcast(" # Handling growth for player ${player.name}")
        val playerChunks = nj.chunkManager.getMyChunks(player)

        for(chunk in playerChunks)
            handleChunk(chunk.first, chunk.second)
    }

    fun handleGrowth() {
        broadcast("# Starting growth handling")
        for(player in nj.server.onlinePlayers)
            handlePlayerGrowth(player)
    }

    fun getHeighestBlock(x: Int, z: Int): Block? {
        val world = nj.njCommands.nongjangWorld ?: return null

        for (y in 254 downTo 0) {
            val block = world.getBlockAt(x, y, z)
            if (block.type != Material.AIR) {
                return block
            }
        }

        return null
    }

    fun createSeedItem(product: String): ItemStack {
        // use itemFrame with custom model data as seed
        val itemStack = ItemStack(Material.ITEM_FRAME, 1)
        val meta = itemStack.itemMeta

        meta.customModelDataComponent.strings = listOf("seed_$product")
        meta.persistentDataContainer.set(namedkeys.productType, PersistentDataType.STRING, product)
        itemStack.itemMeta = meta
        return itemStack
    }

    fun createCBDItem(product: String, level: GrowingLevel, grownLevel: Double? = null, shitLevel: Double? = null): ItemStack? {
        val itemStack = ItemStack(Material.DIRT, 1)
        val meta = itemStack.itemMeta

        val product = namedkeys.products.findLast { it.id == product } ?: return null
        val cbd = when (level) {
            GrowingLevel.SEED -> product.seed_cbd
            GrowingLevel.GROWING -> product.growing_cbd
            GrowingLevel.GROWN -> product.grown_cbd
            GrowingLevel.SHIT -> product.shit_cbd
        }
        val customModelDataComponent = meta.customModelDataComponent
        customModelDataComponent.strings = listOf(cbd)
        meta.setCustomModelDataComponent(customModelDataComponent)
        meta.persistentDataContainer.set(namedkeys.productType, PersistentDataType.STRING, product.id)
        meta.persistentDataContainer.set(namedkeys.grownLevel, PersistentDataType.DOUBLE, grownLevel ?: 0.0)
        meta.persistentDataContainer.set(namedkeys.shitLevel, PersistentDataType.DOUBLE, shitLevel ?: 0.0)
        itemStack.itemMeta = meta

        return itemStack
    }

    // on place item frame
    @EventHandler
    fun onPlaceItemFrame(event: HangingPlaceEvent) {
        val entity = event.entity
        if (entity !is org.bukkit.entity.ItemFrame)
            return

        val world = entity.world
        if (world != nj.njCommands.nongjangWorld)
            return

        val item = event.itemStack ?: return
        val productType = item.persistentDataContainer.get(namedkeys.productType, PersistentDataType.STRING) ?: return

        if(!namedkeys.products.map { it.id }.contains(productType))
            return

        if(event.blockFace != BlockFace.UP) {
            event.isCancelled = true
            return
        }

        val blockUnder = event.block.getRelative(BlockFace.DOWN)
        if(blockUnder.type != Material.FARMLAND) {
            event.isCancelled = true
            return
        }

        val cbdItem = createCBDItem(productType, GrowingLevel.SEED)
        if (cbdItem == null) {
            event.isCancelled = true
            return
        }
        event.block.type = Material.VOID_AIR
        entity.setItem(cbdItem)
        entity.isVisible = false
    }
}
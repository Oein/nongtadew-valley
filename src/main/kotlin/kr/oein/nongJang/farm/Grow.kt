package kr.oein.nongJang.farm

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.datacomponent.item.consumable.ConsumeEffect
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation
import io.papermc.paper.event.player.PlayerItemFrameChangeEvent
import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Farmland
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.hanging.HangingBreakByEntityEvent
import org.bukkit.event.hanging.HangingBreakEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.min
import kotlin.math.roundToInt


enum class GrowingLevel {
    SEED,
    GROWING,
    GROWN,
    SHIT
}

enum class HarvestedLevel {
    RAW,
    MATURE,
    ROTTEN
}

class Grow(val nj: NongJang): Listener {
    @Suppress("DEPRECATION")
    fun broadcast(message: String) {
        nj.server.broadcastMessage(
            message
        )
    }

    fun productById(id: String): Product? {
        return FarmConfig.products.findLast { it.id == id }
    }

    fun handleChunk(x: Int, z: Int) {
        val soil = nj.chunkManager.getSoil(x, z) ?: return
        val wet = nj.chunkManager.getHumidity(x, z) ?: return
        val temp = nj.chunkManager.getTemperature(x, z) ?: return

        for(xApd in 0..15) {
            for(zApd in 0..15) {
                val blockX = (x shl 4) + xApd
                val blockZ = (z shl 4) + zApd
                val highestBlock = getHighestBlock(blockX, blockZ) ?: continue
                if(highestBlock.type != Material.VOID_AIR) continue

                // get nearest item frame
                val nearbyItemFrames = highestBlock.location.toCenterLocation().getNearbyEntitiesByType(
                    ItemFrame::class.java,
                    0.5
                )

                // print nearby item frames
                if(nearbyItemFrames.size != 1) continue

                val nearbyItemFrame = nearbyItemFrames.first()

                var grownLevel = nearbyItemFrame.item.persistentDataContainer.get(FarmConfig.grownLevel, PersistentDataType.DOUBLE) ?: continue
                var shitLevel = nearbyItemFrame.item.persistentDataContainer.get(FarmConfig.shitLevel, PersistentDataType.DOUBLE) ?: continue
                val productType = nearbyItemFrame.item.persistentDataContainer.get(FarmConfig.productType, PersistentDataType.STRING) ?: continue
                val product = productById(productType) ?: continue

                if(grownLevel < 100.0 && shitLevel < 100.0) {
                    grownLevel += product.calculateGrow(
                        temp.toDouble(),
                        soil.toDouble(),
                        wet.toDouble()
                    )
                    shitLevel += product.calculateShit(
                        temp.toDouble(),
                        soil.toDouble(),
                        wet.toDouble()
                    )
                }

                grownLevel = min(grownLevel, 100.0)
                shitLevel = min(shitLevel, 100.0)

                val newGrowState = when {
                    shitLevel >= 100.0 -> {
                        GrowingLevel.SHIT
                    }
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

//                broadcast("Seed[$blockX, $blockZ] GrownLevel: $grownLevel, ShitLevel: $shitLevel")

                val cbdItem = createCBDItem(
                    productType,
                    newGrowState,
                    grownLevel,
                    shitLevel
                ) ?: continue

                val meta = cbdItem.itemMeta
                meta.customName(
                    Component.text(
                        if(shitLevel >= 100.0)
                            "썩음 (${(grownLevel * 10.0).roundToInt() / 10.0}%, ${(shitLevel * 10.0).roundToInt() / 10.0}%)"
                        else
                            "자라는중 (${(grownLevel * 10.0).roundToInt() / 10.0}%, ${(shitLevel * 10.0).roundToInt() / 10.0}%)"
                    )
                )
                cbdItem.itemMeta = meta

                nearbyItemFrame.setItem(cbdItem)
            }
        }
    }

    fun handlePlayerGrowth(player: Player) {
        val playerChunks = nj.chunkManager.getMyChunks(player)

        for(chunk in playerChunks)
            handleChunk(chunk.first, chunk.second)
    }

    fun handleGrowth() {
        for(player in nj.server.onlinePlayers)
            handlePlayerGrowth(player)
    }

    fun getHighestBlock(x: Int, z: Int): Block? {
        nj.njCommands.ensureNongJangWorld() ?: return null
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
        val productClass = productById(product)

        if(productClass == null) {
            val itemStack = ItemStack(Material.DIRT, 1)
            val meta = itemStack.itemMeta
            meta.customName(
                Component.text("알 수 없는 씨앗 ($product)")
            )
            itemStack.itemMeta = meta
            return itemStack
        }

        // use itemFrame with custom model data as seed
        val itemStack = ItemStack(Material.ITEM_FRAME, 1)
        val meta = itemStack.itemMeta

        val customModelDataComponent = meta.customModelDataComponent
        customModelDataComponent.strings = listOf(productClass.seedGuiCbd)
        meta.setCustomModelDataComponent(customModelDataComponent)

        meta.persistentDataContainer.set(FarmConfig.productType, PersistentDataType.STRING, product)

        meta.customName(
            Component.text("${productClass.name ?: product} 씨앗")
        )
        itemStack.itemMeta = meta
        return itemStack
    }

    fun createCBDItem(product: String, level: GrowingLevel, grownLevel: Double? = null, shitLevel: Double? = null): ItemStack? {
        val itemStack = ItemStack(Material.DIRT, 1)
        val meta = itemStack.itemMeta

        val product = FarmConfig.products.findLast { it.id == product } ?: return null
        val cbd = when (level) {
            GrowingLevel.SEED -> product.seedCbd
            GrowingLevel.GROWING -> product.growingCbd
            GrowingLevel.GROWN -> product.grownCbd
            GrowingLevel.SHIT -> product.shitCbd
        }
        val customModelDataComponent = meta.customModelDataComponent
        customModelDataComponent.strings = listOf(cbd)
        meta.setCustomModelDataComponent(customModelDataComponent)
        meta.persistentDataContainer.set(FarmConfig.productType, PersistentDataType.STRING, product.id)
        meta.persistentDataContainer.set(FarmConfig.grownLevel, PersistentDataType.DOUBLE, grownLevel ?: 0.0)
        meta.persistentDataContainer.set(FarmConfig.shitLevel, PersistentDataType.DOUBLE, shitLevel ?: 0.0)
        itemStack.itemMeta = meta

        return itemStack
    }

    fun createHarvestedItem(id: String, level: HarvestedLevel): ItemStack? {
        val itemStack = ItemStack(Material.DIRT, 1)
        val meta = itemStack.itemMeta

        val productClass = FarmConfig.products.findLast { it.id == id } ?: return null
        val cbd = when (level) {
            HarvestedLevel.RAW -> productClass.shitCbd
            HarvestedLevel.MATURE -> productClass.grownCbd
            HarvestedLevel.ROTTEN -> productClass.shitCbd
        }

        val customModelDataComponent = meta.customModelDataComponent
        customModelDataComponent.strings = listOf(cbd)
        meta.setCustomModelDataComponent(customModelDataComponent)

        val readableName = productClass.name ?: productClass.id
        meta.customName(
            Component.text(
                when(level) {
                    HarvestedLevel.RAW -> "들 자란 $readableName"
                    HarvestedLevel.MATURE -> "익은 $readableName"
                    HarvestedLevel.ROTTEN -> "썩은 $readableName"
                }
            )
        )
        meta.persistentDataContainer.set(FarmConfig.productType, PersistentDataType.STRING, productClass.id)

        itemStack.itemMeta = meta

        if(level == HarvestedLevel.RAW || level == HarvestedLevel.ROTTEN) {
            val builder = itemStack.getData(DataComponentTypes.CONSUMABLE)
                ?.toBuilder()
                ?: Consumable.consumable()

            builder.animation(ItemUseAnimation.EAT)
            builder.addEffect(
                ConsumeEffect.applyStatusEffects(
                    listOf(
                        PotionEffect(
                            PotionEffectType.INSTANT_DAMAGE,
                            1,
                            100
                        )
                    ),
                    1.0f
                )
            )

            itemStack.setData(DataComponentTypes.CONSUMABLE, builder.build())
            itemStack.setData(
                DataComponentTypes.FOOD,
                FoodProperties.food()
                    .nutrition(1)
                    .saturation(1.0f)
                    .canAlwaysEat(true)
                    .build()
            )
        } else {
            val builder = itemStack.getData(DataComponentTypes.CONSUMABLE)
                ?.toBuilder()
                ?: Consumable.consumable()

            builder.animation(ItemUseAnimation.EAT)

            itemStack.setData(DataComponentTypes.CONSUMABLE, builder.build())
            itemStack.setData(
                DataComponentTypes.FOOD,
                FoodProperties.food()
                    .nutrition(2)
                    .saturation(2.4f)
                    .build()
            )
        }

        return itemStack
    }

    // on place item frame
    @EventHandler
    fun onPlaceItemFrame(event: HangingPlaceEvent) {
        val entity = event.entity
        if (entity !is ItemFrame)
            return

        val world = entity.world
        if (world != nj.njCommands.nongjangWorld)
            return

        val item = event.itemStack ?: return
        val productType = item.persistentDataContainer.get(FarmConfig.productType, PersistentDataType.STRING) ?: return

        if(!FarmConfig.products.map { it.id }.contains(productType))
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

    fun itemBreakHandle(entity: ItemFrame, setCanceled: (canceled: Boolean) -> Unit) {
        val world = entity.world
        if (world != nj.njCommands.nongjangWorld)
            return

        val item = entity.item
        val productType = item.persistentDataContainer.get(FarmConfig.productType, PersistentDataType.STRING) ?: return

        if(!FarmConfig.products.map { it.id }.contains(productType))
            return

        setCanceled(true)
        val cbdItem = createHarvestedItem(
            productType,
            when {
                (item.persistentDataContainer.get(FarmConfig.shitLevel, PersistentDataType.DOUBLE) ?: 0.0) >= 100.0 -> {
                    HarvestedLevel.ROTTEN
                }
                (item.persistentDataContainer.get(FarmConfig.grownLevel, PersistentDataType.DOUBLE) ?: 0.0) >= 100.0 -> {
                    HarvestedLevel.MATURE
                }
                else -> {
                    HarvestedLevel.RAW
                }
            }
        ) ?: return
        entity.remove()

        val block = entity.location.block
        if(block.type == Material.VOID_AIR) {
            block.type = Material.AIR
        }

        block.world.dropItemNaturally(
            block.location.toCenterLocation(),
            cbdItem
        )
    }

    @EventHandler
    fun onItemFrameBreak(event: HangingBreakEvent) {
        val entity = event.entity
        if (entity !is ItemFrame)
            return

        itemBreakHandle(entity) { canceled ->
            event.isCancelled = canceled
        }
    }

    @EventHandler
    fun onItemFrameBreakByEntity(event: HangingBreakByEntityEvent) {
        val entity = event.entity
        if (entity !is ItemFrame)
            return

        itemBreakHandle(entity) { canceled ->
            event.isCancelled = canceled
        }
    }

    @EventHandler
    fun onItemFrameItemPickup(event: PlayerItemFrameChangeEvent) {
        val itemEntity = event.itemFrame

        itemBreakHandle(itemEntity) { canceled ->
            event.isCancelled = canceled
        }
    }

    // 땅을 갈았을때
    @EventHandler
    fun onPlowLand(event: PlayerInteractEvent) {
        val player = event.player
        var clicked = event.clickedBlock ?: return
        val world = player.world
        if (world != nj.njCommands.nongjangWorld)
            return

        if (event.action != Action.RIGHT_CLICK_BLOCK)
            return

        val itemInHand = event.item ?: return
        if(!itemInHand.type.name.contains("_HOE"))
            return

        if(clicked.type != Material.GRASS_BLOCK && clicked.type != Material.DIRT)
            return

        event.isCancelled = true

        clicked.type = Material.FARMLAND
        val farmlandTyped = clicked.blockData as Farmland
        farmlandTyped.moisture = farmlandTyped.maximumMoisture
        clicked.blockData = farmlandTyped
    }

    // 간 땅이 dirt로 바뀔때, (사람이 점프해서)
    @EventHandler
    fun onDeplowLand(event: EntityChangeBlockEvent) {
        val block = event.block
        val world = block.world
        if(world != nj.njCommands.nongjangWorld)
            return

        val newBlock = event.to
        if(block.type != Material.FARMLAND)
            return
        if(newBlock != Material.DIRT)
            return

        // check has item frame
        val upblock = block.getRelative(BlockFace.UP)
        if(upblock.type != Material.VOID_AIR)
            return

        val nearbyItemFrames = upblock.location.toCenterLocation().getNearbyEntitiesByType(
            ItemFrame::class.java,
            0.5
        )

        if(nearbyItemFrames.size != 1)
            return

        val nearbyItemFrame = nearbyItemFrames.first()
        itemBreakHandle(nearbyItemFrame) { _ -> }
    }

    val scheduleInterval = 10
    var leftTicks = FarmConfig.FULL_GROW_TICKS

    fun scheduleGrowthHandling() {
        nj.server.scheduler.runTaskTimer(
            nj,
            { ->
                if (leftTicks <= 0) {
                    handleGrowth()
                    leftTicks = FarmConfig.FULL_GROW_TICKS
                } else {
                    leftTicks -= scheduleInterval
                }
            },
            0L,
            scheduleInterval.toLong() // every 1 second
        )
    }
}
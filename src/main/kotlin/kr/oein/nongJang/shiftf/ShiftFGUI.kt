package kr.oein.nongJang.shiftf

import kr.oein.interchest.InventoryButton
import kr.oein.interchest.InventoryGUI
import kr.oein.nongJang.NongJang
import kr.oein.nongJang.farm.FarmConfig
import kr.oein.nongJang.farm.GrowingLevel
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import java.lang.Long.parseLong
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class ShiftFGUI(val nj: NongJang): InventoryGUI() {
    @Suppress("DEPRECATION")
    override fun createInventory(): Inventory {
        return Bukkit.createInventory(null, 4 * 9, "농타듀벨리 Shift-F 메뉴")
    }

    fun handleHarvestChunk(chunkX: Int, chunkZ: Int): Long {
        var res = 0L
        for(xApd in 0..15) {
            for(zApd in 0..15) {
                val blockX = (chunkX shl 4) + xApd
                val blockZ = (chunkZ shl 4) + zApd
                val highestBlock = nj.grow.getHighestBlock(blockX, blockZ) ?: continue
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

                if(grownLevel < 100.0) continue
                if(shitLevel >= 100.0) continue

                nearbyItemFrame.remove()
                val price = nj.productPrice.getPrice(productType)
                res += price
            }
        }
        return res
    }

    fun harvestAllFarms(player: Player): Long {
        val playerChunks = nj.chunkManager.getMyChunks(player)
        var res = 0L

        for(chunk in playerChunks)
            res += handleHarvestChunk(chunk.first, chunk.second)

        return res
    }

    val cooldownScope = nj.kvdb.loadScope("shiftf_cooldown")

    fun harvestAndGiveMoney(player: Player, multiply: Double) {
        val moneyEarned = (harvestAllFarms(player) * multiply).roundToLong()
        if(moneyEarned > 0) {
            nj.moneyManager.addMoney(player, moneyEarned)
            player.sendMessage(
                Component.text("농장 전체 수확이 완료되었습니다! ", NamedTextColor.GREEN)
                    .append(
                        Component.text("획득 금액: ${moneyEarned}원", NamedTextColor.GOLD)
                    )
            )
        } else {
            player.sendMessage(
                Component.text("수확할 농장물이 없습니다!", NamedTextColor.RED)
            )
        }
    }

    fun harvestAndReplantChunk(chunkX: Int, chunkZ: Int, moneyInput: Long, player: Player): Long {
        var res = moneyInput
        for(xApd in 0..15) {
            for (zApd in 0..15) {
                val blockX = (chunkX shl 4) + xApd
                val blockZ = (chunkZ shl 4) + zApd
                val highestBlock = nj.grow.getHighestBlock(blockX, blockZ) ?: continue
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

                if(grownLevel < 100.0) continue
                if(shitLevel >= 100.0) continue

                val price = nj.productPrice.getPrice(productType)
                res += price

                val product = nj.grow.productById(productType) ?: continue
                val seedPrice = product.seedPrice

                var seedPurchasable = false
                if(res >= seedPrice) {
                    res -= seedPrice
                    seedPurchasable = true
                }
                else {
                    val playerMoney = nj.moneyManager.getMoney(player)
                    if(playerMoney >= seedPrice) {
                        nj.moneyManager.removeMoney(player, seedPrice)
                        seedPurchasable = true
                    }
                }

                if(!seedPurchasable) {
                    // remove item frame without replanting
                    nearbyItemFrame.remove()
                    continue
                }

                nearbyItemFrame.setItem(
                    nj.grow.createCBDItem(
                        productType,
                        GrowingLevel.SEED
                    )
                )
            }
        }

        return res
    }


    fun harvestAndReplant(player: Player): Long {
        val playerChunks = nj.chunkManager.getMyChunks(player)
        var res = 0L

        for(chunk in playerChunks)
            res = harvestAndReplantChunk(chunk.first, chunk.second, res, player)

        return res
    }

    fun harvestReplantAndGiveMoney(player: Player, multiply: Double) {
        val moneyEarned = (harvestAndReplant(player) * multiply).roundToLong()
        if(moneyEarned > 0) {
            nj.moneyManager.addMoney(player, moneyEarned)
            player.sendMessage(
                Component.text("농장 전체 수확 및 재심기가 완료되었습니다! ", NamedTextColor.GREEN)
                    .append(
                        Component.text("획득 금액: ${moneyEarned}원", NamedTextColor.GOLD)
                    )
            )
        } else {
            player.sendMessage(
                Component.text("수확할 농장물이 없습니다!", NamedTextColor.RED)
            )
        }
    }

    fun cooldown(player: Player, key: String, hours: Long): Boolean {
        val cooldownKey = "${player.uniqueId}__$key"
        val lastUsedStr = cooldownScope.get(cooldownKey)
        val lastUsed = lastUsedStr?.let {
            parseLong(it.toString())
        } ?: 0L
        val currentTime = System.currentTimeMillis()
        val cooldownMillis = hours * 60 * 60 * 1000

        return if (currentTime - lastUsed >= cooldownMillis) {
            cooldownScope.set(cooldownKey, currentTime.toString())
            true
        } else {
            val timeLeft = cooldownMillis - (currentTime - lastUsed)
            val hoursLeft = timeLeft / (60 * 60 * 1000)
            val minutesLeft = (timeLeft % (60 * 60 * 1000)) / (60 * 1000)
            player.sendMessage(
                Component.text("이 기능은 쿨타임이 있습니다! 남은 시간: ${hoursLeft}시간 ${minutesLeft}분", NamedTextColor.RED)
            )
            false
        }
    }

    override fun decorate(player: Player?) {
        // set 11 to grass block
        this.addButton(slot(1, 1),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(Material.GRASS_BLOCK)
                    val meta = itemStack.itemMeta
                    meta?.customName(
                        Component.text("야생으로", NamedTextColor.GREEN)
                    )
                    itemStack.itemMeta = meta
                    itemStack
                }
                .consumer { event ->
                    val p = event?.let { it.whoClicked as Player }
                    if(p != null) {
                        p.closeInventory()
                        nj.playerWildTP.playerWildWorld(p)
                    }
                }
        )
        this.addButton(slot(2, 1),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(Material.MYCELIUM)
                    val meta = itemStack.itemMeta
                    meta?.customName(
                        Component.text("야생으로(랜덤)", NamedTextColor.GREEN)
                    )
                    itemStack.itemMeta = meta
                    itemStack
                }
                .consumer { event ->
                    val p = event?.let { it.whoClicked as Player }
                    if(p != null) {
                        p.closeInventory()
                        nj.playerWildTP.playerWildWorld(p, true)
                    }
                }
        )
        this.addButton(slot(3, 1),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(Material.CHEST)
                    val meta = itemStack.itemMeta
                    meta?.customName(
                        Component.text("로비로", NamedTextColor.GREEN)
                    )
                    itemStack.itemMeta = meta
                    itemStack
                }
                .consumer { event ->
                    val p = event?.let { it.whoClicked as Player }
                    if(p != null) {
                        p.closeInventory()
                        nj.njCommands.lobbyWorld?.let {
                            p.teleport(
                                it.spawnLocation
                            )
                        }
                    }
                }
        )

        this.addButton(slot(1, 2),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(Material.VILLAGER_SPAWN_EGG)
                    val meta = itemStack.itemMeta
                    meta?.customName(
                        Component.text("내 농장 보기", NamedTextColor.GOLD)
                    )
                    itemStack.itemMeta = meta
                    itemStack
                }
                .consumer { event ->
                    val p = event?.let { it.whoClicked as Player }
                    if(p != null) {
                        p.closeInventory()
                        val gui = MyNongJangGUI(nj, p, 0)
                        nj.guiManager.openGUI(gui, p)
                    }
                }
        )
        this.addButton(slot(2, 2),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(Material.BIRCH_WOOD)
                    val meta = itemStack.itemMeta
                    meta?.customName(
                        Component.text("농장 정보 보기", NamedTextColor.AQUA)
                    )
                    itemStack.itemMeta = meta
                    itemStack
                }
                .consumer { event ->
                    val p = event?.let { it.whoClicked as Player }
                    if(p != null) {
                        p.closeInventory()
                        val plworld = p.world
                        if(plworld != nj.njCommands.nongjangWorld) {
                            p.sendMessage(Component.text("농장 정보는 농장 월드에서만 확인할 수 있습니다!", NamedTextColor.RED))
                            return@consumer
                        }
                        val chunkX = p.location.x.toInt() shr 4
                        val chunkZ = p.location.z.toInt() shr 4
                        val owner = nj.chunkManager.getOwner(chunkX, chunkZ)
                        if(owner?.uniqueId != p.uniqueId) {
                            p.sendMessage(Component.text("자신의 농장에서만 농장 정보를 확인할 수 있습니다!", NamedTextColor.RED))
                            return@consumer
                        }

                        val gui = NongJangInfoGUI(nj, chunkX, chunkZ)
                        nj.guiManager.openGUI(gui, p)
                    }
                }
        )
        this.addButton(slot(3, 2),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(Material.BIRCH_SIGN)
                    val meta = itemStack.itemMeta
                    meta?.customName(
                        Component.text("농장 구입하기", NamedTextColor.YELLOW)
                    )
                    itemStack.itemMeta = meta
                    itemStack
                }
                .consumer { event ->
                    val p = event?.let { it.whoClicked as Player }
                    if(p != null) {
                        p.closeInventory()
                        val gui = PurchaseNongJangGUI(nj, p, 0, 0)
                        nj.guiManager.openGUI(gui, p)
                    }
                }
        )

        this.addButton(slot(4, 2),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(Material.WOODEN_HOE)
                    val meta = itemStack.itemMeta
                    meta?.customName(
                        Component.text("농장 전체 수확하기", NamedTextColor.YELLOW)
                    )
                    meta.lore(
                        listOf(
                            Component.text("이용시 농장물을 판매할때 40%의 가격으로 판매합니다.", NamedTextColor.GRAY)
                        )
                    )
                    itemStack.itemMeta = meta
                    itemStack
                }
                .consumer { event ->
                    val p = event?.let { it.whoClicked as Player }
                    if(p != null) {
                        p.closeInventory()
                        p.sendMessage(Component.text("농장 전체 수확을 시작합니다...", NamedTextColor.GREEN))
                        harvestAndGiveMoney(p, 0.4)
                    }
                }
        )
        this.addButton(slot(5, 2),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(Material.IRON_HOE)
                    val meta = itemStack.itemMeta
                    meta?.customName(
                        Component.text("농장 전체 수확하기", NamedTextColor.YELLOW)
                    )
                    meta.lore(
                        listOf(
                            Component.text("이용시 농장물을 판매할때 70%의 가격으로 판매합니다.", NamedTextColor.GRAY),
                            Component.text("이용 쿨타임은 48시간 입니다.", NamedTextColor.GRAY),
                        )
                    )
                    itemStack.itemMeta = meta
                    itemStack
                }
                .consumer { event ->
                    val p = event?.let { it.whoClicked as Player }
                    if(p != null) {
                        p.closeInventory()
                        val cooldownOk = cooldown(p, "iron_hoe_harvest", 48)
                        if(cooldownOk) {
                            p.sendMessage(Component.text("농장 전체 수확을 시작합니다...", NamedTextColor.GREEN))
                            harvestAndGiveMoney(p, 0.7)
                        }
                    }
                }
        )
        this.addButton(slot(6, 2),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(Material.DIAMOND_HOE)
                    val meta = itemStack.itemMeta
                    meta?.customName(
                        Component.text("농장 전체 수확하고 다시 심기", NamedTextColor.YELLOW)
                    )
                    meta.lore(
                        listOf(
                            Component.text("이용시 농장물을 판매할때 45%의 가격으로 판매합니다.", NamedTextColor.GRAY),
                            Component.text("심으면서 씨앗 가격을 청구합니다.", NamedTextColor.GRAY),
                            Component.text("이용 쿨타임은 1시간 입니다.", NamedTextColor.GRAY),
                        )
                    )
                    itemStack.itemMeta = meta
                    itemStack
                }
                .consumer { event ->
                    val p = event?.let { it.whoClicked as Player }
                    if(p != null) {
                        p.closeInventory()
                        val cooldownOk = cooldown(p, "diamond_hoe_harvest_replant", 1)
                        if(cooldownOk) {
                            p.sendMessage(Component.text("농장 전체 수확을 시작합니다...", NamedTextColor.GREEN))
                            harvestReplantAndGiveMoney(p, 0.45)
                        }
                    }
                }
        )
        this.addButton(slot(7, 2),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(Material.NETHERITE_HOE)
                    val meta = itemStack.itemMeta
                    meta?.customName(
                        Component.text("농장 전체 수확하고 다시 심기", NamedTextColor.YELLOW)
                    )
                    meta.lore(
                        listOf(
                            Component.text("이용시 농장물을 판매할때 75%의 가격으로 판매합니다.", NamedTextColor.GRAY),
                            Component.text("심으면서 씨앗 가격을 청구합니다.", NamedTextColor.GRAY),
                            Component.text("이용 쿨타임은 60시간 입니다.", NamedTextColor.GRAY),
                        )
                    )
                    itemStack.itemMeta = meta
                    itemStack
                }
                .consumer { event ->
                    val p = event?.let { it.whoClicked as Player }
                    if(p != null) {
                        p.closeInventory()
                        val cooldownOk = cooldown(p, "netherite_hoe_harvest_replant", 60)
                        if(cooldownOk) {
                            p.sendMessage(Component.text("농장 전체 수확을 시작합니다...", NamedTextColor.GREEN))
                            harvestReplantAndGiveMoney(p, 0.75)
                        }
                    }
                }
        )

        this.addButton(slot(7, 1),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(Material.EMERALD)
                    val meta = itemStack.itemMeta
                    meta?.customName(
                        Component.text("상점", NamedTextColor.GREEN)
                    )
                    itemStack.itemMeta = meta
                    itemStack
                }
                .consumer { event ->
                    val p = event?.let { it.whoClicked as Player }
                    if(p != null) {
                        p.closeInventory()
                        val gui = Shop(nj)
                        nj.guiManager.openGUI(gui, p)
                    }
                }
        )

        super.decorate(player)
    }
}
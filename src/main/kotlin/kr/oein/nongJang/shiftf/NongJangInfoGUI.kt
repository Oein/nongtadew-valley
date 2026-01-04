package kr.oein.nongJang.shiftf

import kr.oein.interchest.InventoryButton
import kr.oein.interchest.InventoryGUI
import kr.oein.nongJang.NongJang
import kr.oein.nongJang.utils.GetRandomizingPricing
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class NongJangInfoGUI(val nj: NongJang, val chunkX: Int, val chunkZ: Int): InventoryGUI() {
    val njTemperature = nj.chunkManager.getTemperature(chunkX, chunkZ)
    val njHumidity = nj.chunkManager.getHumidity(chunkX, chunkZ)
    val njSoil = nj.chunkManager.getSoil(chunkX, chunkZ)
    val randomizedCount = nj.chunkManager.getRandomizedCount(chunkX, chunkZ)
    val landPrice = nj.chunkManager.getPrice(chunkX, chunkZ)
    val isValidFarm = njTemperature != null && njHumidity != null && landPrice != null && njSoil != null
    val randomizePricing = GetRandomizingPricing.getRandomizingPricing(randomizedCount, landPrice?: 0)

    @Suppress("DEPRECATION")
    override fun createInventory(): org.bukkit.inventory.Inventory {
        return org.bukkit.Bukkit.createInventory(null, 3 * 9, "농장 정보 ($chunkX, $chunkZ)")
    }

    override fun decorate(player: org.bukkit.entity.Player?) {
        this.addButton(
            slot(1, 1),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(
                        if(njTemperature!! > 0) Material.REDSTONE_TORCH else Material.SOUL_TORCH,
                        max(abs(njTemperature), 1)
                    )
                    val meta = itemStack.itemMeta
                    meta?.displayName(Component.text("온도: $njTemperature'C", NamedTextColor.WHITE))
                    meta.lore(
                        listOf(
                            // no decoration gray text
                            Component.text("클릭하여 온도 랜덤화", NamedTextColor.WHITE),
                            Component.text("가격 : ${randomizePricing}원", NamedTextColor.GRAY),
                        )
                    )
                    itemStack.itemMeta = meta

                    itemStack
                }
                .consumer {
                    val p = it?.whoClicked as org.bukkit.entity.Player
                    if(!isValidFarm) {
                        p.sendMessage(Component.text("이 농장은 유효한 농장이 아닙니다.", NamedTextColor.RED, TextDecoration.BOLD))
                        return@consumer
                    }
                    val price = randomizePricing
                    if(nj.moneyManager.getMoney(p) >= price) {
                        nj.moneyManager.removeMoney(p, price)
                        nj.chunkManager.resetTemperature(chunkX, chunkZ)
                        p.closeInventory()
                        val newGui = NongJangInfoGUI(nj, chunkX, chunkZ)
                        nj.guiManager.openGUI(newGui, p)
                        p.sendMessage(Component.text("온도가 랜덤화되었습니다! 새로운 온도를 확인해주세요.", NamedTextColor.GREEN))
                    } else {
                        p.sendMessage(Component.text("잔액이 부족합니다! (필요 금액: ${price}원)", NamedTextColor.RED))
                    }
                }
        )

        this.addButton(
            slot(2, 1),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(
                        Material.WATER_BUCKET,
                        1
                    )
                    val meta = itemStack.itemMeta
                    meta.setMaxStackSize(99)
                    meta?.displayName(Component.text("습도: $njHumidity%", NamedTextColor.WHITE))
                    meta.lore(
                        listOf(
                            Component.text("클릭하여 습도 랜덤화", NamedTextColor.WHITE),
                            Component.text("가격 : ${randomizePricing}원", NamedTextColor.GRAY),
                        )
                    )
                    itemStack.itemMeta = meta
                    itemStack.amount = min(max(njHumidity ?: 1, 1), 99)

                    itemStack
                }
                .consumer {
                    val p = it?.whoClicked as org.bukkit.entity.Player
                    if(!isValidFarm) {
                        p.sendMessage(Component.text("이 농장은 유효한 농장이 아닙니다.", NamedTextColor.RED, TextDecoration.BOLD))
                        return@consumer
                    }
                    val price = randomizePricing
                    if(nj.moneyManager.getMoney(p) >= price) {
                        nj.moneyManager.removeMoney(p, price)

                        nj.chunkManager.resetHumidity(chunkX, chunkZ)
                        p.closeInventory()
                        val newGui = NongJangInfoGUI(nj, chunkX, chunkZ)
                        nj.guiManager.openGUI(newGui, p)
                        p.sendMessage(Component.text("습도가 랜덤화되었습니다! 새로운 습도를 확인해주세요.", NamedTextColor.GREEN))
                    } else {
                        p.sendMessage(Component.text("잔액이 부족합니다! (필요 금액: ${price}원)", NamedTextColor.RED))
                    }
                }
        )

        this.addButton(
            slot(3, 1),
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(
                        Material.FARMLAND,
                        1
                    )
                    val meta = itemStack.itemMeta
                    meta.setMaxStackSize(99)
                    meta?.displayName(Component.text("비옥도: $njSoil%", NamedTextColor.WHITE))
                    meta.lore(
                        listOf(
                            Component.text("클릭하여 비옥도 랜덤화", NamedTextColor.WHITE),
                            Component.text("가격 : ${randomizePricing}원", NamedTextColor.GRAY),
                        )
                    )
                    itemStack.itemMeta = meta
                    itemStack.amount = min(max(njSoil ?: 1, 1), 99)

                    itemStack
                }
                .consumer {
                    val p = it?.whoClicked as org.bukkit.entity.Player
                    if(!isValidFarm) {
                        p.sendMessage(Component.text("이 농장은 유효한 농장이 아닙니다.", NamedTextColor.RED, TextDecoration.BOLD))
                        return@consumer
                    }
                    val price = randomizePricing
                    if(nj.moneyManager.getMoney(p) >= price) {
                        nj.moneyManager.removeMoney(p, price)
                        nj.chunkManager.resetSoil(chunkX, chunkZ)
                        p.closeInventory()
                        val newGui = NongJangInfoGUI(nj, chunkX, chunkZ)
                        nj.guiManager.openGUI(newGui, p)
                        p.sendMessage(Component.text("비옥도가 랜덤화되었습니다! 새로운 비옥도를 확인해주세요.", NamedTextColor.GREEN))
                    } else {
                        p.sendMessage(Component.text("잔액이 부족합니다! (필요 금액: ${price}원)", NamedTextColor.RED))
                    }
                }
        )


        super.decorate(player)
    }

    override fun onOpen(event: InventoryOpenEvent) {
        if(!isValidFarm) {
            event.player.sendMessage("§c이 농장은 유효한 농장이 아닙니다.")
            this.inventory.close()
            return
        }
        super.onOpen(event)
    }
}
package kr.oein.nongJang.shiftf

import kr.oein.interchest.InventoryGUI
import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class Shop(val nj: NongJang): InventoryGUI() {
    @Suppress("DEPRECATION")
    override fun createInventory(): Inventory {
        return Bukkit.createInventory(null, 6 * 9, "상점")
    }

    val shopItems: MutableList<Pair<
            ItemStack, Int // item, price
    >> = mutableListOf(
        Pair(
            ItemStack(org.bukkit.Material.DIAMOND, 1),
            1000
        )
    )

    override fun decorate(player: org.bukkit.entity.Player?) {
        // loop in items
        for((index, item) in shopItems.withIndex()) {
            val slot = index
            this.addButton(slot,
                kr.oein.interchest.InventoryButton()
                    .creator {
                        val itemStack = item.first.clone()
                        val meta = itemStack.itemMeta
                        meta?.lore(
                            listOf(
                                Component.text("가격: ${item.second} 농장 코인", net.kyori.adventure.text.format.NamedTextColor.GOLD)
                            )
                        )
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }

        super.decorate(player)
    }
}
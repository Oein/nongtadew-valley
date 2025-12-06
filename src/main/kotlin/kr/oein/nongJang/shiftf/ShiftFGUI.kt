package kr.oein.nongJang.shiftf

import kr.oein.interchest.InventoryButton
import kr.oein.interchest.InventoryGUI
import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class ShiftFGUI(val nongJang: NongJang): InventoryGUI() {
    override fun createInventory(): Inventory {
        return Bukkit.createInventory(null, 3 * 9, "농타듀벨리 Shift-F 메뉴")
    }

    override fun decorate(player: Player?) {
        // set 11 to grass block
        this.addButton(11,
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
                        val world = Bukkit.getWorld("world")
                        if (world != null) {
                            p.teleport(world.spawnLocation)
                        }
                    }
                }
        )

        this.addButton(13,
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
                        val gui = MyNongJangGUI(nongJang, p, 0)
                        nongJang.guiManager.openGUI(gui, p)
                    }
                }
        )
        this.addButton(15,
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
                        val gui = PurchaseNongJangGUI(nongJang, p, 0, 0)
                        nongJang.guiManager.openGUI(gui, p)
                    }
                }
        )

        super.decorate(player)
    }
}
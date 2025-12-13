package kr.oein.nongJang.shiftf

import kr.oein.interchest.InventoryButton
import kr.oein.interchest.InventoryGUI
import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil

class MyNongJangGUI(val nongJang: NongJang, val player: Player, val page: Int = 0): InventoryGUI() {
    val myNongJangs = nongJang.chunkManager.getMyChunks(player)

    val itemsInPage = 9 * 5
    val startIndex = page * itemsInPage
    val allPages = ceil(myNongJangs.size.toDouble() / itemsInPage).toInt()
    @Suppress("DEPRECATION")
    override fun createInventory(): Inventory {
        return Bukkit.createInventory(null, 6 * 9, "내 농장 보기 - 페이지 (${page + 1}/${allPages})")
    }

    override fun decorate(player: Player?) {
        if(page > 0) {
            this.addButton(45,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.PINK_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta?.customName(
                            Component.text("이전 페이지", NamedTextColor.YELLOW)
                        )
                        itemStack.itemMeta = meta
                        itemStack
                    }
                    .consumer { event ->
                        val p = event?.let { it.whoClicked as Player }
                        if(p != null) {
                            p.closeInventory()
                            val gui = MyNongJangGUI(nongJang, p, page - 1)
                            nongJang.guiManager.openGUI(gui, p)
                        }
                    }
            )
        } else {
            this.addButton(45,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.GRAY_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta?.customName(
                            Component.text("이전 페이지 없음", NamedTextColor.DARK_GRAY)
                        )
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }

        for(i in 46 until 53) {
            this.addButton(i,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
                        val meta = itemStack.itemMeta
                        meta?.customName(
                            Component.text(" ", NamedTextColor.WHITE)
                        )
                        itemStack.itemMeta.isHideTooltip = true
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }

        if(page < allPages - 1) {
            this.addButton(53,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.PINK_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta?.customName(
                            Component.text("다음 페이지", NamedTextColor.YELLOW)
                        )
                        itemStack.itemMeta = meta
                        itemStack
                    }
                    .consumer { event ->
                        val p = event?.let { it.whoClicked as Player }
                        if(p != null) {
                            p.closeInventory()
                            val gui = MyNongJangGUI(nongJang, p, page + 1)
                            nongJang.guiManager.openGUI(gui, p)
                        }
                    }
            )
        } else {
            this.addButton(53,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.GRAY_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta?.customName(
                            Component.text("다음 페이지 없음", NamedTextColor.DARK_GRAY)
                        )
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }

        for(i in 0 until itemsInPage) {
            val index = startIndex + i
            if(index >= myNongJangs.size) {
                break
            }
            val chunk = myNongJangs[index]
            val chunkX = chunk.first
            val chunkZ = chunk.second

            this.addButton(i,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.FARMLAND)
                        val meta = itemStack.itemMeta
                        meta?.customName(
                            Component.text("농장 좌표: (${chunkX}, ${chunkZ})", NamedTextColor.GREEN)
                        )
                        itemStack.itemMeta = meta
                        itemStack
                    }
                    .consumer { event ->
                        val p = event?.let { it.whoClicked as Player }
                        if(p != null) {
                            p.closeInventory()
                            val world = nongJang.njCommands.ensureNongJangWorld()
                            if (world != null) {
                                val x = (chunkX * 16) + 8
                                val z = (chunkZ * 16) + 8
                                var y = 6
                                for (yCheck in 254 downTo 0) {
                                    val block = world.getBlockAt(x, yCheck, z)
                                    if (block.type != Material.AIR) {
                                        y = (yCheck.toDouble() + 1.0).toInt()
                                        break
                                    }
                                }
                                p.teleport(org.bukkit.Location(world, x.toDouble(), y.toDouble(), z.toDouble()))

                                // show title
                                p.showTitle(
                                    Title.title(
                                        Component.text("농장으로 이동됨!", NamedTextColor.GREEN),
                                        Component.text("좌표: (${chunkX}, ${chunkZ})", NamedTextColor.WHITE)
                                    )
                                )
                            } else {
                                p.sendMessage(Component.text("농장 월드가 설정되지 않았습니다.", NamedTextColor.RED))
                            }
                        }
                    }
            )
        }

        super.decorate(player)
    }
}
package kr.oein.nongJang.shiftf

import kr.oein.interchest.InventoryButton
import kr.oein.interchest.InventoryGUI
import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class PurchaseNongJangGUI(val nongJang: NongJang, val player: Player, val centerX: Int, val centerY: Int): InventoryGUI() {
    val viewWidth = 9 - 2
    val viewHeight = 5 - 2

    val worldRadius = nongJang.chunkManager.T3_radius * 2 + 1

    override fun createInventory(): Inventory {
        return Bukkit.createInventory(null, 5 * 9, "농장 구입하기")
    }

    override fun decorate(player: Player?) {
        // ____UP____
        // |         |
        // |         | <- viewHeight
        // |         |
        // |__DOWN___|
        // <-viewWidth->

        // add up button at slot 4
        if(centerY + viewHeight <= worldRadius) {
            this.addButton(
                4,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.PINK_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("위로 이동"))
                        itemStack.itemMeta = meta
                        itemStack
                    }
                    .consumer { event ->
                        val p = event?.let { it.whoClicked as Player }
                        if (p != null) {
                            p.closeInventory()
                            val gui = PurchaseNongJangGUI(nongJang, p, centerX, centerY + viewHeight)
                            nongJang.guiManager.openGUI(gui, p)
                        }
                    }
            )
        } else {
            this.addButton(
                4,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.GRAY_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("더 이상 위로 이동할 수 없습니다."))
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }

        // add up button at slot 5
        if(centerY + viewHeight * 4 <= worldRadius) {
            this.addButton(
                5,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.PINK_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("위로 4페이지 이동"))
                        itemStack.itemMeta = meta
                        itemStack
                    }
                    .consumer { event ->
                        val p = event?.let { it.whoClicked as Player }
                        if (p != null) {
                            p.closeInventory()
                            val gui = PurchaseNongJangGUI(nongJang, p, centerX, centerY + viewHeight * 4)
                            nongJang.guiManager.openGUI(gui, p)
                        }
                    }
            )
        } else {
            this.addButton(
                4,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.GRAY_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("더 이상 위로 이동할 수 없습니다."))
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }

        // add down button at slot 45 + 4
        if(centerY - viewHeight >= -worldRadius) {
            this.addButton(9 * 4 + 4,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.PINK_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("아래로 이동"))
                        itemStack.itemMeta = meta
                        itemStack
                    }
                    .consumer { event ->
                        val p = event?.let { it.whoClicked as Player }
                        if (p != null) {
                            p.closeInventory()
                            val gui = PurchaseNongJangGUI(nongJang, p, centerX, centerY - viewHeight)
                            nongJang.guiManager.openGUI(gui, p)
                        }
                    }
            )
        } else {
            this.addButton(
                9 * 4 + 4,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.GRAY_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("더 이상 아래로 이동할 수 없습니다."))
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }


        // add down button at slot 45 + 5
        if(centerY - viewHeight * 4 >= -worldRadius) {
            this.addButton(9 * 4 + 5,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.PINK_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("아래로 4페이지 이동"))
                        itemStack.itemMeta = meta
                        itemStack
                    }
                    .consumer { event ->
                        val p = event?.let { it.whoClicked as Player }
                        if (p != null) {
                            p.closeInventory()
                            val gui = PurchaseNongJangGUI(nongJang, p, centerX, centerY - viewHeight * 4)
                            nongJang.guiManager.openGUI(gui, p)
                        }
                    }
            )
        } else {
            this.addButton(
                9 * 4 + 5,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.GRAY_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("더 이상 아래로 이동할 수 없습니다."))
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }

        // add left button at slot 18
        if(centerX - viewWidth >= -worldRadius) {
            this.addButton(9 * 2,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.PINK_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("왼쪽으로 이동"))
                        itemStack.itemMeta = meta
                        itemStack
                    }
                    .consumer { event ->
                        val p = event?.let { it.whoClicked as Player }
                        if (p != null) {
                            p.closeInventory()
                            val gui = PurchaseNongJangGUI(nongJang, p, centerX - viewWidth, centerY)
                            nongJang.guiManager.openGUI(gui, p)
                        }
                    }
            )
        } else {
            this.addButton(
                9 * 2,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.GRAY_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("더 이상 왼쪽으로 이동할 수 없습니다."))
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }

        // add left button at slot 27
        if(centerX - viewWidth * 4 >= -worldRadius) {
            this.addButton(9 * 3,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.PINK_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("왼쪽으로 4페이지 이동"))
                        itemStack.itemMeta = meta
                        itemStack
                    }
                    .consumer { event ->
                        val p = event?.let { it.whoClicked as Player }
                        if (p != null) {
                            p.closeInventory()
                            val gui = PurchaseNongJangGUI(nongJang, p, centerX - viewWidth * 4, centerY)
                            nongJang.guiManager.openGUI(gui, p)
                        }
                    }
            )
        } else {
            this.addButton(
                9 * 3,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.GRAY_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("더 이상 왼쪽으로 이동할 수 없습니다."))
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }

        // add right button at slot 26
        if(centerX + viewWidth <= worldRadius) {
            this.addButton(
                9 * 3 - 1,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.PINK_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("오른쪽으로 이동"))
                        itemStack.itemMeta = meta
                        itemStack
                    }
                    .consumer { event ->
                        val p = event?.let { it.whoClicked as Player }
                        if (p != null) {
                            p.closeInventory()
                            val gui = PurchaseNongJangGUI(nongJang, p, centerX + viewWidth, centerY)
                            nongJang.guiManager.openGUI(gui, p)
                        }
                    }
            )
        } else {
            this.addButton(
                9 * 3 - 1,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.GRAY_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("더 이상 오른쪽으로 이동할 수 없습니다."))
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }


        // add right button at slot 35
        if(centerX + viewWidth * 4 <= worldRadius) {
            this.addButton(
                9 * 4 - 1,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.PINK_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("오른쪽으로 4페이지 이동"))
                        itemStack.itemMeta = meta
                        itemStack
                    }
                    .consumer { event ->
                        val p = event?.let { it.whoClicked as Player }
                        if (p != null) {
                            p.closeInventory()
                            val gui = PurchaseNongJangGUI(nongJang, p, centerX + viewWidth * 4, centerY)
                            nongJang.guiManager.openGUI(gui, p)
                        }
                    }
            )
        } else {
            this.addButton(
                9 * 4 - 1,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.GRAY_GLAZED_TERRACOTTA)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text("더 이상 오른쪽으로 이동할 수 없습니다."))
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }

        for(x in -viewWidth/2 until viewWidth/2 + 1) {
            for(y in -viewHeight/2 until viewHeight/2 + 1) {
                var slot = ((y + viewHeight/2 + 1) * 9) + (x + viewWidth/2 + 1)
                val yRevSlot = ((-y + viewHeight/2 + 1) * 9) + (x + viewWidth/2 + 1)
                slot = yRevSlot
                val worldX = centerX + x
                val worldY = centerY + y
                val owner = nongJang.chunkManager.getOwner(worldX, worldY)
                val isPurchased = owner != null
                val price = nongJang.chunkManager.getPrice(worldX, worldY) ?: continue

                this.addButton(slot,
                    InventoryButton()
                        .creator {
                            val itemStack = if (isPurchased) {
                                ItemStack(Material.PLAYER_HEAD)
                            } else {
                                ItemStack(Material.GREEN_TERRACOTTA)
                            }
                            val meta = itemStack.itemMeta

                            if (isPurchased) {
                                meta.customName(Component.text("구입 불가 (${worldX}, ${worldY})"))
                            } else {
                                meta.customName(Component.text("구입 가능 (${worldX}, ${worldY}) - ${price}원"))
                            }
                            if(isPurchased) {
                                meta.lore(listOf(
                                    Component.text("소유주: ${owner.name}")
                                ))
                                // set skull owner
                                val skullMeta = meta as org.bukkit.inventory.meta.SkullMeta
                                skullMeta.owningPlayer = owner
                            } else {
                                meta.lore(listOf(
                                    Component.text("클릭하여 농장 구입")
                                ))
                            }
                            itemStack.itemMeta = meta
                            itemStack
                        }
                        .consumer { event ->
                            val p = event?.let { it.whoClicked as Player }
                            if (p == null) return@consumer
                            if (isPurchased) {
                                p.sendMessage {
                                    Component.text("이미 구입된 농장입니다.", net.kyori.adventure.text.format.NamedTextColor.RED)
                                }
                                return@consumer
                            }
                            val res = nongJang.chunkManager.purchaseChunk(worldX, worldY, p)
                            if(res) {
                                p.sendMessage {
                                    Component.text("농장 구입에 성공했습니다! (${worldX}, ${worldY})", net.kyori.adventure.text.format.NamedTextColor.GREEN)
                                }
                            } else {
                                p.sendMessage {
                                    Component.text("농장 구입에 실패했습니다. 잔액을 확인해주세요. (${worldX}, ${worldY})", net.kyori.adventure.text.format.NamedTextColor.RED)
                                }
                            }

                            // reopen the GUI
                            p.closeInventory()
                            val gui = PurchaseNongJangGUI(nongJang, p, centerX, centerY)
                            nongJang.guiManager.openGUI(gui, p)
                        }
                )
            }
        }

        // empty panes
        val emptyPanes = listOf<Int>(
            0,1,2,3,  6,7,8,
            9, 17,


            36, 37, 38, 39,  42, 43, 44,
        )
        for(slot in emptyPanes) {
            this.addButton(slot,
                InventoryButton()
                    .creator {
                        val itemStack = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
                        val meta = itemStack.itemMeta
                        meta.customName(Component.text(" "))
                        meta.isHideTooltip = true
                        itemStack.itemMeta = meta
                        itemStack
                    }
            )
        }

        super.decorate(player)
    }
}
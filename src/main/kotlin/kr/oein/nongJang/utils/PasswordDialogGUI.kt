package kr.oein.nongJang.utils

import kr.oein.interchest.InventoryButton
import kr.oein.interchest.InventoryGUI
import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Light
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class PasswordDialogGUI(val nj: NongJang): InventoryGUI() {
    var passwordInput: String = ""

    @Suppress("DEPRECATION")
    override fun createInventory(): Inventory {
        return Bukkit.createInventory(null, 6 * 9, "안전번호를 입력해주세요")
    }

    // use 10 for *
    // use 11 for backspace
    fun createLightBulb(number: Int): ItemStack {
        val cbd = "number_input_$number"
        val itemStack = ItemStack(Material.LIGHT, 1)
        val meta = itemStack.itemMeta
        meta?.customName(
            Component.text(
                when(number) {
                    10 -> "*"
                    11 -> "←"
                    else -> number.toString()
                }
                , NamedTextColor.YELLOW)
        )

        val cbdComponent = meta.customModelDataComponent
        cbdComponent.strings = listOf(cbd)
        meta.setCustomModelDataComponent(cbdComponent)


        // set light level
        // no block data for itemstack


        itemStack.itemMeta = meta

        return itemStack
    }

    val keypad = listOf(
        listOf(7, 8, 9),
        listOf(4, 5, 6),
        listOf(1, 2, 3),
        listOf(0, 10, 11)
    )

    override fun decorate(player: Player?) {
        for(x in 3..5) {
            for(y in 2..5) {
                val slot = y * 9 + x
                val number = keypad[y - 2][x - 3]
                this.addButton(slot,
                    InventoryButton()
                        .creator {
                            createLightBulb(number)
                        }
                        .consumer { event ->
                            val p = event?.let { it.whoClicked as Player }
                            if(p != null) {
                                if(number == 11) {
                                    // backspace
                                    if(passwordInput.isNotEmpty()) {
                                        passwordInput = passwordInput.dropLast(1)
                                    }
                                } else {
                                    passwordInput += if(number == 10) "*" else number.toString()
                                }

                                p.sendMessage("현재 입력된 안전번호: $passwordInput")
                            }
                        }
                )
            }
        }

        this.addButton(9 * 6 - 1,
            InventoryButton()
                .creator {
                    val itemStack = ItemStack(Material.RED_WOOL)
                    val meta = itemStack.itemMeta
                    meta?.customName(
                        Component.text("서버 나가기", NamedTextColor.RED)
                    )
                    itemStack.itemMeta = meta
                    itemStack
                }
                .consumer { event ->
                    val p = event?.let { it.whoClicked as Player }
                    p?.kick(Component.text("안전번호 입력을 취소하고 서버를 나갔습니다.", NamedTextColor.RED))
                }
        )

        super.decorate(player);
    }

    override fun onClose(event: InventoryCloseEvent?) {
        if (event == null) return
        nj.server.scheduler.runTaskLater(nj, { ->
            nj.guiManager.openGUI(PasswordDialogGUI(nj), event.player as Player);
        }, 1L)
    }
}
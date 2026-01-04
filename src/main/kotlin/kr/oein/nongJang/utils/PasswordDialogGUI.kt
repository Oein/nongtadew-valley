package kr.oein.nongJang.utils

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.BlockItemDataProperties
import kr.oein.interchest.InventoryButton
import kr.oein.interchest.InventoryGUI
import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.data.type.Light
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class PasswordDialogGUI(val nj: NongJang): InventoryGUI() {
    var passwordInput: String = ""

    @Suppress("DEPRECATION")
    override fun createInventory(): Inventory {
        return Bukkit.createInventory(null, 6 * 9, "안전번호를 입력해주세요 (4자리)")
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

        itemStack.itemMeta = meta

        val bData = Bukkit.createBlockData(Material.LIGHT) as Light
        bData.level = number


        return itemStack
    }

    val keypad = listOf(
        listOf(7, 8, 9),
        listOf(4, 5, 6),
        listOf(1, 2, 3),
        listOf(0, 10, 11)
    )

    fun showPlayerTutorialMessage(player: Player) {
        player.sendMessage(Component.text("#### 농타듀벨리 ###", NamedTextColor.GREEN))
        player.sendMessage(Component.text("농타듀벨리에 오신것을 환영합니다."))
        player.sendMessage(Component.text("Shift-F(웅크리기+왼손들기)를 통해 메뉴에 접근할 수 있습니다."))
    }

    val authScope = nj.kvdb.loadScope("auth")
    override fun decorate(player: Player?) {
        player?.playSound(
            player.location,
            "minecraft:custom.pw_input",
            1.0f, // volume
            1.0f  // pitch
        )

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
                                    if (passwordInput.isNotEmpty())
                                        passwordInput = passwordInput.dropLast(1)

                                    player?.playSound(
                                        player.location,
                                        "minecraft:custom.pw_remove",
                                        1.0f, // volume
                                        1.0f  // pitch
                                    )
                                } else {
                                    passwordInput += if (number == 10) "*" else number.toString()
                                    player?.playSound(
                                        player.location,
                                        "minecraft:entity.experience_orb.pickup",
                                        1.0f, // volume
                                        1.0f  // pitch
                                    )
                                }

                                p.sendMessage("현재 입력된 안전번호: $passwordInput")
                                if (passwordInput.length == 4) {
                                    // if new
                                    val pw = authScope.get("password_${player!!.uniqueId}")
                                    if(pw == null) {
                                        authScope.set("password_${player.uniqueId}", passwordInput);
                                        nj.blockInteractionLobbyWorld.nonProcessedUsers.remove(player);
                                        nj.blockInteractionLobbyWorld.sessionData[player.uniqueId.toString()] = PlayerSessionData(
                                            player = player,
                                            password = passwordInput,
                                            discordId = null,
                                            discordOauthToken = null
                                        );
                                        // close gui
                                        p.closeInventory()
                                        p.sendMessage(Component.text("안전번호가 설정되었습니다!", NamedTextColor.GREEN))
                                        showPlayerTutorialMessage(player)
                                    }
                                    else if(passwordInput == pw){
                                        nj.blockInteractionLobbyWorld.nonProcessedUsers.remove(player);
                                        nj.blockInteractionLobbyWorld.sessionData[player.uniqueId.toString()] = PlayerSessionData(
                                            player = player,
                                            password = passwordInput,
                                            discordId = null,
                                            discordOauthToken = null
                                        );
                                        p.closeInventory()
                                        p.sendMessage(Component.text("안전번호가 확인되었습니다!", NamedTextColor.GREEN))

                                        player.playSound(
                                            player.location,
                                            "minecraft:custom.correct_pw",
                                            1.0f, // volume
                                            1.0f  // pitch
                                        )
                                        showPlayerTutorialMessage(player)
                                    } else {
                                        p.sendMessage(Component.text("안전번호가 일치하지 않습니다. 다시 시도해주세요.", NamedTextColor.RED))
                                        player.playSound(
                                            player.location,
                                            "minecraft:custom.wrong_pw",
                                            1.0f, // volume
                                            1.0f  // pitch
                                        )
//                                        player.playSound(
//                                            player.location,
//                                            "minecraft:custom.pw_wrong_sfx",
//                                            1.0f, // volume
//                                            1.0f  // pitch
//                                        )

                                        // reset input
                                        passwordInput = ""
                                    }
                                }
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
        // check is non authenticated user
        val player = event.player as Player
        if (!nj.blockInteractionLobbyWorld.nonProcessedUsers.contains(player)) return
        nj.server.scheduler.runTaskLater(nj, { ->
            nj.guiManager.openGUI(PasswordDialogGUI(nj), event.player as Player);
        }, 1L)
    }
}
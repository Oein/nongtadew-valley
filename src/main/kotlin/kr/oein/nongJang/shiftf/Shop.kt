package kr.oein.nongJang.shiftf

import kr.oein.interchest.InventoryButton
import kr.oein.interchest.InventoryGUI
import kr.oein.nongJang.NongJang
import kr.oein.nongJang.farm.FarmConfig
import kr.oein.nongJang.farm.Grow
import kr.oein.nongJang.farm.HarvestedLevel
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import kotlin.math.min

class Shop(val nj: NongJang, val page: Int = 0): InventoryGUI() {
    @Suppress("DEPRECATION")
    override fun createInventory(): Inventory {
        return Bukkit.createInventory(null, 6 * 9, "상점")
    }

    fun itemWithLore(item: ItemStack, price: Long): ItemStack {
        val itemStack = item.clone()
        val meta = itemStack.itemMeta
        meta?.lore(
            listOf(
                Component.text("판매 가격: ${price} 농장 코인", NamedTextColor.GOLD)
            )
        )
        itemStack.itemMeta = meta
        return itemStack
    }

    fun checkInventory(items: List<ItemStack>, player: org.bukkit.entity.Player): Boolean {
        val inv = player.inventory
        val itemCountMap = mutableMapOf<ItemStack, Int>()
        for(item in items) {
            val cloned = item.clone()
            cloned.amount = 1
            itemCountMap[cloned] = itemCountMap.getOrDefault(cloned, 0) + item.amount
        }

        for((mat, neededCount) in itemCountMap) {
            var foundCount = 0
            for(i in 0 until inv.size) {
                val invItem = inv.getItem(i)
                val clonedInv = invItem?.clone()
                clonedInv?.let { it.amount = 1 }

                if (invItem != null && clonedInv == mat) {
                    foundCount += invItem.amount
                    if(foundCount >= neededCount) {
                        break
                    }
                }
            }
            if(foundCount < neededCount) {
                return false
            }
        }
        return true
    }

    fun removeInventory(items: List<ItemStack>, player: org.bukkit.entity.Player) {
        val inv = player.inventory
        val itemCountMap = mutableMapOf<ItemStack, Int>()
        for(item in items) {
            val mat = item.clone()
            mat.amount = 1
            itemCountMap[mat] = itemCountMap.getOrDefault(mat, 0) + item.amount
        }

        for((mat, neededCount) in itemCountMap) {
            var toRemove = neededCount
            for(i in 0 until inv.size) {
                val invItem = inv.getItem(i)
                val clonedInv = invItem?.clone()
                clonedInv?.let { it.amount = 1 }
                if (invItem != null && clonedInv == mat) {
                    val invItemAmount = invItem.amount
                    if(invItemAmount <= toRemove) {
                        inv.setItem(i, null)
                        toRemove -= invItemAmount
                    } else {
                        invItem.amount = invItemAmount - toRemove
                        inv.setItem(i, invItem)
                        toRemove = 0
                    }
                    if(toRemove <= 0) {
                        break
                    }
                }
            }
        }
    }

    fun item2coinShopItem(material: Material, amount: Int, price: Long): ShopItem {
        return ShopItem()
            .creator {
                itemWithLore(ItemStack(material, amount), price)
            }
            .canPurchase { player ->
                checkInventory(
                    listOf(
                        ItemStack(material, amount)
                    ),
                    player
                )
            }
            .purchase { player ->
                removeInventory(listOf(
                    ItemStack(material, amount)
                ), player)
                nj.moneyManager.addMoney(player, price)
            }
    }

    fun itemStack2coinShopItem(itemStack: ItemStack, price: Long): ShopItem {
        return ShopItem()
            .creator {
                val nItemStack = itemStack.clone()
                val meta = nItemStack.itemMeta
                meta?.lore(
                    listOf(
                        Component.text("판매 가격: $price 농장 코인", NamedTextColor.GOLD)
                    )
                )
                nItemStack.itemMeta = meta
                nItemStack
            }
            .canPurchase { player ->
                checkInventory(
                    listOf(
                        itemStack
                    ),
                    player
                )
            }
            .purchase { player ->
                removeInventory(listOf(
                    itemStack
                ), player)
                nj.moneyManager.addMoney(player, price)
            }
    }

    fun coin2itemShopItem(itemStack: ItemStack, price: Long): ShopItem {
        return ShopItem()
            .creator {
                val nItemStack = itemStack.clone()
                val meta = nItemStack.itemMeta
                meta?.lore(
                    listOf(
                        Component.text("구매 가격: $price 농장 코인", NamedTextColor.GOLD)
                    )
                )
                nItemStack.itemMeta = meta
                nItemStack
            }
            .canPurchase { player ->
                nj.moneyManager.getMoney(player) >= price
            }
            .purchase { player ->
                nj.moneyManager.removeMoney(player, price)
                val inv = player.inventory
                inv.addItem(itemStack)
            }
    }

    fun itemStackWithEnchant(material: Material, amount: Int, enchantment: Enchantment, level: Int): ItemStack {
        val itemStack = ItemStack(material, amount)
        val meta = itemStack.itemMeta
        meta?.addEnchant(enchantment, level, true)
        itemStack.itemMeta = meta
        return itemStack
    }

    var shopItems = mutableListOf<ShopItem>(
        item2coinShopItem(Material.COAL_ORE, 1, 10L),
        item2coinShopItem(Material.DEEPSLATE_COAL_ORE, 1, 10L),
        item2coinShopItem(Material.COPPER_ORE, 1, 15L),
        item2coinShopItem(Material.DEEPSLATE_COPPER_ORE, 1, 15L),
        item2coinShopItem(Material.IRON_ORE, 1, 25L),
        item2coinShopItem(Material.DEEPSLATE_IRON_ORE, 1, 25L),
        item2coinShopItem(Material.GOLD_ORE, 1, 20L),
        item2coinShopItem(Material.DEEPSLATE_GOLD_ORE, 1, 20L),
        item2coinShopItem(Material.DIAMOND_ORE, 1, 100L),
        item2coinShopItem(Material.DEEPSLATE_DIAMOND_ORE, 1, 100L),
        item2coinShopItem(Material.LAPIS_ORE, 1, 25L),
        item2coinShopItem(Material.DEEPSLATE_LAPIS_ORE, 1, 25L),
        item2coinShopItem(Material.EMERALD_ORE, 1, 77L),
        item2coinShopItem(Material.DEEPSLATE_EMERALD_ORE, 1, 77L),
        item2coinShopItem(Material.REDSTONE_ORE, 1, 14L),
        item2coinShopItem(Material.DEEPSLATE_REDSTONE_ORE, 1, 14L),
        item2coinShopItem(Material.NETHERITE_INGOT, 1, 700L),
        coin2itemShopItem(
            itemStackWithEnchant(Material.WOODEN_PICKAXE, 1, Enchantment.SILK_TOUCH, 1),
            0L
        ),
        coin2itemShopItem(
            itemStackWithEnchant(Material.STONE_PICKAXE, 1, Enchantment.SILK_TOUCH, 1),
            5L
        ),
        coin2itemShopItem(
            itemStackWithEnchant(Material.IRON_PICKAXE, 1, Enchantment.SILK_TOUCH, 1),
            50L
        ),
        coin2itemShopItem(
            itemStackWithEnchant(Material.DIAMOND_PICKAXE, 1, Enchantment.SILK_TOUCH, 1),
            500L
        ),
        coin2itemShopItem(
            ItemStack(Material.BREAD, 64),
            3000L
        )
    )

    init {
        FarmConfig.products.forEach { product ->
            shopItems.add(
                coin2itemShopItem(
                    Grow(nj).createSeedItem(product.id),
                    product.seedPrice
                )
            )
        }


        FarmConfig.products.forEach { product ->
            Grow(nj).createHarvestedItem(product.id, HarvestedLevel.MATURE)?.let {
                shopItems.add(
                    itemStack2coinShopItem(
                        it,
                        nj.productPrice.getPrice(product.id),
                    )
                )
            }
        }
    }

    override fun decorate(player: org.bukkit.entity.Player?) {
        // loop in items
        // 5 * 9 items in page
        val itemsInPage = 5 * 9
        val startIndex = page * itemsInPage
        val endIndex = min(startIndex + itemsInPage, shopItems.size)
        for(index in 0 until (endIndex - startIndex)) {
            val item = shopItems[startIndex + index]
            this.addButton(
                index,
                InventoryButton()
                    .creator { p ->
                        item.iconCreator!!.apply(player)
                    }
                    .consumer {
                        val p = it?.whoClicked as org.bukkit.entity.Player
                        if(item.canPurchase!!.apply(p)) {
                            item.purchase!!.apply(p)
                            p.sendMessage(Component.text("거래에 성공했습니다!", NamedTextColor.GREEN))
                        } else {
                            p.sendMessage(Component.text("거래에 필요한 아이템이 부족합니다.", NamedTextColor.RED))
                        }
                    }
            )
        }

        // add pagination buttons
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
                        val p = event?.let { it.whoClicked as org.bukkit.entity.Player }
                        if(p != null) {
                            p.closeInventory()
                            val gui = Shop(nj, page - 1)
                            nj.guiManager.openGUI(gui, p)
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

        if(endIndex < shopItems.size) {
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
                        val p = event?.let { it.whoClicked as org.bukkit.entity.Player }
                        if(p != null) {
                            p.closeInventory()
                            val gui = Shop(nj, page + 1)
                            nj.guiManager.openGUI(gui, p)
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

        super.decorate(player)
    }
}
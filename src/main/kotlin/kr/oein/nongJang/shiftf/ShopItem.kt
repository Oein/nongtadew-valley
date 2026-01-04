package kr.oein.nongJang.shiftf

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Function

class ShopItem {
    var iconCreator: Function<Player?, ItemStack>? = null
        private set
    var canPurchase: Function<Player, Boolean>? = null
        private set
    var purchase: Function<Player, Unit>? = null

    fun creator(iconCreator: Function<Player?, ItemStack>?): ShopItem {
        this.iconCreator = iconCreator
        return this
    }

    fun canPurchase(canPurchase: Function<Player, Boolean>?): ShopItem {
        this.canPurchase = canPurchase
        return this
    }

    fun purchase(purchase: Function<Player, Unit>?): ShopItem {
        this.purchase = purchase
        return this
    }
}
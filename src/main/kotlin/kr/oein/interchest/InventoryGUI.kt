package kr.oein.interchest

import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.inventory.Inventory


abstract class InventoryGUI : InventoryHandler {
    open val inventory: Inventory by lazy(LazyThreadSafetyMode.NONE) {
        createInventory()
    }
    private val buttonMap = HashMap<Int?, InventoryButton?>()

    fun addButton(slot: Int, button: InventoryButton?) {
        this.buttonMap[slot] = button
    }

    open fun decorate(player: Player?) {
        this.buttonMap.forEach { (slot: Int?, button: InventoryButton?) ->
            val icon = button!!.iconCreator!!.apply(player)
            this.inventory.setItem(slot!!, icon)
        }
    }

    fun slot(x: Int, y: Int): Int {
        return y * 9 + x
    }

    override fun onClick(event: InventoryClickEvent) {
        event.isCancelled = true
        val slot = event.slot
        val button = this.buttonMap[slot]
        if (button != null)
            button.eventConsumer!!.accept(event)
    }

    override fun onOpen(event: InventoryOpenEvent) {
        this.decorate(event.player as Player)
    }

    override fun onClose(event: InventoryCloseEvent?) {
    }

    protected abstract fun createInventory(): Inventory
}
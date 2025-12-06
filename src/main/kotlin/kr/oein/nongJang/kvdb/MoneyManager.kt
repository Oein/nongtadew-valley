package kr.oein.nongJang.kvdb

import org.bukkit.entity.Player
import java.lang.Long.parseLong

class MoneyManager(val kvdb: KVDB) {
    val moneyScope = kvdb.loadScope("money")
    val debitScope = kvdb.loadScope("debit")

    fun getMoney(player: Player): Long {
        moneyScope.get(player.uniqueId.toString())?.let {
            return parseLong(it.toString())
        }
        return 0L
    }

    fun setMoney(player: Player, amount: Long) {
        moneyScope.set(player.uniqueId.toString(), amount.toString())
    }

    fun addMoney(player: Player, amount: Long) {
        val currentMoney = getMoney(player)
        setMoney(player, currentMoney + amount)
    }

    fun removeMoney(player: Player, amount: Long) {
        val currentMoney = getMoney(player)
        setMoney(player, currentMoney - amount)
    }

    fun getDebit(player: Player): Long {
        debitScope.get(player.uniqueId.toString())?.let {
            return parseLong(it.toString())
        }
        return 0L
    }

    fun setDebit(player: Player, amount: Long) {
        debitScope.set(player.uniqueId.toString(), amount.toString())
    }

    fun addDebit(player: Player, amount: Long) {
        val currentDebit = getDebit(player)
        setDebit(player, currentDebit + amount)
    }

    fun removeDebit(player: Player, amount: Long) {
        val currentDebit = getDebit(player)
        setDebit(player, currentDebit - amount)
    }
}
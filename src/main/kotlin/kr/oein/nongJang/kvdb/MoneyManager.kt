package kr.oein.nongJang.kvdb

import org.bukkit.entity.Player
import java.lang.Long.parseLong

class MoneyManager(kvdb: KVDB) {
    val moneyScope = kvdb.loadScope("money")

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
}
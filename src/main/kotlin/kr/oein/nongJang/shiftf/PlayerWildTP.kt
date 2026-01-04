package kr.oein.nongJang.shiftf

import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player

class PlayerWildTP(val nj: NongJang) {
    val lastWildScope = nj.kvdb.loadScope("last_wild")

    fun playerWildWorld(player: Player, forceRandom: Boolean = false) {
        val world = Bukkit.getWorld("world")
        if (world != null) {
            if(lastWildScope.has(player.uniqueId.toString()) && !forceRandom) {
                val lastWildData = lastWildScope.get(player.uniqueId.toString())!!.toString().split(",")
                val lastX = lastWildData[0].toDouble()
                val lastY = lastWildData[1].toDouble()
                val lastZ = lastWildData[2].toDouble()
                val lastLocation = org.bukkit.Location(world, lastX, lastY, lastZ)
                player.sendMessage(
                    Component.text("이전 야생 위치로 이동합니다.", NamedTextColor.YELLOW)
                )
                player.teleport(lastLocation)
                return
            }
            while (true) {
                val randZApd = (Math.random() * 10000) - 5000
                val randXApd = (Math.random() * 10000) - 5000
                val hBlock = world.getHighestBlockAt(randXApd.toInt(), randZApd.toInt())
                if (
                    hBlock.type == Material.WATER ||
                    hBlock.type == Material.LAVA ||
                    hBlock.type == Material.CACTUS ||
                    hBlock.type == Material.SUGAR_CANE
                ) {
                    continue
                }
                val spawnLocation = world.getHighestBlockAt(randXApd.toInt(), randZApd.toInt()).location.add(0.0, 1.0, 0.0)
                player.teleport(spawnLocation)

                player.sendMessage(
                    Component.text("랜덤한 위치로 이동합니다.", NamedTextColor.YELLOW)
                )
                break
            }
        }
    }

    fun cacheLastWildLocation(player: Player) {
        val location = player.location
        val world = location.world ?: return
        if (world.name != "world") return
        val x = location.x
        val y = location.y
        val z = location.z
        val locString = "$x,$y,$z"
        lastWildScope.set(player.uniqueId.toString(), locString)
    }

    fun cacheAllPlayersLastWildLocation() {
        for (player in nj.server.onlinePlayers) {
            cacheLastWildLocation(player)
        }
    }

    fun scheduleCacheAllPlayersLastWildLocation() {
        nj.server.scheduler.runTaskTimerAsynchronously(nj, { ->
            cacheAllPlayersLastWildLocation()
        }, 0L, 20L * 30) // 0.5 minutes
    }
}
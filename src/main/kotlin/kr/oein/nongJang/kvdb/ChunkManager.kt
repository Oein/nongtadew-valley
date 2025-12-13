package kr.oein.nongJang.kvdb

import kr.oein.nongJang.NongJang
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*
import kotlin.math.abs
import kotlin.math.max

class ChunkManager(val nj: NongJang) {
    val kvdb = nj.kvdb
    val t1Radius = 2
    val t2Radius = 4
    val t3Radius = 6

    val t3SoilFrom = 20
    val t3SoilTo = 50
    val t2SoilFrom = 50
    val t2SoilTo = 80
    val t1SoilFrom = 90
    val t1SoilTo = 140

    val minTemp = -20
    val maxTemp = 50

    val minHumidity = 0
    val maxHumidity = 100

    val t3Price = 600000L
    val t2Price = 7000000L
    val t1Price = 80000000L

    val tempScore = kvdb.loadScope("chunks-temperature")
    val humidityScope = kvdb.loadScope("chunks-humidity")
    val ownerScope = kvdb.loadScope("chunks-owner")
    val soilScope = kvdb.loadScope("chunks-soil")
    val myChunksScope = kvdb.loadScope("player-mychunks")

    fun getTier(x: Int, z: Int): Int {
        val dist = max(abs(x), abs(z))
        if(dist <= t1Radius) {
            return 1
        } else if(dist <= t2Radius) {
            return 2
        } else if(dist <= t3Radius) {
            return 3
        }
        return 4
    }

    fun getSoilRange(tier: Int): Pair<Int, Int> {
        return when(tier) {
            1 -> Pair(t1SoilFrom, t1SoilTo)
            2 -> Pair(t2SoilFrom, t2SoilTo)
            3 -> Pair(t3SoilFrom, t3SoilTo)
            else -> Pair(0, 0)
        }
    }

    fun genTemperature(): Int {
        return (minTemp..maxTemp).random()
    }

    fun genHumidity(): Int {
        return (minHumidity..maxHumidity).random()
    }

    fun genChunkData(x: Int, z: Int): Map<String, Int> {
        val tier = getTier(x, z)
        val (soilFrom, soilTo) = getSoilRange(tier)
        val temperature = genTemperature()
        val humidity = genHumidity()
        val soil = (soilFrom..soilTo).random()

        return mapOf(
            "temperature" to temperature,
            "humidity" to humidity,
            "soil" to soil
        )
    }

    fun setupChunk(x: Int, z: Int) {
        val key = "${x},${z}"
        if(humidityScope.has(key)) return  // already set up

        val tier = getTier(x, z)
        val (soilFrom, soilTo) = getSoilRange(tier)
        val temperature = genTemperature()
        val humidity = genHumidity()
        val soil = (soilFrom..soilTo).random()

        tempScore.set(key, temperature)
        humidityScope.set(key, humidity)
        soilScope.set(key, soil)
    }
    fun genAllChunkData() {
        var tempYamlStr = ""
        var humidityYamlStr = ""
        var soilYamlStr = ""
        for(x in -t3Radius .. t3Radius) {
            for(z in -t3Radius .. t3Radius) {
                val key = "${x},${z}"
                val chunkData = genChunkData(x, z)
                tempYamlStr += "$key: ${chunkData["temperature"]}\n"
                humidityYamlStr += "$key: ${chunkData["humidity"]}\n"
                soilYamlStr += "$key: ${chunkData["soil"]}\n"
            }
        }

        tempScore.getFile().writeText(tempYamlStr)
        tempScore.load()

        humidityScope.getFile().writeText(humidityYamlStr)
        humidityScope.load()

        soilScope.getFile().writeText(soilYamlStr)
        soilScope.load()
    }

    fun hasOwner(x: Int, z: Int): Boolean {
        val key = "${x},${z}"
        return ownerScope.has(key)
    }
    fun getOwner(x: Int, z: Int): Player? {
        val key = "${x},${z}"
        val uuid = ownerScope.get(key) as String?
        if(uuid != null) {
            return Bukkit.getPlayer(UUID.fromString(uuid))
        }
        return null
    }
    fun add2myChunks(x: Int, z: Int, player: Player) {
        val key = player.uniqueId.toString()
        val chunkKey = "${x},${z}"
        val myChunks = myChunksScope.get(key)
        if(myChunks != null) {
            val chunkList = myChunks.toString().split("!").toMutableList()
            if(!chunkList.contains(chunkKey)) {
                chunkList.add(chunkKey)
                myChunksScope.set(key, chunkList.joinToString("!"))
            }
        } else {
            myChunksScope.set(key, chunkKey)
        }
    }
    fun removeFromMyChunks(x: Int, z: Int, player: Player) {
        val key = player.uniqueId.toString()
        val chunkKey = "${x},${z}"
        val myChunks = myChunksScope.get(key)
        if(myChunks != null) {
            val chunkList = myChunks.toString().split("!").toMutableList()
            if(chunkList.contains(chunkKey)) {
                chunkList.remove(chunkKey)
                myChunksScope.set(key, chunkList.joinToString("!"))
            }
        }
    }
    fun getMyChunks(player: Player): List<Pair<Int, Int>> {
        val key = player.uniqueId.toString()
        val myChunks = myChunksScope.get(key)
        val result = mutableListOf<Pair<Int, Int>>()
        if(myChunks != null) {
            val chunkList = myChunks.toString().split("!")
            for(chunkKey in chunkList) {
                val parts = chunkKey.split(",")
                if(parts.size == 2) {
                    val x = parts[0].toIntOrNull()
                    val z = parts[1].toIntOrNull()
                    if(x != null && z != null) {
                        result.add(Pair(x, z))
                    }
                }
            }
        }
        return result
    }
    fun setOwner(x: Int, z: Int, player: Player) {
        val key = "${x},${z}"
        ownerScope.set(key, player.uniqueId.toString())
        add2myChunks(x, z, player)
    }
    fun removeOwner(x: Int, z: Int) {
        val key = "${x},${z}"
        val owner = getOwner(x, z)
        if(owner != null) {
            removeFromMyChunks(x, z, owner)
        }
        ownerScope.remove(key)
    }

    fun getTemperature(x: Int, z: Int): Int? {
        val key = "${x},${z}"
        return tempScore.get(key) as Int?
    }
    fun getHumidity(x: Int, z: Int): Int? {
        val key = "${x},${z}"
        return humidityScope.get(key) as Int?
    }
    fun getSoil(x: Int, z: Int): Int? {
        val key = "${x},${z}"
        return soilScope.get(key) as Int?
    }

    fun getChunkData(x: Int, z: Int): Map<String, Int?> {
        val temperature = getTemperature(x, z)
        val humidity = getHumidity(x, z)
        val soil = getSoil(x, z)
        return mapOf(
            "temperature" to temperature,
            "humidity" to humidity,
            "soil" to soil
        )
    }

    val moneyManager = nj.moneyManager
    fun getPrice(x: Int, z: Int): Long? {
        val tier = getTier(x, z)
        return when(tier) {
            1 -> t1Price
            2 -> t2Price
            3 -> t3Price
            else -> null
        }
    }
    fun isOwnerEqual(x: Int, z: Int, player: Player): Boolean {
        val owner = getOwner(x, z)
        if(owner != null) {
            return owner.uniqueId == player.uniqueId
        }
        return false
    }
    fun purchaseChunk(x: Int, z: Int, player: Player): Boolean {
        if(hasOwner(x, z)) {
            return false
        }

        val price = getPrice(x, z) ?: return false

        val playerMoney = moneyManager.getMoney(player)
        if(playerMoney < price) {
            return false
        }
        moneyManager.removeMoney(player, price)
        setOwner(x, z, player)

        // check left
        var nx = x - 1
        var nz = z
        val njWorld = nj.njCommands.nongjangWorld!!
        if(isOwnerEqual(nx, nz, player)) {
            // x == 15
            // z == 0~14
            // remove barrier
            for(zd in 0..14) {
                for (y in 1..254) {
                    val material = when (y) {
                        in 1..4 -> org.bukkit.Material.DIRT
                        5 -> org.bukkit.Material.GRASS_BLOCK
                        else -> org.bukkit.Material.AIR
                    }
                    njWorld.getBlockAt((nx * 16) + 15, y, (nz * 16) + zd).type = material
                }
            }
        }

        // check right
        nx = x + 1
        nz = z
        if(isOwnerEqual(nx, nz, player)) {
            // x == 15
            // z == 0~14
            // remove barrier
            for(zd in 0..14) {
                for (y in 1..254) {
                    val material = when (y) {
                        in 1..4 -> org.bukkit.Material.DIRT
                        5 -> org.bukkit.Material.GRASS_BLOCK
                        else -> org.bukkit.Material.AIR
                    }
                    njWorld.getBlockAt((nx * 16) - 1, y, (nz * 16) + zd).type = material
                }
            }
        }

        // check up
        nx = x
        nz = z - 1
        if(isOwnerEqual(nx, nz, player)) {
            // x == 0~14
            // z == 15
            // remove barrier
            for(xd in 0..14) {
                for (y in 1..254) {
                    val material = when (y) {
                        in 1..4 -> org.bukkit.Material.DIRT
                        5 -> org.bukkit.Material.GRASS_BLOCK
                        else -> org.bukkit.Material.AIR
                    }
                    njWorld.getBlockAt((x * 16) + xd, y, (z * 16) - 1).type = material
                }
            }
        }

        // check down
        nx = x
        nz = z + 1
        if(isOwnerEqual(nx, nz, player)) {
            // x == 0~14
            // z == 15
            // remove barrier
            for(xd in 0..14) {
                for (y in 1..254) {
                    val material = when (y) {
                        in 1..4 -> org.bukkit.Material.DIRT
                        5 -> org.bukkit.Material.GRASS_BLOCK
                        else -> org.bukkit.Material.AIR
                    }
                    njWorld.getBlockAt((x * 16) + xd, y, (z * 16) + 15).type = material
                }
            }
        }

        // check left,up,left-up
        if(isOwnerEqual(x - 1, z, player) && isOwnerEqual(x, z - 1, player) && isOwnerEqual(x - 1, z - 1, player)) {
            // remove corner barrier
            for(y in 1..254) {
                val material = when (y) {
                    in 1..4 -> org.bukkit.Material.DIRT
                    5 -> org.bukkit.Material.GRASS_BLOCK
                    else -> org.bukkit.Material.AIR
                }
                njWorld.getBlockAt((x - 1) * 16 + 16 - 1, y, (z - 1) * 16 + 16 - 1).type = material
            }
        }

        // check right,up,right-up
        if(isOwnerEqual(x + 1, z, player) && isOwnerEqual(x, z - 1, player) && isOwnerEqual(x + 1, z - 1, player)) {
            // remove corner barrier
            for(y in 1..254) {
                val material = when (y) {
                    in 1..4 -> org.bukkit.Material.DIRT
                    5 -> org.bukkit.Material.GRASS_BLOCK
                    else -> org.bukkit.Material.AIR
                }
                njWorld.getBlockAt((x) * 16 + 16 - 1, y, (z - 1) * 16 + 16 - 1).type = material
            }
        }

        // check left,down,left-down
        if(isOwnerEqual(x - 1, z, player) && isOwnerEqual(x, z + 1, player) && isOwnerEqual(x - 1, z + 1, player)) {
            // remove corner barrier
            for(y in 1..254) {
                val material = when (y) {
                    in 1..4 -> org.bukkit.Material.DIRT
                    5 -> org.bukkit.Material.GRASS_BLOCK
                    else -> org.bukkit.Material.AIR
                }
                njWorld.getBlockAt((x - 1) * 16 + 16 - 1, y, (z) * 16 + 16 - 1).type = material
            }
        }

        // check right,down,right-down
        if(isOwnerEqual(x + 1, z, player) && isOwnerEqual(x, z + 1, player) && isOwnerEqual(x + 1, z + 1, player)) {
            // remove corner barrier
            for(y in 1..254) {
                val material = when (y) {
                    in 1..4 -> org.bukkit.Material.DIRT
                    5 -> org.bukkit.Material.GRASS_BLOCK
                    else -> org.bukkit.Material.AIR
                }
                njWorld.getBlockAt(x * 16 + 15, y, z * 16 + 15).type = material
            }
        }

        return true
    }

    fun clearPurchases() {
        ownerScope.clear()
        ownerScope.save()
        myChunksScope.clear()
        myChunksScope.save()
    }
}
package kr.oein.nongJang.http

import io.javalin.Javalin
import kr.oein.nongJang.NongJang
import kr.oein.nongJang.farm.FarmConfig
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

class Farmland(val nj: NongJang, val app: Javalin) {
    private var cache: MutableMap<String, GrowingState> = mutableMapOf()

    fun calculateGrownStateForChunk(x: Int, z: Int): GrowingState {
        if(nj.njCommands.nongjangWorld == null) {
            return GrowingState(0L, 0L, 0L)
        }
        var growing = 0L
        var grown = 0L
        var shit = 0L

        for(xApd in 0..15) {
            for(zApd in 0..15) {
                val xPos = (x shl 4) + xApd
                val zPos = (z shl 4) + zApd

                var hBlock: Block? = null
                for(y in 254 downTo 0) {
                    val block = nj.njCommands.nongjangWorld!!.getBlockAt(xPos, y, zPos)
                    if(block.type != Material.AIR) {
                        hBlock = block
                        break
                    }
                }

                if(hBlock == null) continue
                if(hBlock.type != Material.VOID_AIR) continue

                // get item frame

                // get nearest item frame
                val nearbyItemFrames = hBlock.location.toCenterLocation().getNearbyEntitiesByType(
                    ItemFrame::class.java,
                    0.5
                )

                // print nearby item frames
                if(nearbyItemFrames.size != 1) continue

                val nearbyItemFrame = nearbyItemFrames.first()

                val grownLevel = nearbyItemFrame.item.persistentDataContainer.get(FarmConfig.grownLevel, PersistentDataType.DOUBLE) ?: continue
                val shitLevel = nearbyItemFrame.item.persistentDataContainer.get(FarmConfig.shitLevel, PersistentDataType.DOUBLE) ?: continue

                if(shitLevel >= 100.0) {
                    shit++
                } else if(grownLevel >= 100.0) {
                    grown++
                } else {
                    growing++
                }
            }
        }

        return GrowingState(growing, grown, shit)
    }

    fun calculateGrownStateForPlayer(player: Player): GrowingState {
        val playerChunks = nj.chunkManager.getMyChunks(player)
        var growing = 0L
        var grown = 0L
        var shit = 0L
        for(chunk in playerChunks) {
            val chunkX = chunk.first
            val chunkZ = chunk.second
            val res = calculateGrownStateForChunk(chunkX, chunkZ)
            growing += res.growing
            grown += res.grown
            shit += res.shit
        }
        cache[player.uniqueId.toString()] = GrowingState(growing, grown, shit)
        return GrowingState(growing, grown, shit)
    }

    fun calculateGrownStateForAllPlayers() {
        nj.njCommands.ensureNongJangWorld()
        for(player in nj.server.onlinePlayers) {
            calculateGrownStateForPlayer(player)
        }
    }

    // per 60 seconds
    fun scheduleUpdate() {
        nj.server.scheduler.scheduleSyncRepeatingTask(nj, {
            calculateGrownStateForAllPlayers()
        }, 0L, 20L * 60L)
    }

    init {
        app.get("/farm/{uuid}") { ctx ->
            val uuid = ctx.pathParam("uuid")
            val cacheData = cache[uuid]
            if(cacheData == null) {
                ctx.result("NCD:No Cache Data")
                return@get
            }

            ctx.result("SUC:${cacheData.growing}|${cacheData.grown}|${cacheData.shit}")
        }
    }
}
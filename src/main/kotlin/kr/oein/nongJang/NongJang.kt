package kr.oein.nongJang

import kr.oein.interchest.GUIListener
import kr.oein.interchest.GUIManager
import kr.oein.nongJang.commands.NongJangCommands
import kr.oein.nongJang.farm.Grow
import kr.oein.nongJang.kvdb.ChunkManager
import kr.oein.nongJang.kvdb.KVDB
import kr.oein.nongJang.kvdb.MoneyManager
import kr.oein.nongJang.utils.Scoreboard
import kr.oein.nongJang.shiftf.ShiftF
import kr.oein.nongJang.utils.BlockInteraction
import kr.oein.nongJang.utils.Bossbar
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class NongJang : JavaPlugin() {
    var kvdb = KVDB(this)
    val moneyManager = MoneyManager(kvdb)
    val chunkManager = ChunkManager(this)
    val guiManager = GUIManager()
    val guiListener = GUIListener(guiManager)
    var njCommands = NongJangCommands
    val grow = Grow(this)
    val bossbar = Bossbar(this)

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(guiListener, this)
        Bukkit.getPluginManager().registerEvents(ShiftF(this), this)
        Bukkit.getPluginManager().registerEvents(Scoreboard(this), this)
        Bukkit.getPluginManager().registerEvents(BlockInteraction(this), this)
        Bukkit.getPluginManager().registerEvents(grow, this)

        saveDefaultConfig()
        // Register commands and ensure the nong-jang world after the server has finished loading worlds
        njCommands.register(this)

        grow.scheduleGrowthHandling()
        bossbar.updateSchedule()
    }

    override fun onLoad() {
        // Keep onLoad minimal. Avoid interacting with worlds or CommandAPI here because the server
        // may not have finished loading worlds or plugin registries.
    }

    override fun onDisable() {
        bossbar.hideAll()
    }
}

package kr.oein.nongJang.utils

import kr.oein.nongJang.NongJang
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

class Bossbar(val nj: NongJang) {
    val activeBar: BossBar = BossBar.bossBar(
        Component.text("성장까지 남은 시간 : 0초"),
        1.0f,
        BossBar.Color.GREEN,
        BossBar.Overlay.NOTCHED_20
    )
    fun handlePlayer(player: Player) {
        player.showBossBar(activeBar)
    }

    fun updateSchedule() {
        nj.server.scheduler.scheduleSyncRepeatingTask(nj, {
            activeBar.name(Component.text("성장까지 남은 시간 : ${nj.grow.leftTicks.toFloat() / 20.0f}초"))
            activeBar.progress(nj.grow.leftTicks.toFloat() / nj.grow.fullGrowTicks.toFloat())
            for (player in nj.server.onlinePlayers) {
                handlePlayer(player)
            }
        }, 0L, nj.grow.scheduleInterval.toLong())
    }

    fun hideAll() {
        for (player in nj.server.onlinePlayers) {
            player.hideBossBar(activeBar)
        }
    }
}
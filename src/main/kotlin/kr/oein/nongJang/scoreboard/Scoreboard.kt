package kr.oein.nongJang.scoreboard

import io.papermc.paper.scoreboard.numbers.NumberFormat
import kr.oein.nongJang.NongJang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.ScoreboardManager

class Scoreboard: Listener {
    val nj: NongJang
    val scoreboardManager: ScoreboardManager

    fun setupForPlayer(player: org.bukkit.entity.Player) {
        val board = scoreboardManager.newScoreboard
        val objective = board.registerNewObjective(
            "nongjang",
            Criteria.DUMMY,
            Component.text("농타듀벨리", NamedTextColor.GREEN)
        )
        objective.displaySlot = org.bukkit.scoreboard.DisplaySlot.SIDEBAR
        objective.numberFormat(NumberFormat.blank())

        val score = objective.getScore("welcome")
        score.score = 1

        score.customName(
            Component.text("환영합니다, ").append {
                Component.text(player.name, NamedTextColor.YELLOW)
            }
        )

        val moneyScore = objective.getScore("money")
        moneyScore.customName(
            Component.text("보유 금액: ").append {
                val money = nj.moneyManager.getMoney(player)
                Component.text("${money}원", NamedTextColor.GOLD)
            }
        )
        moneyScore.score = 0

        player.scoreboard = board
    }

    @EventHandler
    fun onPlayerJoin(event: org.bukkit.event.player.PlayerJoinEvent) {
        setupForPlayer(event.player)
    }

    fun updateSchedule() {
        nj.server.scheduler.scheduleSyncRepeatingTask(nj, {
            for (player in nj.server.onlinePlayers) {
                val board = player.scoreboard
                val objective = board.getObjective("nongjang") ?: continue
                val moneyScore = objective.getScore("money")
                moneyScore.customName(
                    Component.text("보유 금액: ").append {
                        val money = nj.moneyManager.getMoney(player)
                        Component.text("${money}원", NamedTextColor.GOLD)
                    }
                )
            }
        }, 0L, 5L) // 0.25초마다 갱신
    }

    constructor(nj: NongJang) {
        this.nj = nj
        this.scoreboardManager = nj.server.scoreboardManager

        for (players in nj.server.onlinePlayers) {
            setupForPlayer(players)
        }
        updateSchedule()
    }
}
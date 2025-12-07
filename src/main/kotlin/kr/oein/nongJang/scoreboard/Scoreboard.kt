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

        val moneyScore = objective.getScore("money")
        moneyScore.customName(
            Component.text("보유 금액 ")
        )

        val money = nj.moneyManager.getMoney(player)

        moneyScore.numberFormat(
            NumberFormat.fixed(
                Component.text("${money}원", NamedTextColor.GOLD)
            )
        )
        moneyScore.score = 1000

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

                val money = nj.moneyManager.getMoney(player)
                moneyScore.numberFormat(
                    NumberFormat.fixed(
                        Component.text("${money}원", NamedTextColor.GOLD)
                    )
                )

                val spacerScore = objective.getScore("spacer1")
                val nongjangNameScore = objective.getScore("nongjangName")
                val nongjangTempScore = objective.getScore("nongjangTemp")
                val nongjangSoilScore = objective.getScore("nongjangSoil")
                val nongjangWaterScore = objective.getScore("nongjangWater")

                if(player.world == nj.njCommands.nongjangWorld) {
                    spacerScore.score = 905
                    nongjangNameScore.score = 904
                    nongjangTempScore.score = 903
                    nongjangSoilScore.score = 902
                    nongjangWaterScore.score = 901

                    spacerScore.customName(Component.text(" "))
                    spacerScore.numberFormat(NumberFormat.blank())
                    val playerPos = player.location
                    val chunkX = playerPos.x.toInt() shr 4
                    val chunkZ = playerPos.z.toInt() shr 4

                    val chunkData = nj.chunkManager.getChunkData(chunkX, chunkZ)
                    nongjangNameScore.customName(
                        Component.text("농장 ")
                    )
                    nongjangNameScore.numberFormat(
                        NumberFormat.fixed(
                            Component.text("($chunkX, $chunkZ)", NamedTextColor.GREEN)
                        )
                    )
                    nongjangTempScore.customName(
                        Component.text("현재 온도 ")
                    )
                    nongjangTempScore.numberFormat(
                        NumberFormat.fixed(
                            Component.text("${chunkData["temperature"]}'C", NamedTextColor.AQUA)
                        )
                    )
                    nongjangSoilScore.customName(
                        Component.text("비옥도 ")
                    )
                    nongjangSoilScore.numberFormat(
                        NumberFormat.fixed(
                            Component.text("${chunkData["soil"]}%", NamedTextColor.BLUE)
                        )
                    )
                    nongjangWaterScore.customName(
                        Component.text("습도 ")
                    )
                    nongjangWaterScore.numberFormat(
                        NumberFormat.fixed(
                            Component.text("${chunkData["humidity"]}%", NamedTextColor.DARK_AQUA)
                        )
                    )
                } else {
                    spacerScore.resetScore()
                    nongjangNameScore.resetScore()
                    nongjangTempScore.resetScore()
                    nongjangSoilScore.resetScore()
                    nongjangWaterScore.resetScore()
                }
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
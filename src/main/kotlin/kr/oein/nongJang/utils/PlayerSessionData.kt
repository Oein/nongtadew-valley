package kr.oein.nongJang.utils

import org.bukkit.entity.Player

data class PlayerSessionData(
    val player: Player,
    var isNewbie: Boolean,
    var password: String?,
    var discordId: String?,
    var discordOauthToken: String?
)

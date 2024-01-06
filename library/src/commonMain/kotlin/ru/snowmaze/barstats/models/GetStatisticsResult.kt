package ru.snowmaze.barstats.models

class GetStatisticsResult(
    val playerId: Long,
    val playerName: String,
    val playerData: PlayerData,
    val unbalancedMatchesStats: UnbalancedMatchesStats? = null,
    val bestTeammates: List<WithPlayerStat>,
    val lobsterTeammates: List<WithPlayerStat>,
    val bestAgainst: List<WithPlayerStat>,
    val bestOpponents: List<WithPlayerStat>,
)

class WithPlayerStat(val playerData: PlayerData, val playerStats: PlayerStats)
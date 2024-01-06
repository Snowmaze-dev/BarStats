package ru.snowmaze.barstats.models

import ru.snowmaze.barstats.models.external.AbstractMatchModel

class PlayerData(
    val userId: Long,
    val playerName: String,
    val preset: String,
    val totalMatchesCount: Int,
    val accountedMatchesCount: Int,
    val wonMatchesCount: Int,
    val lostMatchesCount: Int,
    val winrate: Float,
    val averageTeammateSkill: Float?,
    val averageEnemySkill: Float?,
    val matches: List<AbstractMatchModel>,
    val mapsStats: List<MapStat>?
)

data class MapStat(val mapName: String, val wins: Int, val loses: Int) {

    val totalMatchesCount = wins + loses

    val winrate = (wins.toFloat() / totalMatchesCount) * 100f

    override fun toString(): String {
        return "MapStat(totalMatchesCount=$totalMatchesCount, winrate=$winrate%, wins=$wins, loses=$loses)"
    }
}

class IntermediateMapModel(val mapName: String, var wins: Int, var loses: Int)
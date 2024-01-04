package ru.snowmaze.barstats.models

data class SimplifiedPlayerStats(
    val playerName: String,
    val totalMatchesCount: Int,
    val accountedMatchesCount: Int,
    val wonMatchesCount: Int,
    val lostMatchesCount: Int,
    val winrate: Float
) {

    override fun toString(): String {
        return "SimplifiedPlayerStats(totalMatchesCount=$totalMatchesCount, accountedMatchesCount=$accountedMatchesCount, wonMatchesCount=$wonMatchesCount, lostMatchesCount=$lostMatchesCount, winrate=$winrate%)"
    }
}
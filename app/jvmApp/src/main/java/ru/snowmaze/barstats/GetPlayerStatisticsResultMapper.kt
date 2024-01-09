package ru.snowmaze.barstats

import ru.snowmaze.barstats.models.GetStatisticsResult
import ru.snowmaze.barstats.models.PlayerData
import ru.snowmaze.barstats.models.PlayerStats
import ru.snowmaze.barstats.models.WithPlayerStat
import java.util.concurrent.TimeUnit

fun GetStatisticsResult.mapToSection(splitter: String): Section {
    val stats = this.playerData
    val mapPlayedWith: List<WithPlayerStat>.(isEnemies: Boolean) -> List<SectionValue> = { isEnemies ->
        mapIndexed { index, withStats ->
            SectionValue.StringSectionValue(
                mapStats(
                    index = index,
                    playerName = withStats.withPlayerName,
                    playerData = withStats.playerData,
                    playerStats = withStats.playerStats,
                    isEnemies = isEnemies,
                    splitter = splitter
                )
            )
        }
    }
    return Section(
        name = "Player ${stats.playerName} stat",
        values = buildList<Pair<String, Any?>> {
            addAll(
                listOf(
                    "Total matches count" to stats.totalMatchesCount,
                    "Accounted matches count" to stats.accountedMatchesCount.takeIf { stats.totalMatchesCount != it },
                    "Won matches" to stats.wonMatchesCount,
                    "Lost matches" to stats.lostMatchesCount,
                    "Winrate" to stats.winrate.toString() + "%",
                    "Average teammate skill" to stats.averageTeammateSkill,
                    "Average ${stats.preset} enemy skill" to stats.averageEnemySkill,
                    "Hours spent in analyzed games" to TimeUnit.MILLISECONDS.toHours(stats.overallPlayerPlaytimeMs),
                )
            )
            val unbalancedMatchesStats = unbalancedMatchesStats
            if (unbalancedMatchesStats != null) {
                addAll(
                    listOf(
                        "Unbalanced matches count" to unbalancedMatchesStats.unbalancedMatchesCount,
                        "Won unbalanced matches in weaker team" to unbalancedMatchesStats.wonUnbalancedMatchesInWeakerTeam,
                        "Lost unbalanced matches in weaker team" to unbalancedMatchesStats.lostUnbalancedMatchesInWeakerTeam,
                    )
                )
            }
        }.map { SectionValue.KeyValueSectionValue(it.first, it.second) },
        subSubsections = listOf(
            Section(
                name = "Maps stats", values = stats.mapsStats?.mapIndexed { index, mapStat ->
                    SectionValue.StringSectionValue(buildString {
                        append("${index + 1}. ", mapStat.mapName, splitter)
                        append("Total matches count: ", mapStat.totalMatchesCount, splitter)
                        append("Winrate: ${mapStat.winrate}%", splitter)
                        append("Wins: ${mapStat.wins}", splitter)
                        append("Loses: ${mapStat.loses}")
                    })
                }
            ),
            Section(
                "Best teammates",
                values = bestTeammates.mapPlayedWith(false),
            ),
            Section(
                "Lobster teammates",
                values = lobsterTeammates.mapPlayedWith(false)
            ),
            Section(
                "Best against",
                values = bestAgainst.mapPlayedWith(true)
            ),
            Section(
                "Best opponents",
                values = bestOpponents.mapPlayedWith(true)
            )
        )
    )
}

fun mapStats(
    index: Int,
    playerName: String,
    playerData: PlayerData?,
    playerStats: PlayerStats,
    isEnemies: Boolean,
    splitter: String
) = buildString {
    append(index + 1, ". ", playerName, splitter)
    val totalGamesWithText = if (isEnemies) "Games against" else "Games together"
    append(totalGamesWithText, ": ", playerStats.totalGamesTogether, splitter)
    val withText = if (isEnemies) "against player" else "with teammate"
    append("Winrate ", withText, ": ", playerStats.winrate, "%", splitter)
    val winrate = playerData?.winrate
    if (winrate != null) append("Player overall winrate: ", winrate, "%", splitter)
    val winsWith = if (isEnemies) "against player" else "with teammate"
    append("Wins ", winsWith, ": ", if (isEnemies) playerStats.lostGames else playerStats.wonGames, splitter)
    val losesWith = if (isEnemies) "against player" else "with teammate"
    append("Loses ", losesWith, ": ", if (isEnemies) playerStats.wonGames else playerStats.lostGames)
}
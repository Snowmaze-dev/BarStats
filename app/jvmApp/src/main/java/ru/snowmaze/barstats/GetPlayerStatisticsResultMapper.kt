package ru.snowmaze.barstats

import ru.snowmaze.barstats.models.GetStatisticsResult
import ru.snowmaze.barstats.models.PlayerData
import ru.snowmaze.barstats.models.PlayerStats
import ru.snowmaze.barstats.models.WithPlayerStat

fun GetStatisticsResult.mapToSection(splitter: String): Section {
    val stats = this.playerData
    val mapPlayedWith: List<WithPlayerStat>.(isEnemies: Boolean) -> List<SectionValue> = { isEnemies ->
        mapIndexed { index, withStats ->
            SectionValue.StringSectionValue(
                mapStats(
                    index,
                    withStats.playerData,
                    withStats.playerStats,
                    isEnemies,
                    splitter
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
    playerData: PlayerData,
    playerStats: PlayerStats,
    isEnemies: Boolean,
    splitter: String
) = buildString {
    append(index + 1, ". ", playerData.playerName, splitter)
    val totalGamesWithText = if (isEnemies) "Games against" else "Games together"
    append(totalGamesWithText, ": ", playerStats.totalGamesTogether, splitter)
    val withText = if (isEnemies) "against player" else "with teammate"
    append("Winrate ", withText, ": ", playerStats.winrate, "%", splitter)
    append("Player overall winrate: ", playerData.winrate, "%", splitter)
    val winsWith = if (isEnemies) "against player" else "with teammate"
    append("Wins ", winsWith, ": ", if (isEnemies) playerStats.lostGames else playerStats.wonGames, splitter)
    val losesWith = if (isEnemies) "against player" else "with teammate"
    append("Loses ", losesWith, ": ", if (isEnemies) playerStats.wonGames else playerStats.lostGames)
}
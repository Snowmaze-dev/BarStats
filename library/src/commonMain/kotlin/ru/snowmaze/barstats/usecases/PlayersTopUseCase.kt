package ru.snowmaze.barstats.usecases

import okio.Path
import ru.snowmaze.barstats.getSystemFileSystem
import ru.snowmaze.barstats.models.IntermediateMatchesModel
import ru.snowmaze.barstats.models.SimplifiedPlayerStats
import ru.snowmaze.barstats.parallelMap
import ru.snowmaze.barstats.repository.GetMatchesRepository
import ru.snowmaze.barstats.repository.PlayersRepository

class PlayersTopUseCase(
    private val getMatchesRepository: GetMatchesRepository,
    private val playersRepository: PlayersRepository,
    private val getPlayerStatsUseCase: GetPlayerStatsUseCase,
    private val dataDir: Path
) {

    suspend fun printTopByPlayerMatches(preset: String, map: String? = null) {
        val playerNames = (getSystemFileSystem().list(dataDir.resolve("players_matches"))).asSequence().map {
            it.name.substringBeforeLast(".").split("-")
        }.filter {
            if (it.size == 1) true
            else if (it.isEmpty()) false
            else it[1] == preset
        }.map { it[0] }.toSet()
        println("Sorting ${playerNames.size} players")

        val mappedMap = playerNames.parallelMap(10) {
            it to kotlin.runCatching {
                getPlayerStatsUseCase.getPlayerStats(it, preset, map = map)
            }.getOrNull()
        }.associate { it }.mapValues {
            val stats = it.value ?: return@mapValues null
            val totalMatches = stats.wonMatchesCount + stats.lostMatchesCount
            SimplifiedPlayerStats(
                playerName = stats.playerName,
                totalMatchesCount = totalMatches,
                accountedMatchesCount = totalMatches,
                wonMatchesCount = stats.wonMatchesCount,
                lostMatchesCount = stats.lostMatchesCount,
                winrate = (stats.wonMatchesCount / totalMatches.toFloat()) * 100
            )
        }
        printTop(mappedMap, map)
    }

    suspend fun printTopByMatches(preset: String, map: String? = null) {
        val playersMap = mutableMapOf<Long, IntermediateMatchesModel>()

        val matchesIds = (getSystemFileSystem().list(dataDir.resolve("matches"))).map {
            it.name.substringBeforeLast(".")
        }
        for (matchId in matchesIds) {
            val match = getMatchesRepository.getExtendedMatch(matchId)
            if (match.preset != preset) continue
            if (match.teams.all { !it.winningTeam }) continue
            if (map != null) {
                if (!match.map.scriptName.contains(map, ignoreCase = true)) continue
            }
            for (team in match.teams) {
                for (player in team.players) {
                    player.userId ?: continue

                    val model = playersMap.getOrPut(player.userId) {
                        IntermediateMatchesModel(
                            playersRepository.getPlayer(player.userId).username,
                            0,
                            0
                        )
                    }
                    if (team.winningTeam) model.wins++
                    else model.loses++
                }
            }
        }
        val mappedMap = playersMap.mapValues {
            val stats = it.value
            val totalMatches = stats.wins + stats.loses
            SimplifiedPlayerStats(
                playerName = it.value.playerName,
                totalMatchesCount = totalMatches,
                accountedMatchesCount = totalMatches,
                wonMatchesCount = stats.wins,
                lostMatchesCount = stats.loses,
                winrate = (stats.wins / totalMatches.toFloat()) * 100
            )
        }
        printTop(mappedMap, map)
    }

    private fun printTop(playersMap: Map<*, SimplifiedPlayerStats?>, map: String? = null) {
        val sortedMap = playersMap.filter {
            val value = it.value
            value != null && value.totalMatchesCount > 150
        } as Map<Any?, SimplifiedPlayerStats>
        val keysSorted = sortedMap.keys.sortedByDescending { sortedMap.getValue(it).winrate }
        var whiteSpace = ""
        val println = { string: CharSequence ->
            println(whiteSpace + string)
        }
        val onMap = if (map == null) "" else " on map $map"
        println("Winrates of cached players$onMap:")
        whiteSpace = " "
        for ((index, key) in keysSorted.withIndex()) {
            val value = sortedMap.getValue(key)
            println("$index. ${value.playerName} winrate=${value.winrate}% $value")
        }
    }
}
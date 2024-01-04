package ru.snowmaze.barstats.usecases

import okio.Path
import ru.snowmaze.barstats.getSystemFileSystem
import ru.snowmaze.barstats.repository.GetMatchesRepository

// TODO
class WinrateByPositionUseCase(
    private val getMatchesRepository: GetMatchesRepository,
    private val dataDir: Path
) {

    private val positionsDeterminers = listOf(IsthmusPositionsDeterminer())

    suspend fun printWinratesByPositions(preset: String) {
        val winratesByPositions = mutableMapOf<String, PositionMatches>()
        val matchesIds = (getSystemFileSystem().list(dataDir.resolve("matches"))).map {
            it.name.substringBeforeLast(".")
        }
        for (matchId in matchesIds) {
            val match = getMatchesRepository.getExtendedMatch(matchId)
            if (match.preset != preset) continue
            if (match.teams.all { !it.winningTeam }) continue
            val positionsDeterminer = positionsDeterminers.firstOrNull {
                match.map.scriptName.contains(it.mapName)
            } ?: continue
            for (team in match.teams) {
                for (player in team.players) {
                    val position =
                        positionsDeterminer.determinePosition(player.startPos ?: continue)
                            ?: continue
                    val model = winratesByPositions.getOrPut(position) {
                        PositionMatches(0, 0)
                    }
                }
            }
        }
    }
}

private class PositionMatches(val wins: Int, val loses: Int)
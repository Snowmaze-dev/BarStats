package ru.snowmaze.barstats.usecases

import ru.snowmaze.barstats.models.GetStatisticsResult
import ru.snowmaze.barstats.models.IntermediateMatchesModel
import ru.snowmaze.barstats.models.PlayerData
import ru.snowmaze.barstats.models.PlayerStats
import ru.snowmaze.barstats.models.UnbalancedMatchesStats
import ru.snowmaze.barstats.models.WithPlayerStat
import ru.snowmaze.barstats.models.external.MatchModel
import ru.snowmaze.barstats.models.external.MatchTeamModel
import ru.snowmaze.barstats.models.external.PlayerModel
import ru.snowmaze.barstats.parallelMap
import kotlin.math.abs

class GetStatisticsUseCase(
    private val getPlayersStatsUseCase: GetPlayerStatsUseCase,
    private val showInfo: ((String) -> Unit)? = null
) {

    suspend fun getStatistics(
        playerName: String,
        preset: String,
        countOfTeammatesAndEnemiesToShow: Int = 50,
        minGamesForStats: Int = 10,
        limit: Int? = null,
        map: String? = null,
        fromTimeSeconds: Long? = null,
        shouldPrintStatsWithOtherPlayers: Boolean = false,
        onPartOfDataLoaded: suspend (PlayerData) -> Unit = {}
    ): GetStatisticsResult {
        val data = getPlayersStatsUseCase.getPlayerStats(
            playerName = playerName,
            preset = preset,
            limit = limit,
            map = map,
            fromTime = fromTimeSeconds,
            gatherDataModel = GatherDataModel(
                shouldGatherSkillsStats = true,
                shouldGatherMapsStats = true
            )
        )
        onPartOfDataLoaded(data)
        showInfo?.invoke("Analyzing ${data.matches.size} matches")
        val teammatesMatchesStat = mutableMapOf<Long, IntermediateMatchesModel>()
        val enemiesMatchesStat = mutableMapOf<Long, IntermediateMatchesModel>()
        val matches = data.matches as List<MatchModel>
        for (match in matches) {
            if (match.teams.all { !it.winningTeam }) continue
            val playerTeam = match.playerTeam(data.userId) ?: continue
            val isWon = playerTeam.winningTeam
            for (allyPlayer in playerTeam.players) {
                if (allyPlayer.userId == data.userId || allyPlayer.userId == null) continue
                val model = teammatesMatchesStat.getOrPut(allyPlayer.userId) {
                    IntermediateMatchesModel(allyPlayer.name, 0, 0)
                }
                if (isWon) model.wins++ else model.loses++
            }
            for (team in match.teams) {
                if (team == playerTeam) continue
                for (player in team.players) {
                    val model = enemiesMatchesStat.getOrPut(player.userId ?: continue) {
                        IntermediateMatchesModel(player.name, 0, 0)
                    }
                    if (team.winningTeam) model.wins++ else model.loses++
                }
            }
        }

        val unbalancedMatchesStats = if (shouldPrintStatsWithOtherPlayers) getUnbalancedMatches(
            userId = data.userId,
            matches = matches,
            preset = preset,
            limit = limit,
            map = map,
            fromTime = fromTimeSeconds
        ) else null

        val teammatesStatMap = teammatesMatchesStat.mapToStats(false)

        val gamesWithFilter = teammatesStatMap.filterByMinGames(minGamesForStats)
        val bestTeammatesKeys = gamesWithFilter.keys.asSequence().filter {
            teammatesStatMap.getValue(it).winrate >= 50f
        }.sortedByDescending { teammatesStatMap.getValue(it).winrate }
            .take(countOfTeammatesAndEnemiesToShow).toList()

        val lobsterTeammatesKeys = gamesWithFilter.keys.asSequence().filter {
            50f > teammatesStatMap.getValue(it).winrate
        }.sortedBy { teammatesStatMap.getValue(it).winrate }.take(countOfTeammatesAndEnemiesToShow)
            .toList()

        val enemiesStatMap = enemiesMatchesStat.mapToStats(true)
        val enemiesStatFiltered = enemiesStatMap.filterByMinGames(minGamesForStats)
        val bestAgainstKeys = enemiesStatFiltered.keys.asSequence().filter {
            enemiesStatMap.getValue(it).winrate >= 50f
        }.sortedByDescending {
            enemiesStatMap.getValue(it).winrate
        }.take(countOfTeammatesAndEnemiesToShow).toList()

        val bestOpponentsKeys = enemiesStatFiltered.keys.asSequence().filter {
            50f > enemiesStatMap.getValue(it).winrate
        }.sortedBy {
            enemiesStatMap.getValue(it).winrate
        }.take(countOfTeammatesAndEnemiesToShow).toList()

        val mapStats: suspend Long.(statMap: Map<Long, PlayerStats>) -> WithPlayerStat = {
            val teammateId = this
            val teammateStats = it.getValue(teammateId)
            val teammateData = getPlayersStatsUseCase.getPlayerStats(teammateId, preset, limit, map)
            WithPlayerStat(teammateData, teammateStats)
        }
        val mapTeammates: suspend List<Long>.() -> List<WithPlayerStat> = {
            parallelMap(20) {
                it.mapStats(teammatesStatMap)
            }
        }
        val mapEnemies: suspend List<Long>.() -> List<WithPlayerStat> = {
            parallelMap(20) {
                it.mapStats(enemiesStatMap)
            }
        }

        return GetStatisticsResult(
            playerId = data.userId,
            playerName = data.playerName,
            playerData = data,
            unbalancedMatchesStats = unbalancedMatchesStats,
            bestTeammates = mapTeammates(bestTeammatesKeys),
            lobsterTeammates = mapTeammates(lobsterTeammatesKeys),
            bestAgainst = mapEnemies(bestAgainstKeys),
            bestOpponents = mapEnemies(bestOpponentsKeys)
        )
    }

    private fun Map<Long, PlayerStats>.filterByMinGames(minGames: Int = 10) = filter {
        it.value.totalGamesTogether > minGames
    }.takeIf { it.isNotEmpty() } ?: this

    private fun MutableMap<Long, IntermediateMatchesModel>.mapToStats(isEnemies: Boolean) =
        mapValues {
            val model = it.value
            val totalGamesTogether = model.wins + model.loses
            PlayerStats(
                name = model.playerName,
                totalGamesTogether = totalGamesTogether,
                wonGames = model.wins,
                lostGames = model.loses,
                winrate = ((if (isEnemies) model.loses else model.wins) / totalGamesTogether.toFloat()) * 100
            )
        }

    private suspend fun getUnbalancedMatches(
        userId: Long,
        matches: List<MatchModel>,
        preset: String,
        limit: Int?,
        map: String?,
        fromTime: Long?
    ): UnbalancedMatchesStats {
        var wonUnbalancedMatches = 0
        var wonUnbalancedMatchesInWeakerTeam = 0
        var lostUnbalancedMatchesInWeakerTeam = 0
        var unbalancedMatchesCount = 0
        val playersWinrates: Map<Long, Float> = matches.asSequence()
            .flatMap(MatchModel::teams)
            .map(MatchTeamModel::players)
            .flatten()
            .filter { it.userId != null }
            .toList()
            .parallelMap(30) { player ->
                (player.userId!! to getPlayersStatsUseCase.getPlayerStats(
                    userId = player.userId,
                    preset = preset,
                    limit = limit,
                    map = map,
                    fromTime = fromTime
                ).winrate)
            }.associate { it }
        val getTeamAverageWinrate: List<PlayerModel>.() -> Float = {
            map {
                it.userId ?: return@map 50f
                playersWinrates.getValue(it.userId)
            }.sum() / size
        }
        for (match in matches) {
            if (match.withoutResult) continue
            val playerTeam = match.playerTeam(userId) ?: continue
            val firstTeam = match.teams.first()
            val secondTeam = match.teams[1]
            val firstTeamWinrate = firstTeam.players.getTeamAverageWinrate()
            val secondTeamWinrate = secondTeam.players.getTeamAverageWinrate()
            if (abs(secondTeamWinrate - firstTeamWinrate) > 5f) {
                unbalancedMatchesCount++
                if (playerTeam.winningTeam) wonUnbalancedMatches++
                if (playerTeam == firstTeam && secondTeamWinrate > firstTeamWinrate) {
                    if (playerTeam.winningTeam) wonUnbalancedMatchesInWeakerTeam++
                    else lostUnbalancedMatchesInWeakerTeam++
                }
                if (playerTeam == secondTeam && firstTeamWinrate > secondTeamWinrate) {
                    if (playerTeam.winningTeam) wonUnbalancedMatchesInWeakerTeam++
                    else lostUnbalancedMatchesInWeakerTeam++
                }
            }
        }
        return UnbalancedMatchesStats(
            wonUnbalancedMatches = wonUnbalancedMatches,
            wonUnbalancedMatchesInWeakerTeam = wonUnbalancedMatchesInWeakerTeam,
            lostUnbalancedMatchesInWeakerTeam = lostUnbalancedMatchesInWeakerTeam,
            unbalancedMatchesCount = unbalancedMatchesCount
        )
    }
}
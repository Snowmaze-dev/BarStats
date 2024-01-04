package ru.snowmaze.barstats.usecases

import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString
import ru.snowmaze.barstats.defaultParseDateToSeconds
import ru.snowmaze.barstats.models.IntermediateMapModel
import ru.snowmaze.barstats.models.MapStat
import ru.snowmaze.barstats.models.PlayerData
import ru.snowmaze.barstats.models.external.MatchModel
import ru.snowmaze.barstats.models.external.MatchTeamModel
import ru.snowmaze.barstats.parallelMap
import ru.snowmaze.barstats.repository.GetMatchesRepository
import ru.snowmaze.barstats.repository.PlayersRepository

class GatherDataModel(val shouldGatherSkillsStats: Boolean = false, val shouldGatherMapsStats: Boolean = false)

class GetPlayerStatsUseCase(
    private val getMatchesRepository: GetMatchesRepository,
    private val playersRepository: PlayersRepository,
    private val stringFormat: StringFormat
) {

    companion object {
        const val LOAD_MATCHES_BATCH_SIZE = 20
    }

    suspend fun getPlayerStats(
        userId: Long,
        preset: String,
        limit: Int? = null,
        map: String? = null,
        fromTime: Long? = null,
        gatherDataModel: GatherDataModel = GatherDataModel()
    ) = getPlayerStats(
        playerName = playersRepository.getPlayer(userId).username,
        preset = preset,
        limit = limit,
        map = map,
        fromTime = fromTime,
        gatherDataModel = gatherDataModel
    )

    suspend fun getPlayerStats(
        playerName: String,
        preset: String,
        limit: Int? = null,
        map: String? = null,
        fromTime: Long? = null,
        gatherDataModel: GatherDataModel = GatherDataModel()
    ): PlayerData {
        var initialData = getMatchesRepository.getPlayerMatches(playerName, preset).asSequence()
        if (limit != null) initialData = initialData.take(limit)
        if (!map.isNullOrBlank()) initialData = initialData.filter {
            it.map.fileName?.contains(map, ignoreCase = true) == true
        }
        if (fromTime != null) {
            initialData = initialData.filter {
                defaultParseDateToSeconds(it.startTime) > fromTime
            }
        }
        val data = initialData.toList()
        val totalMatchesCount = data.size
        var accountedMatchesCount = 0
        var wonMatchesCount = 0
        var lostMatchesCount = 0
        val userId = playersRepository.getPlayer(playerName).id
        var teammatesCount = 0
        var teammatesSkill = 0f
        playersRepository.addPlayerNickname(userId, playerName)
        val shouldGatherSkillsStats = gatherDataModel.shouldGatherSkillsStats
        val extendedMatches = if (shouldGatherSkillsStats) data.parallelMap(LOAD_MATCHES_BATCH_SIZE) { match ->
            getMatchesRepository.getExtendedMatch(match.id)
        } else data

        val mapsData = if (gatherDataModel.shouldGatherMapsStats) mutableMapOf<String, IntermediateMapModel>() else null
        val playerNames = playersRepository.getPlayerNicknames(userId)!!
        val mappedData = buildList(extendedMatches.size) {
            for (match in extendedMatches) {
                val playerTeam = if (match is MatchModel) match.playerTeam(userId) else {
                    match.playerTeam(playerNames.value.firstOrNull { match.playerTeam(it) != null } ?: run {
                        val currentPlayerName = getMatchesRepository.getExtendedMatch(match.id).player(userId)?.name
                        playersRepository.addPlayerNickname(userId, currentPlayerName ?: return@run null)
                        currentPlayerName
                    } ?: continue)
                } ?: continue
                for (team in match.teams) {
                    if (team is MatchTeamModel) {
                        for (player in team.players) {
                            playersRepository.addPlayerNickname(player.userId ?: continue, player.name)
                        }
                    }
                }
                val isWon = playerTeam.winningTeam
                if (playerTeam is MatchTeamModel) {
                    playerTeam.players.forEach {
                        val skill = try {
                            stringFormat.decodeFromString<FloatArray>(it.skill).firstOrNull()
                        } catch (e: Exception) {
                            it.skill.toFloatOrNull()
                        } ?: return@forEach
                        teammatesCount++
                        teammatesSkill += skill
                    }
                }
                val mapStat = mapsData?.getOrPut(match.mapName) {
                    IntermediateMapModel(match.mapName, 0, 0)
                }
                accountedMatchesCount++
                if (isWon) {
                    wonMatchesCount++
                    if (mapStat != null) mapStat.wins++
                } else {
                    lostMatchesCount++
                    if (mapStat != null) mapStat.loses++
                }
                add(match)
            }
        }

        return PlayerData(
            userId = userId,
            playerName = playerName,
            totalMatchesCount = totalMatchesCount,
            accountedMatchesCount = accountedMatchesCount,
            wonMatchesCount = wonMatchesCount,
            lostMatchesCount = lostMatchesCount,
            winrate = (wonMatchesCount / accountedMatchesCount.toFloat()) * 100f,
            matches = mappedData,
            averageTeammateSkill = (teammatesSkill / teammatesCount).takeUnless { it.isNaN() },
            mapsStats = mapsData?.map {
                val value = it.value
                MapStat(value.mapName, value.wins, value.loses)
            }?.sortedByDescending {
                it.totalMatchesCount
            }
        )
    }
}
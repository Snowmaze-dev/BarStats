package ru.snowmaze.barstats.usecases

import kotlinx.serialization.StringFormat
import ru.snowmaze.barstats.defaultParseDateToSeconds
import ru.snowmaze.barstats.models.IntermediateMapModel
import ru.snowmaze.barstats.models.MapStat
import ru.snowmaze.barstats.models.PlayerData
import ru.snowmaze.barstats.models.external.AbstractMatchModel
import ru.snowmaze.barstats.models.external.AbstractTeamModel
import ru.snowmaze.barstats.models.external.MatchModel
import ru.snowmaze.barstats.models.external.MatchTeamModel
import ru.snowmaze.barstats.parallelMap
import ru.snowmaze.barstats.repository.GetMatchesRepository
import ru.snowmaze.barstats.repository.PlayersRepository

class GatherDataModel(
    val shouldGatherSkillsStats: Boolean = false,
    val shouldGatherMapsStats: Boolean = false,
    val shouldAddMatchesToResult: Boolean = false
)

class GetPlayerStatsUseCase(
    private val getMatchesRepository: GetMatchesRepository,
    private val playersRepository: PlayersRepository,
    private val stringFormat: StringFormat
) {

    companion object {
        const val LOAD_PARALLELISM = 20
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

    // TODO calculate overall player game time
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
        val dataList = initialData.toList()
        val totalMatchesCount = dataList.size
        var accountedMatchesCount = 0
        var wonMatchesCount = 0
        var lostMatchesCount = 0
        val userId = playersRepository.getPlayer(playerName).id
        var teammatesCount = 0
        var teammatesSkill = 0f
        playersRepository.addPlayerNickname(userId, playerName)
        val shouldGatherSkillsStats = gatherDataModel.shouldGatherSkillsStats
        val extendedMatches = if (shouldGatherSkillsStats) dataList.parallelMap(LOAD_PARALLELISM) {
            getMatchesRepository.getExtendedMatch(it.id)
        } else dataList
        val isDuel = preset == "duel"

        val mapsData = if (gatherDataModel.shouldGatherMapsStats) {
            mutableMapOf<String, IntermediateMapModel>()
        } else null
        val playerNames = playersRepository.getPlayerNicknames(userId)!!
        var overallPlayerPlaytimeMs = 0L
        val mappedData = if (gatherDataModel.shouldAddMatchesToResult) {
            ArrayList<AbstractMatchModel>(extendedMatches.size)
        } else null
        for (match in extendedMatches) {
            if (match.withoutResult) continue
            overallPlayerPlaytimeMs += match.durationMs
            val playerTeam = getPlayerTeam(match, userId, playerNames.value) ?: continue
            for (team in match.teams) {
                if (team is MatchTeamModel) {
                    for (player in team.players) {
                        playersRepository.addPlayerNickname(
                            player.userId ?: continue,
                            player.name
                        )
                    }
                }
            }
            val isWon = playerTeam.winningTeam
            if (isDuel) {
                for (team in match.teams) {
                    if (team !is MatchTeamModel || team == playerTeam) continue
                    val enemyPlayerSkill = team.players.firstOrNull()?.getSkill(stringFormat)
                        ?: continue
                    teammatesCount++
                    teammatesSkill += enemyPlayerSkill
                }
            }
            if (playerTeam is MatchTeamModel) {
                playerTeam.players.forEach {
                    if (it.userId == userId) return@forEach
                    val skill = it.getSkill(stringFormat) ?: return@forEach
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
            mappedData?.add(match)
        }
        val averagePlayersSkill = (teammatesSkill / teammatesCount).takeUnless { it.isNaN() }
        return PlayerData(
            userId = userId,
            playerNames = playerNames.value,
            preset = preset,
            totalMatchesCount = totalMatchesCount,
            accountedMatchesCount = accountedMatchesCount,
            wonMatchesCount = wonMatchesCount,
            lostMatchesCount = lostMatchesCount,
            winrate = (wonMatchesCount / accountedMatchesCount.toFloat()) * 100f,
            matches = mappedData,
            overallPlayerPlaytimeMs = overallPlayerPlaytimeMs,
            averageTeammateSkill = if (isDuel) null else averagePlayersSkill,
            averageEnemySkill = if (isDuel) averagePlayersSkill else null,
            mapsStats = mapsData?.map {
                val value = it.value
                MapStat(value.mapName, value.wins, value.loses)
            }?.sortedByDescending {
                it.totalMatchesCount
            }
        )
    }

    private suspend fun getPlayerTeam(
        match: AbstractMatchModel,
        userId: Long,
        playerNames: Set<String>
    ): AbstractTeamModel? {
        if (match is MatchModel) return match.playerTeam(userId)
        val playerNickname = playerNames.firstOrNull {
            match.playerTeam(it) != null
        } ?: run {
            val currentPlayerName = getMatchesRepository.getExtendedMatch(match.id)
                .player(userId)?.name ?: return@run null
            playersRepository.addPlayerNickname(userId, currentPlayerName)
            currentPlayerName
        } ?: return null
        return match.playerTeam(playerNickname)
    }
}
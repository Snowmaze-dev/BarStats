package ru.snowmaze.barstats.models.external

import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class SimpleMatchesResponse(val data: List<SimpleMatchModel>, val responseTime: Long = Clock.System.now().epochSeconds)

@Serializable
data class SimpleMatchModel(
    override val id: String,
    override val startTime: String,
    override val durationMs: Long,
    @SerialName("Map") val map: SimplifiedMapModel,
    @SerialName("AllyTeams") override val teams: List<SimpleTeamModel>
) : AbstractMatchModel() {

    override val mapName: String = map.scriptName

    override val mapFilename = map.fileName
}

@Serializable
class SimplifiedMapModel(val fileName: String? = null, val scriptName: String)

@Serializable
data class SimpleTeamModel(
    override val winningTeam: Boolean,
    @SerialName("Players") override val players: List<SimplePlayerModel>,
) : AbstractTeamModel()

@Serializable
class SimplePlayerModel(override val name: String) : AbstractPlayerModel()
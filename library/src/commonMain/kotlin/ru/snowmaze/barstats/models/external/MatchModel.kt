package ru.snowmaze.barstats.models.external

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.StringFormat
import kotlinx.serialization.decodeFromString

@Serializable
data class MatchModel(
    override val id: String,
    val fileName: String,
    val engineVersion: String,
    val gameVersion: String,
    override val startTime: String,
    override val durationMs: Long,
    val fullDurationMs: Long,
    val hostSettings: Map<String, String>,
    val gameSettings: Map<String, String>,
    val mapSettings: Map<String, String>,
    val spadsSettings: Map<String, String>? = null,
    val hasBots: Boolean,
    val preset: String,
    val reported: Boolean,
    val awards: AwardsModel? = null,
    val mapId: Int,
    @SerialName("Map") val map: MapModel,
    val createdAt: String,
    val updatedAt: String,
    @SerialName("AllyTeams") override val teams: List<MatchTeamModel>
) : AbstractMatchModel() {

    override val mapName = map.scriptName
    override val mapFilename: String? = map.fileName

    fun playerTeam(userId: Long) = teams.firstOrNull {
        it.players.firstOrNull { player -> player.userId == userId } != null
    }

    fun player(userId: Long) = playerTeam(userId)?.players?.firstOrNull { it.userId == userId }
}

@Serializable
class MapModel(
    val id: Long,
    val scriptName: String,
    val fileName: String? = null,
    val width: Int? = null,
    val height: Int? = null
)

@Serializable
data class MatchTeamModel(
    val id: Long,
    val allyTeamId: Int,
    override val winningTeam: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val demoId: String,
    @SerialName("Players") override val players: List<PlayerModel>,
) : AbstractTeamModel()

@Serializable
data class PlayerModel(
    val id: Long,
    val playerId: Int,
    override val name: String,
    val teamId: Int,
    val handicap: Int,
    val faction: String,
    val countryCode: String? = null,
    val rgbColor: ColorModel?,
    val rank: Int? = null,
    val clanId: Int? = null,
    val skillUncertainty: Float?,
    val skill: String,
//    val trueSkill: FloatArray?,
//    val trueSkillMuBefore: FloatArray?,
//    val trueSkillSigmaBefore: FloatArray?,
//    val trueSkillMuAfter: FloatArray?,
//    val trueSkillSigmaAfter: FloatArray?,
    val createdAt: String,
    val updatedAt: String,
    val allyTeamId: Long,
    val userId: Long? = null,
    val startPos: StartPos? = null
) : AbstractPlayerModel() {

    fun getSkill(stringFormat: StringFormat) = try {
        stringFormat.decodeFromString<FloatArray>(skill).firstOrNull()
    } catch (e: Exception) {
        skill.toFloatOrNull()
    }

    override fun toString(): String {
        return "PlayerModel(id=$id, playerId=$playerId, name='$name')"
    }
}

@Serializable
data class StartPos(val x: Float, val y: Float, val z: Float)

@Serializable
data class ColorModel(val r: Float, val g: Float, val b: Float)
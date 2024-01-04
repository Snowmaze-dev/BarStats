package ru.snowmaze.barstats.models.external

abstract class AbstractMatchModel {

    abstract val id: String
    abstract val startTime: String
    abstract val durationMs: Long
    abstract val mapName: String
    abstract val teams: List<AbstractTeamModel>

    val withoutResult get() = teams.all { !it.winningTeam }

    fun playerTeam(playerName: String) = teams.firstOrNull {
        it.players.firstOrNull { player -> player.name.equals(playerName, ignoreCase = true) } != null
    }

    fun playerTeam(playerNames: Set<String>) = teams.firstOrNull {
        it.players.firstOrNull { player -> playerNames.contains(player.name) } != null
    }
}

abstract class AbstractTeamModel {

    abstract val winningTeam: Boolean

    abstract val players: List<AbstractPlayerModel>
}

abstract class AbstractPlayerModel {

    abstract val name: String

}
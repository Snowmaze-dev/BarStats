package ru.snowmaze.barstats

import ru.snowmaze.barstats.models.WithPlayerStat

class SelectedPlayerStat(
    val playerNickNames: Collection<String>,
    val withPlayerStat: WithPlayerStat,
    val isEnemy: Boolean
)
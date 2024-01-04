package ru.snowmaze.barstats.models.external

import kotlinx.serialization.Serializable

@Serializable
class AwardsModel(
    val econDestroyed: List<TeamFloatValue>? = null,
    val fightingUnitsDestroyed: List<TeamFloatValue>? = null,
    val resourceEfficiency: List<TeamFloatValue>? = null,
    val cow: CowAward? = null,
    val mostResourcesProduced: TeamFloatValue? = null,
    val mostDamageTaken: TeamFloatValue? = null,
    val sleep: TeamFloatValue? = null
)

@Serializable
class TeamFloatValue(val teamId: Int, val value: Float)

@Serializable
class CowAward(val teamId: Int)
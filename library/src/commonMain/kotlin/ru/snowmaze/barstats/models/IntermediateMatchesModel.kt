package ru.snowmaze.barstats.models

import ru.snowmaze.barstats.models.external.AbstractMatchModel

data class IntermediateMatchesModel(
    val playerName: String,
    var wins: Int,
    var loses: Int,
    val matchesTogether: MutableList<AbstractMatchModel>? = null
)
package ru.snowmaze.barstats.models

import ru.snowmaze.barstats.models.external.AbstractMatchModel

data class PlayerStats(
    val name: String,
    val totalGamesTogether: Int,
    val wonGames: Int,
    val lostGames: Int,
    val winrate: Float,
    val matchesTogether: List<AbstractMatchModel>?
)
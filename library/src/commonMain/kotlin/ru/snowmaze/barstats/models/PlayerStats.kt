package ru.snowmaze.barstats.models

data class PlayerStats(val name: String, val totalGamesTogether: Int, val wonGames: Int, val lostGames: Int, val winrate: Float)
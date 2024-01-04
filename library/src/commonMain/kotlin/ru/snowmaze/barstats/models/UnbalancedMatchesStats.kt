package ru.snowmaze.barstats.models

class UnbalancedMatchesStats(
    val wonUnbalancedMatches: Int,
    val wonUnbalancedMatchesInWeakerTeam: Int,
    val lostUnbalancedMatchesInWeakerTeam: Int,
    val unbalancedMatchesCount: Int
)
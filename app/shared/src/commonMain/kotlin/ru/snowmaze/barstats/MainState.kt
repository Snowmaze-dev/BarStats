package ru.snowmaze.barstats

import ru.snowmaze.barstats.models.GetStatisticsResult
import ru.snowmaze.barstats.models.PlayerData

sealed class MainState {

    class Loading(val playerName: String) : MainState()

    object Empty : MainState()

    class PartOfDataReady(val data: PlayerData) : MainState()

    class Ready(val getStatisticsResult: GetStatisticsResult) : MainState()

    class Error(val message: String) : MainState()
}
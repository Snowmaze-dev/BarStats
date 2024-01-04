package ru.snowmaze.barstats

import ru.snowmaze.barstats.usecases.GetStatisticsResult

sealed class MainState {

    class Loading(val playerName: String) : MainState()

    object Empty : MainState()

    class Ready(val getStatisticsResult: GetStatisticsResult) : MainState()
}
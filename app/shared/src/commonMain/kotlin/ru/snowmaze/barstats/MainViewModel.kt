package ru.snowmaze.barstats

import dev.icerock.moko.mvvm.viewmodel.ViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.snowmaze.barstats.usecases.GetStatisticsUseCase
import java.lang.Exception

class MainViewModel(private val getStatisticsUseCase: GetStatisticsUseCase) : ViewModel() {

    val playerName = MutableStateFlow("")
    val preset = MutableStateFlow("all")
    private val _state = MutableStateFlow<MainState>(MainState.Empty)
    private var job: Job? = null
    val state: StateFlow<MainState> = _state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = MainState.Empty
    )

    fun getPlayerStats(
        map: String?,
        limitCount: Int?,
        minGamesForStats: Int?
    ) {
        job?.cancel()
        job = viewModelScope.launch {
            val playerName = playerName.value
            val preset = preset.value
            if (playerName.isBlank()) return@launch
            _state.value = MainState.Loading(playerName)
            val result = try {
                withContext(Dispatchers.IO) {
                    getStatisticsUseCase.getStatistics(
                        playerName = playerName,
                        preset = preset,
                        map = map.takeIf { !it.isNullOrBlank() },
                        limit = limitCount,
                        minGamesForStats = minGamesForStats ?: 10
                    ) { playerData ->
                        withContext(Dispatchers.Main) {
                            _state.value = MainState.PartOfDataReady(playerData)
                        }
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) return@launch
                e.printStackTrace()
                val message = e.message
                _state.value = MainState.Error(
                    if (message.isNullOrBlank()) e.javaClass.simpleName
                    else message
                )
                return@launch
            }
            _state.value = MainState.Ready(result)
        }
    }
}
package com.shiwa.onearun.finish

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shiwa.onearun.OneARunApplication
import com.shiwa.onearun.data.repository.LocationPoint
import com.shiwa.onearun.data.repository.RaceRepository
import com.shiwa.onearun.race.RaceUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FinishUiState(
    val isWinner: Boolean = true,
    val topSpeedText: String = "5:12min/km",
    val distanceText: String = "500m",
    val elapsedTimeText: String = "3:21",
    val routePoints: List<LocationPoint> = emptyList()
)

class FinishViewModel(
    private val repository: RaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinishUiState())
    val uiState: StateFlow<FinishUiState> = _uiState.asStateFlow()

    init {
        observeRaceStats()
    }

    private fun observeRaceStats() {
        viewModelScope.launch {
            repository.raceStats.collect { stats ->
                val session = repository.currentSession.value
                if (stats != null) {
                    val isWinner =
                        stats.won ?: (stats.myDistanceMeters >= stats.opponentDistanceMeters)

                    val topSpeedFormatted = if (stats.bestPaceSecPerKm != null) {
                        "${RaceUtils.formatPace(stats.bestPaceSecPerKm)}min/km"
                    } else {
                        "5:12min/km"
                    }

                    val distanceInt = if (stats.myDistanceMeters > 0) {
                        stats.myDistanceMeters.toInt()
                    } else {
                        session?.distanceMeters ?: 500
                    }
                    val distanceFormatted = "${distanceInt}m"

                    val elapsedMillis = stats.myFinishMillis ?: stats.raceDurationMillis ?: 0L
                    val timeFormatted = RaceUtils.formatTime(elapsedMillis)

                    _uiState.update {
                        it.copy(
                            isWinner = isWinner,
                            topSpeedText = topSpeedFormatted,
                            distanceText = distanceFormatted,
                            elapsedTimeText = if (timeFormatted == "00:00") "3:21" else timeFormatted,
                            routePoints = stats.routePoints
                        )
                    }
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as OneARunApplication)
                FinishViewModel(
                    repository = application.container.raceRepository
                )
            }
        }
    }
}

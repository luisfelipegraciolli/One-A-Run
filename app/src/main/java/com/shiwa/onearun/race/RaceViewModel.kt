package com.shiwa.onearun.race

import android.annotation.SuppressLint
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.gson.Gson
import com.shiwa.onearun.OneARunApplication
import com.shiwa.onearun.data.location.LocationDataSource
import com.shiwa.onearun.data.repository.RaceRepository
import com.shiwa.onearun.data.repository.RaceStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

data class RaceUiState(
    val myName: String = "Você",
    val opponentName: String = "Adversário",
    val myDistanceMeters: Double = 0.0,
    val opponentDistanceMeters: Double = 0.0,
    val targetDistanceMeters: Double = 400.0,
    val isFirstPlace: Boolean = true,
    val paceText: String = "--:--",
    val timeText: String = "00:00",
    val isFinished: Boolean = false,
    val isOpponentFinished: Boolean = false,
    val sessionFinished: Boolean = false,
    val isWaitingOpponentToFinish: Boolean = false,
    val errorMessage: String? = null
)

data class ProgressPoint(
    val distanceMeters: Double,
    val timestampMillis: Long
)

data class MqttProgressDto(
    val sessionId: String? = null,
    val runnerId: String? = null,
    val distanceMeters: Double = 0.0,
    val opponentRunnerId: String? = null,
    val opponentDistanceMeters: Double = 0.0,
    val targetDistanceMeters: Double = 400.0,
    val remainingMeters: Double? = null,
    val opponentRemainingMeters: Double? = null,
    val finished: Boolean = false,
    val opponentFinished: Boolean = false,
    val sessionStatus: String? = null,
    val winnerRunnerId: String? = null,
    val won: Boolean? = null
)

data class MqttResultDto(
    val sessionId: String? = null,
    val sessionStatus: String? = null,
    val targetDistanceMeters: Double? = null,
    val winnerRunnerId: String? = null,
    val hostRunnerId: String? = null,
    val hostDistanceMeters: Double? = null,
    val guestRunnerId: String? = null,
    val guestDistanceMeters: Double? = null
)

object RaceUtils {
    fun calculatePace(
        history: List<ProgressPoint>,
        nowMillis: Long,
        windowMillis: Long = 10_000L
    ): Int? {
        val windowPoints = history.filter { it.timestampMillis >= nowMillis - windowMillis }
        if (windowPoints.size < 2) return null

        val oldest = windowPoints.first()
        val latest = windowPoints.last()

        val deltaDistance = latest.distanceMeters - oldest.distanceMeters
        val deltaTimeSec = (latest.timestampMillis - oldest.timestampMillis) / 1000.0

        if (deltaDistance < 10.0 || deltaTimeSec <= 0.0) return null

        val speedMetersPerSec = deltaDistance / deltaTimeSec
        if (speedMetersPerSec <= 0.0) return null

        val paceSecPerKm = (1000.0 / speedMetersPerSec).roundToInt()

        if (paceSecPerKm !in 120..1200) return null

        return paceSecPerKm
    }

    fun formatPace(paceSecPerKm: Int?): String {
        if (paceSecPerKm == null) return "--:--"
        val minutes = paceSecPerKm / 60
        val seconds = paceSecPerKm % 60
        return "%d:%02d".format(minutes, seconds)
    }

    fun formatTime(elapsedMillis: Long): String {
        val totalSeconds = (elapsedMillis / 1000).coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%02d:%02d".format(minutes, seconds)
        }
    }

    fun calculateIsFirstPlace(
        myDistance: Double,
        opponentDistance: Double,
        won: Boolean?,
        winnerRunnerId: String?
    ): Boolean {
        if (winnerRunnerId != null) {
            if (won == true) return true
            if (won == false) return false
        }
        return myDistance >= opponentDistance
    }
}

class RaceViewModel(
    private val repository: RaceRepository,
    private val locationDataSource: LocationDataSource? = null,
    private val timeProvider: () -> Long = { SystemClock.elapsedRealtime() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(RaceUiState())
    val uiState: StateFlow<RaceUiState> = _uiState.asStateFlow()

    private val gson = Gson()
    private val progressHistory = mutableListOf<ProgressPoint>()

    private var startTimeMillis: Long = 0L
    private var bestPaceSecPerKm: Int? = null
    private var myFinishMillis: Long? = null
    private var isRaceFinishedRecorded = false

    private var timerJob: Job? = null
    private var progressJob: Job? = null
    private var resultJob: Job? = null
    private var locationJob: Job? = null

    init {
        val session = repository.currentSession.value
        val sessionId = session?.sessionId
        val runnerId = session?.runnerId

        if (sessionId.isNullOrBlank() || runnerId.isNullOrBlank()) {
            _uiState.update {
                it.copy(errorMessage = "Sessão ou jogador não encontrados. Volte e tente novamente.")
            }
        } else {
            startTimeMillis = timeProvider()
            startRaceSession(sessionId, runnerId)
        }
    }

    private fun startRaceSession(sessionId: String, runnerId: String) {
        fetchRunnerNames(sessionId, runnerId)
        startTimer()
        viewModelScope.launch {
            try {
                repository.connectToBroker()
            } catch (e: Exception) {
                Log.e(TAG, "Erro ao conectar ao broker MQTT", e)
            }
            observeProgress(sessionId, runnerId)
            observeResult(sessionId, runnerId)
            startLocationTracking()
        }
    }

    private fun fetchRunnerNames(sessionId: String, runnerId: String) {
        viewModelScope.launch {
            repository.getRaceSession(sessionId).onSuccess { details ->
                val isHost = (runnerId == details.hostRunnerId)
                val rawMyName = if (isHost) details.hostRunnerName else details.guestRunnerName
                val rawOpponentName =
                    if (isHost) details.guestRunnerName else details.hostRunnerName

                val myName = if (!rawMyName.isNullOrBlank()) rawMyName else "Você"
                val opponentName =
                    if (!rawOpponentName.isNullOrBlank()) rawOpponentName else "Adversário"

                _uiState.update {
                    it.copy(
                        myName = myName,
                        opponentName = opponentName
                    )
                }
            }.onFailure {
                Log.w(TAG, "Falha ao carregar detalhes da corrida para obter nomes", it)
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (!_uiState.value.sessionFinished) {
                val elapsed = (timeProvider() - startTimeMillis).coerceAtLeast(0L)
                val timeStr = RaceUtils.formatTime(elapsed)
                _uiState.update { it.copy(timeText = timeStr) }
                delay(500.milliseconds)
            }
        }
    }

    private fun observeProgress(sessionId: String, runnerId: String) {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            val topic = "race/$sessionId/runner/$runnerId/progress"
            repository.observeTopic(topic)
                .catch { Log.w(TAG, "Erro ao assinar progresso da corrida", it) }
                .collect { json ->
                    handleProgressMessage(json, runnerId)
                }
        }
    }

    private fun observeResult(sessionId: String, runnerId: String) {
        resultJob?.cancel()
        resultJob = viewModelScope.launch {
            val topic = "race/$sessionId/result"
            repository.observeTopic(topic)
                .catch { Log.w(TAG, "Erro ao assinar resultado da corrida", it) }
                .collect { json ->
                    handleResultMessage(json, runnerId)
                }
        }
    }

    private fun handleProgressMessage(json: String, runnerId: String) {
        runCatching {
            val dto = gson.fromJson(json, MqttProgressDto::class.java) ?: return
            val now = timeProvider()

            progressHistory.add(ProgressPoint(dto.distanceMeters, now))

            val currentPaceSecPerKm = RaceUtils.calculatePace(progressHistory, now)
            if (currentPaceSecPerKm != null) {
                val prevBest = bestPaceSecPerKm
                if (prevBest == null || currentPaceSecPerKm < prevBest) {
                    bestPaceSecPerKm = currentPaceSecPerKm
                }
            }
            val formattedPace = RaceUtils.formatPace(currentPaceSecPerKm)

            val finishedNow = dto.finished
            if (finishedNow && myFinishMillis == null) {
                myFinishMillis = now - startTimeMillis
            }

            val sessionIsFinished = (dto.sessionStatus == "FINISHED")
            val isWaiting = finishedNow && !sessionIsFinished

            val wonValue = dto.won
                ?: if (dto.winnerRunnerId != null) (dto.winnerRunnerId == runnerId) else null
            val isFirst = RaceUtils.calculateIsFirstPlace(
                dto.distanceMeters,
                dto.opponentDistanceMeters,
                wonValue,
                dto.winnerRunnerId
            )

            _uiState.update { current ->
                current.copy(
                    myDistanceMeters = dto.distanceMeters,
                    opponentDistanceMeters = dto.opponentDistanceMeters,
                    targetDistanceMeters = dto.targetDistanceMeters,
                    isFirstPlace = isFirst,
                    paceText = formattedPace,
                    isFinished = finishedNow,
                    isOpponentFinished = dto.opponentFinished,
                    sessionFinished = sessionIsFinished,
                    isWaitingOpponentToFinish = isWaiting
                )
            }

            if (sessionIsFinished) {
                finishRace(wonValue)
            }
        }.onFailure {
            Log.e(TAG, "Erro ao processar mensagem MQTT de progresso", it)
        }
    }

    private fun handleResultMessage(json: String, runnerId: String) {
        runCatching {
            val dto = gson.fromJson(json, MqttResultDto::class.java) ?: return

            val wonValue =
                if (dto.winnerRunnerId != null) (dto.winnerRunnerId == runnerId) else null
            val sessionIsFinished = (dto.sessionStatus == "FINISHED")

            _uiState.update { current ->
                val isFirst = RaceUtils.calculateIsFirstPlace(
                    current.myDistanceMeters,
                    current.opponentDistanceMeters,
                    wonValue,
                    dto.winnerRunnerId
                )
                current.copy(
                    isFirstPlace = isFirst,
                    sessionFinished = sessionIsFinished,
                    isWaitingOpponentToFinish = current.isFinished && !sessionIsFinished
                )
            }

            if (sessionIsFinished) {
                finishRace(wonValue)
            }
        }.onFailure {
            Log.e(TAG, "Erro ao processar mensagem MQTT de resultado", it)
        }
    }

    fun startLocationTracking() {
        if (locationJob != null) return
        val session = repository.currentSession.value
        val sessionId = session?.sessionId
        val runnerId = session?.runnerId

        if (sessionId.isNullOrBlank() || runnerId.isNullOrBlank()) return

        locationJob = viewModelScope.launch {
            locationDataSource?.getLocationUpdates()
                ?.catch { Log.w(TAG, "Erro no fluxo de localização GPS", it) }
                ?.collect { update ->
                    val state = _uiState.value
                    if (!state.isFinished && !state.sessionFinished) {
                        repository.publishLocation(sessionId, runnerId, update)
                    } else {
                        stopLocationTracking()
                    }
                }
        }
    }

    fun stopLocationTracking() {
        locationJob?.cancel()
        locationJob = null
    }

    @SuppressLint("EmptySuperCall")
    override fun onCleared() {
        super.onCleared()
        stopLocationTracking()
    }

    private fun finishRace(wonValue: Boolean?) {
        if (isRaceFinishedRecorded) return
        isRaceFinishedRecorded = true

        timerJob?.cancel()
        stopLocationTracking()

        val totalDuration = timeProvider() - startTimeMillis
        val currentState = _uiState.value

        val stats = RaceStats(
            bestPaceSecPerKm = bestPaceSecPerKm,
            myFinishMillis = myFinishMillis,
            raceDurationMillis = totalDuration,
            myDistanceMeters = currentState.myDistanceMeters,
            opponentDistanceMeters = currentState.opponentDistanceMeters,
            won = wonValue
        )

        repository.updateRaceStats(stats)
    }

    companion object {
        private const val TAG = "RaceViewModel"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as OneARunApplication)
                RaceViewModel(
                    repository = application.container.raceRepository,
                    locationDataSource = application.container.locationDataSource
                )
            }
        }
    }
}
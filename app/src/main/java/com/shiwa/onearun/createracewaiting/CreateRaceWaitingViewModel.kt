package com.shiwa.onearun.createracewaiting

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shiwa.onearun.OneARunApplication
import com.shiwa.onearun.data.repository.RaceRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

data class CreateRaceWaitingUiState(
    val guestJoined: Boolean = false
)

class CreateRaceWaitingViewModel(
    private val repository: RaceRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Argumento de navegação "sessionId" (o mesmo nome usado na rota)
    private val sessionId: String = checkNotNull(savedStateHandle.get<String>("sessionId"))

    private val _uiState = MutableStateFlow(CreateRaceWaitingUiState())
    val uiState: StateFlow<CreateRaceWaitingUiState> = _uiState.asStateFlow()

    private var watchJob: Job? = null

    // O init vem depois das propriedades acima, senão elas ainda seriam null aqui
    init {
        startWatchingForGuest(sessionId)
    }

    /**
     * Espera o convidado por dois caminhos ao mesmo tempo:
     *  - MQTT (rápido): qualquer mensagem em race/{id}/status quer dizer "algo mudou";
     *  - polling (reserva): cobre mensagem perdida, assinatura que caiu ou broker fora do ar.
     * Em ambos, a API (GET races/{id}) é quem decide; o texto da mensagem MQTT nunca é confiado.
     */
    private fun startWatchingForGuest(sessionId: String) {
        watchJob?.cancel()
        watchJob = viewModelScope.launch {
            // O MQTT roda em uma coroutine à parte: se o broker demorar ou falhar, o polling não espera por ele
            val statusListener = launch {
                repository.observeTopic("race/$sessionId/status")
                    .catch {
                        Log.w(
                            TAG,
                            "Sem MQTT no tópico de status; seguindo só com polling",
                            it
                        )
                    }
                    .collect { refreshRace(sessionId) }
            }

            while (!_uiState.value.guestJoined) {
                refreshRace(sessionId)
                delay(POLL_INTERVAL_MS.milliseconds)
            }
            statusListener.cancel()
        }
    }

    private suspend fun refreshRace(sessionId: String) {
        repository.getRaceSession(sessionId).onSuccess { race ->

            if (race.guestRunnerId != null) {
                _uiState.update { it.copy(guestJoined = true) }
            }
        }
    }

    companion object {
        private const val TAG = "CreateRaceWaitingVM"
        private const val POLL_INTERVAL_MS = 5_000L

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as OneARunApplication)
                CreateRaceWaitingViewModel(
                    repository = application.container.raceRepository,
                    // Recebe os argumentos de navegação da tela (inclusive o sessionId)
                    savedStateHandle = createSavedStateHandle()
                )
            }
        }
    }
}
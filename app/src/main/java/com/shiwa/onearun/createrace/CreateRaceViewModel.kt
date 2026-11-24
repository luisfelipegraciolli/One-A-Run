package com.shiwa.onearun.createrace

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shiwa.onearun.OneARunApplication
import com.shiwa.onearun.data.repository.RaceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CreateRaceUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hostName: String = "",
    val distanceInput: String = "",
    val isCreated: Boolean = false,
    val joinCode: String = "",
    val sessionId: String = "",
    val guestJoined: Boolean = false
)

class CreateRaceViewModel(
    private val repository: RaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRaceUiState())
    val uiState: StateFlow<CreateRaceUiState> = _uiState.asStateFlow()

    fun onHostNameChanged(name: String) {
        _uiState.update { it.copy(hostName = name, errorMessage = null) }
    }

    fun onDistanceChanged(distance: String) {
        _uiState.update { it.copy(distanceInput = distance, errorMessage = null) }
    }

    fun createRace(onSuccess: (sessionId: String, joinCode: String, hostName: String, distance: String) -> Unit = { _, _, _, _ -> }) {
        val currentState = _uiState.value

        if (currentState.hostName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor, preencha o seu nome.") }
            return
        }

        val numericDistance = currentState.distanceInput.replace(Regex("[^0-9]"), "").toIntOrNull()
        if (numericDistance == null || numericDistance !in 100..1000) {
            _uiState.update { it.copy(errorMessage = "A distância para criação deve ser de 100m a 1000m.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.createRaceSession(
                distanceInMeters = numericDistance,
                hostRunnerName = currentState.hostName.trim()
            )
            if (result.isSuccess) {
                val response = result.getOrNull()
                val code = response?.joinCode ?: ""
                val session = response?.sessionId ?: ""
                val hostName = response?.hostRunnerName ?: currentState.hostName

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCreated = true,
                        joinCode = code,
                        sessionId = session
                    )
                }
                onSuccess(session, code, hostName, "${numericDistance}m")
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Erro ao criar corrida"
                    )
                }
            }
        }
    }

    fun resetCreatedState() {
        _uiState.update { it.copy(isCreated = false) }
    }
    companion object {
        private const val TAG = "CreateRaceViewModel"
        private const val POLL_INTERVAL_MS = 5_000L
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as OneARunApplication)
                CreateRaceViewModel(repository = application.container.raceRepository)
            }
        }
    }
}

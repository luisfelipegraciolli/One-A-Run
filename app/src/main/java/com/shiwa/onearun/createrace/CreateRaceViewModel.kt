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
    val hostName: String = "João",
    val distanceInput: String = "400m",
    val isCreated: Boolean = false,
    val joinCode: String = "",
    val sessionId: String = ""
)

class CreateRaceViewModel(
    private val repository: RaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateRaceUiState())
    val uiState: StateFlow<CreateRaceUiState> = _uiState.asStateFlow()

    fun onHostNameChanged(name: String) {
        _uiState.update { it.copy(hostName = name) }
    }

    fun onDistanceChanged(distance: String) {
        _uiState.update { it.copy(distanceInput = distance) }
    }

    fun createRace(onSuccess: (joinCode: String, hostName: String, distance: String) -> Unit = { _, _, _ -> }) {
        val currentState = _uiState.value
        val numericDistance = currentState.distanceInput.replace(Regex("[^0-9]"), "").toIntOrNull() ?: 400

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.createRaceSession(
                distanceInMeters = numericDistance,
                hostRunnerName = currentState.hostName
            )
            if (result.isSuccess) {
                val response = result.getOrNull()
                val code = response?.joinCode ?: ""
                val session = response?.sessionId ?: ""
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isCreated = true,
                        joinCode = code,
                        sessionId = session
                    )
                }
                onSuccess(code, currentState.hostName, currentState.distanceInput)
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
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as OneARunApplication)
                CreateRaceViewModel(repository = application.container.raceRepository)
            }
        }
    }
}

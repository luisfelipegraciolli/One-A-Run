package com.shiwa.onearun.joinrace

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

data class JoinRaceUiState(
    val sessionId: String = "",
    val guestRunnerName: String = "",
    val joinCode: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val isJoined: Boolean = false
}

class JoinRaceViewModel(
    private val repository: RaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinRaceUiState())
    val uiState: StateFlow<JoinRaceUiState> = _uiState.asStateFlow()

    fun onGuestRunnerNameChanged(name: String) {
        _uiState.update { it.copy(guestRunnerName = name, errorMessage = null) }
    }

    fun onJoinCodeChanged(code: String) {
        _uiState.update { it.copy(joinCode = code, errorMessage = null) }
    }

    fun joinRace(onSuccess: (sessionId: String) -> Unit = { _ -> }) {
        val currentState = _uiState.value

        // Validação simples dos campos
        if (currentState.guestRunnerName.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor, preencha o seu nome.") }
            return
        }

        if (currentState.joinCode.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Por favor, preencha o código da sessão.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val result = repository.joinRaceSession(
                guestRunnerName = currentState.guestRunnerName.trim(),
                joinCode = currentState.joinCode.trim()
            )

            if (result.isSuccess) {
                val response = result.getOrNull()
                val sessionId = response?.sessionId ?: ""
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        sessionId = sessionId,
                    )
                }
                onSuccess(sessionId)
            } else {
                val error = result.exceptionOrNull()?.message ?: "Erro ao entrar na sessão."
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error
                    )
                }
            }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as OneARunApplication)
                JoinRaceViewModel(repository = application.container.raceRepository)
            }
        }
    }
}

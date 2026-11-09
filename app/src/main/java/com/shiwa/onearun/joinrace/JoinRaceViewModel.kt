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
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val joinCode: String = ""
) {
    val isJoined: Boolean = joinCode.isNotEmpty()
}

class JoinRaceViewModel(
    private val repository: RaceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(JoinRaceUiState())
    val uiState: StateFlow<JoinRaceUiState> = _uiState.asStateFlow()


    fun joinRace(guestRunnerName: String, joinCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Mudar depois para o nome do usuário que ele digitar
            val result = repository.joinRaceSession(guestRunnerName = "Guest", joinCode = joinCode)
            if (result.isSuccess) {
                val response = result.getOrNull()
                val session = response?.sessionId ?: ""
                repository.publishRaceMessage(
                    "$guestRunnerName está na corrida!",
                    topic = "race/$session/status"
                )
                _uiState.update { it.copy(isLoading = false) }
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

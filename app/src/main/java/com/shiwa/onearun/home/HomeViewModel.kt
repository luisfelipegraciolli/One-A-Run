package com.shiwa.onearun.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shiwa.onearun.OneARunApplication
import com.shiwa.onearun.data.remote.MqttConnectionState
import com.shiwa.onearun.data.repository.RaceRepository
import com.shiwa.onearun.data.repository.RaceRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val connectionState: MqttConnectionState = MqttConnectionState.Disconnected,
    val lastReceivedMessage: String? = null,
    val success: Boolean = false,
    val statusMessage: String = "Disconnected"
)

class HomeViewModel(
    private val repository: RaceRepository = RaceRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeRepository()
        connectAndSubscribe()
    }

    private fun observeRepository() {
        viewModelScope.launch {
            repository.connectionState.collect { state ->
                val statusText = when (state) {
                    MqttConnectionState.Connected -> "Connected to Mosquitto Broker"
                    MqttConnectionState.Connecting -> "Connecting to Mosquitto Broker..."
                    MqttConnectionState.Disconnected -> "Disconnected"
                    is MqttConnectionState.Error -> "Error: ${state.message}"
                }
                _uiState.update {
                    it.copy(
                        connectionState = state,
                        statusMessage = statusText
                    )
                }
            }
        }

        viewModelScope.launch {
            repository.incomingMessages.collect { message ->
                _uiState.update {
                    it.copy(lastReceivedMessage = message)
                }
            }
        }
    }

    fun connectAndSubscribe() {
        viewModelScope.launch {
            repository.connectToBroker()
            repository.subscribeToRaceTopic()
        }
    }

    fun onPublishHelloClick(message: String = "Hello from Android!") {
        viewModelScope.launch {
            repository.publishRaceMessage(message)
        }
    }

    fun joinRace() {
        _uiState.update { it.copy(success = true) }
        onPublishHelloClick("Joining a race...")
    }

    fun createRace() {
        _uiState.update { it.copy(success = true) }
        onPublishHelloClick("Creating a race...")
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as OneARunApplication)
                val raceRepository = application.container.raceRepository
                HomeViewModel(repository = raceRepository)
            }
        }
    }
}


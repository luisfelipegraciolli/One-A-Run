package com.shiwa.onearun.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.shiwa.onearun.OneARunApplication
import com.shiwa.onearun.data.repository.RaceRepository
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: RaceRepository
) : ViewModel() {

    init {
        connectToBroker()
    }

    private fun connectToBroker() {
        viewModelScope.launch {
            repository.connectToBroker()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as OneARunApplication)
                HomeViewModel(repository = application.container.raceRepository)
            }
        }
    }
}
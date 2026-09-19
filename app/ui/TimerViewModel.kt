package com.example.timewallet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewallet.data.Repository
import com.example.timewallet.network.AiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UiState(
    val coins: Int = 0,
    val currentTask: String? = null,
    val currentDuration: Int = 0,
    val message: String? = null
)

class TimerViewModel(
    private val repository: Repository
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state

    fun loadCoins() {
        viewModelScope.launch {
            val c = repository.getCoins()
            _state.value = _state.value.copy(coins = c)
        }
    }

    fun startSession(taskType: String, durationMinutes: Int) {
        _state.value = _state.value.copy(
            currentTask = taskType,
            currentDuration = durationMinutes,
            message = "Session gestartet"
        )
        viewModelScope.launch {
            repository.createSession(taskType, durationMinutes)
        }
    }

    fun finishSession(photoPath: String) {
        val task = _state.value.currentTask ?: return
        val duration = _state.value.currentDuration

        viewModelScope.launch {
            val score = AiClient.verifyPhoto(task, photoPath)
            if (score >= 0.7f) {
                val earned = duration / 5
                repository.addCoins(earned)
                val c = repository.getCoins()
                _state.value = _state.value.copy(
                    coins = c,
                    message = "Verifiziert! +$earned Coins"
                )
            } else {
                _state.value = _state.value.copy(
                    message = "Nicht verifiziert, keine Coins"
                )
            }
        }
    }

    fun buySocialTime(minutes: Int) {
        viewModelScope.launch {
            val coins = repository.getCoins()
            val pricePer30 = 10
            val blocks = kotlin.math.ceil(minutes / 30.0).toInt()
            val cost = blocks * pricePer30

            if (coins < cost) {
                _state.value = _state.value.copy(
                    message = "Zu wenig Coins"
                )
                return@launch
            }

            repository.addCoins(-cost)
            repository.setSocialUnlock(minutes)
            val newCoins = repository.getCoins()
            _state.value = _state.value.copy(
                coins = newCoins,
                message = "Social Media für $minutes Minuten freigeschaltet"
            )
        }
    }
}

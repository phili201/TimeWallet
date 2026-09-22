package com.example.timewallet.timer

import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.data.TimeWalletRepository
import com.example.timewallet.data.coins.CoinEntry
import com.example.timewallet.data.session.SessionEntry
import com.example.timewallet.ki.SessionVerifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TimerState(
    val coins: Int = 0,
    val message: String? = null,
    val isRunning: Boolean = false,
    val remainingMinutes: Int = 0,
    val currentTask: String = "",
    val sessionMinutes: Int = 0,
    val elapsedMinutes: Int = 0
)

class TimerViewModel(private val app: TimeWalletApp) : ViewModel() {
    private val repo: TimeWalletRepository = app.repository
    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state
    private var timerJob = viewModelScope.launch { }

    init {
        viewModelScope.launch {
            repo.getCoinBalance().collect { balance ->
                _state.value = _state.value.copy(coins = balance)
            }
        }
    }

    fun startSession(task: String, minutes: Int) {
        val safeMinutes = minutes.coerceIn(1, 180)
        timerJob.cancel()
        repo.startProductivitySession()
        _state.value = _state.value.copy(
            isRunning = true,
            remainingMinutes = safeMinutes,
            sessionMinutes = safeMinutes,
            elapsedMinutes = 0,
            currentTask = task,
            message = "Session gestartet: $task"
        )
        timerJob = viewModelScope.launch {
            while (_state.value.isRunning && _state.value.remainingMinutes > 0) {
                delay(60_000)
                val current = _state.value
                if (!current.isRunning) break
                val remaining = (current.remainingMinutes - 1).coerceAtLeast(0)
                _state.value = current.copy(remainingMinutes = remaining, elapsedMinutes = current.elapsedMinutes + 1)
                if (remaining == 0) {
                    _state.value = _state.value.copy(isRunning = false, message = "Session beendet – bitte Foto machen!")
                }
            }
        }
    }

    fun finishSession(photoPath: String) {
        val current = _state.value
        if (current.sessionMinutes <= 0 || current.isRunning) {
            _state.value = current.copy(message = "Bitte erst die laufende Session vollständig beenden.")
            return
        }
        timerJob.cancel()
        viewModelScope.launch {
            val bitmap = BitmapFactory.decodeFile(photoPath)
            if (bitmap == null) {
                _state.value = _state.value.copy(message = "Foto konnte nicht gelesen werden.")
                return@launch
            }
            val result = SessionVerifier().calculateScore(bitmap)
            val minutes = _state.value.sessionMinutes
            val task = _state.value.currentTask
            val valid = result.score >= 60
            repo.insertSession(
                SessionEntry(
                    minutes = minutes,
                    score = result.score,
                    valid = valid,
                    timestamp = System.currentTimeMillis(),
                    task = task
                )
            )
            repo.endProductivitySession()
            if (valid) {
                val coins = calculateCoins(minutes)
                repo.insertCoin(CoinEntry(coins, "Produktive Session: $task", System.currentTimeMillis()))
                _state.value = _state.value.copy(isRunning = false, remainingMinutes = 0, message = "Session bestätigt ✔ +$coins Coins • Score: ${result.score}")
            } else {
                _state.value = _state.value.copy(isRunning = false, message = "Session abgelehnt ❌ Score: ${result.score} (${result.reason})")
            }
        }
    }

    private fun calculateCoins(minutes: Int): Int = minutes + if (minutes >= 60) 10 else if (minutes >= 30) 5 else 0

    override fun onCleared() {
        timerJob.cancel()
        super.onCleared()
    }
}

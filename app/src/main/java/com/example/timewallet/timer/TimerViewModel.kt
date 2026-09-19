package com.example.timewallet.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.data.TimeWalletRepository
import com.example.timewallet.data.session.SessionEntry
import com.example.timewallet.ki.SessionVerifier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory

data class TimerState(
    val coins: Int = 0,
    val message: String? = null,
    val isRunning: Boolean = false,
    val remainingMinutes: Int = 0,
    val currentTask: String = "",
    val sessionMinutes: Int = 0
)

class TimerViewModel(
    private val app: TimeWalletApp
) : ViewModel() {

    private val repo: TimeWalletRepository = app.repository

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state

    private var timerJob = viewModelScope.launch { }

    fun startSession(task: String, minutes: Int) {
        timerJob.cancel()

        _state.value = _state.value.copy(
            isRunning = true,
            remainingMinutes = minutes,
            sessionMinutes = minutes,
            currentTask = task,
            message = "Session gestartet: $task"
        )

        timerJob = viewModelScope.launch {
            while (_state.value.remainingMinutes > 0) {
                delay(60000)
                _state.value = _state.value.copy(
                    remainingMinutes = _state.value.remainingMinutes - 1
                )
            }

            _state.value = _state.value.copy(
                isRunning = false,
                message = "Session beendet – bitte Foto machen!"
            )
        }
    }

    fun finishSession(photoPath: String) {
        viewModelScope.launch {
            val bitmap = BitmapFactory.decodeFile(photoPath)
            val verifier = SessionVerifier()
            val score = verifier.calculateScore(bitmap)

            val minutes = _state.value.sessionMinutes

            val entry = SessionEntry(
                minutes = minutes,
                score = score.score,
                valid = score.score >= 60,
                timestamp = System.currentTimeMillis()
            )

            repo.insertSession(entry)

            if (score.score >= 60) {
                val coins = calculateCoins(minutes)
                repo.insertCoin(
                    com.example.timewallet.data.coins.CoinEntry(
                        amount = coins,
                        reason = "Produktive Session",
                        timestamp = System.currentTimeMillis()
                    )
                )

                _state.value = _state.value.copy(
                    message = "Session bestätigt ✔ Score: ${score.score}"
                )
            } else {
                _state.value = _state.value.copy(
                    message = "Session abgelehnt ❌ Score: ${score.score} (${score.reason})"
                )
            }
        }
    }

    private fun calculateCoins(minutes: Int): Int {
        var coins = minutes
        if (minutes >= 30) coins += 5
        if (minutes >= 60) coins += 10
        return coins
    }
}

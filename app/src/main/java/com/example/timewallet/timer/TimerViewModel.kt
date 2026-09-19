package com.example.timewallet.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewallet.data.CoinsRepository
import com.example.timewallet.TimeWalletApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class TimerState(
    val coins: Int = 0,
    val message: String? = null,
    val isRunning: Boolean = false,
    val remainingMinutes: Int = 0,
    val currentTask: String = ""
)

class TimerViewModel(
    private val app: TimeWalletApp
) : ViewModel() {

    private val coinsRepo = CoinsRepository(app.database.coinDao())

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state

    private var timerJob = viewModelScope.launch { }

    fun loadCoins() {
        viewModelScope.launch {
            val balance = coinsRepo.getBalance()
            _state.value = _state.value.copy(coins = balance)
        }
    }

    fun startSession(task: String, durationMinutes: Int) {
        timerJob.cancel()

        _state.value = _state.value.copy(
            isRunning = true,
            remainingMinutes = durationMinutes,
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
            val verifier = ImageVerifier()
            val ok = verifier.isProductive(photoPath)

            if (ok) {
                coinsRepo.addCoins(10)
                val newBalance = coinsRepo.getBalance()
                _state.value = _state.value.copy(
                    coins = newBalance,
                    message = "KI bestätigt: produktiv! +10 Coins",
                    isRunning = false
                )
            } else {
                _state.value = _state.value.copy(
                    message = "KI sagt: nicht produktiv – keine Coins",
                    isRunning = false
                )
            }
        }
    }
    }

    fun buySocialTime(minutes: Int) {
        viewModelScope.launch {
            val cost = minutes
            val success = coinsRepo.removeCoins(cost)

            if (success) {
                _state.value = _state.value.copy(
                    message = "$minutes Minuten Social Media gekauft!"
                )
            } else {
                _state.value = _state.value.copy(
                    message = "Nicht genug Coins!"
                )
            }

            loadCoins()
        }
    }
}

import android.graphics.BitmapFactory
import com.example.timewallet.ki.SessionVerifier

suspend fun verifyPhoto(photoPath: String): Boolean {
    val bitmap = BitmapFactory.decodeFile(photoPath)
    val verifier = SessionVerifier()
    return verifier.verifySession(bitmap)
}

fun finishSession(photoPath: String) {
    viewModelScope.launch {
        val valid = verifyPhoto(photoPath)

        if (valid) {
            repository.addCoins(10)
            _state.value = _state.value.copy(message = "Session bestätigt ✔")
        } else {
            _state.value = _state.value.copy(message = "Session abgelehnt ❌")
        }
    }
}

private fun calculateCoins(minutes: Int): Int {
    var coins = minutes // 1 Coin pro Minute

    if (minutes >= 30) coins += 5     // Bonus für lange Sessions
    if (minutes >= 60) coins += 10    // Extra-Bonus

    return coins
}

fun finishSession(photoPath: String) {
    viewModelScope.launch {
        val valid = verifyPhoto(photoPath)

        if (valid) {
            val minutes = _state.value.sessionMinutes
            val coins = calculateCoins(minutes)

            repository.addCoins(coins, "Produktive Session bestätigt")

            _state.value = _state.value.copy(
                coins = _state.value.coins + coins,
                message = "Session bestätigt ✔ +$coins Coins"
            )
        } else {
            repository.addCoins(-5, "Ungültige Session (KI)")
            _state.value = _state.value.copy(
                message = "Session abgelehnt ❌ -5 Coins"
            )
        }
    }
}

suspend fun getProductivityScore(photoPath: String): ProductivityScore {
    val bitmap = BitmapFactory.decodeFile(photoPath)
    val verifier = SessionVerifier()
    return verifier.calculateScore(bitmap)
}

fun finishSession(photoPath: String) {
    viewModelScope.launch {

        val score = getProductivityScore(photoPath)

        if (score.score >= 60) {
            val minutes = _state.value.sessionMinutes
            val coins = calculateCoins(minutes)

            repository.addCoins(coins, "Produktive Session ✔ Score: ${score.score}")

            _state.value = _state.value.copy(
                coins = _state.value.coins + coins,
                message = "Session bestätigt ✔ Score: ${score.score}"
            )
        } else {
            repository.addCoins(-5, "Session abgelehnt ❌ Score: ${score.score}")

            _state.value = _state.value.copy(
                message = "Session abgelehnt ❌ Score: ${score.score} (${score.reason})"
            )
        }
    }
}

fun buySocialMediaTime(minutes: Int) {
    viewModelScope.launch {
        if (_state.value.coins >= minutes) {
            repository.socialMediaMinutes += minutes
            repository.addCoins(-minutes, "Social-Media-Minuten gekauft")
            _state.value = _state.value.copy(
                coins = _state.value.coins - minutes,
                message = "Gekauft: $minutes Minuten Social Media"
            )
        } else {
            _state.value = _state.value.copy(
                message = "Nicht genug Coins ❌"
            )
        }
    }
}

fun startSession(task: String, minutes: Int) {
    repository.sessionRunning = true
    repository.sessionValid = false

    _state.value = _state.value.copy(
        sessionMinutes = minutes,
        message = "Session gestartet: $task ($minutes min)"
    )
}

fun finishSession(photoPath: String) {
    viewModelScope.launch {

        val score = getProductivityScore(photoPath)

        repository.sessionRunning = false

        if (score.score >= 60) {
            repository.sessionValid = true
            val minutes = _state.value.sessionMinutes
            val coins = calculateCoins(minutes)

            repository.addCoins(coins, "Produktive Session ✔ Score: ${score.score}")

            _state.value = _state.value.copy(
                coins = _state.value.coins + coins,
                message = "Session bestätigt ✔ Score: ${score.score}"
            )
        } else {
            repository.sessionValid = false
            repository.addCoins(-5, "Session abgelehnt ❌ Score: ${score.score}")

            _state.value = _state.value.copy(
                message = "Session abgelehnt ❌ Score: ${score.score} (${score.reason})"
            )
        }
    }
}

fun startSession(task: String, minutes: Int) {
    repository.sessionRunning = true
    repository.sessionValid = false

    _state.value = _state.value.copy(
        sessionMinutes = minutes,
        message = "Session gestartet: $task ($minutes min)"
    )
}

fun finishSession(photoPath: String) {
    viewModelScope.launch {
        val score = getProductivityScore(photoPath)

        repository.sessionRunning = false

        if (score.score >= 60) {
            repository.sessionValid = true
            val minutes = _state.value.sessionMinutes
            val coins = calculateCoins(minutes)

            repository.addCoins(coins, "Produktive Session ✔ Score: ${score.score}")

            _state.value = _state.value.copy(
                coins = _state.value.coins + coins,
                message = "Session bestätigt ✔ Score: ${score.score}"
            )
        } else {
            repository.sessionValid = false
            repository.addCoins(-5, "Session abgelehnt ❌ Score: ${score.score}")

            _state.value = _state.value.copy(
                message = "Session abgelehnt ❌ Score: ${score.score} (${score.reason})"
            )
        }
    }
}

private val _score = MutableStateFlow(0)
val score = _score

private val _scoreReason = MutableStateFlow("")
val scoreReason = _scoreReason

_score.value = score.score
_scoreReason.value = score.reason

repository.addSession(_state.value.sessionMinutes, score.score, score.score >= 60)
repository.addSession(_state.value.sessionMinutes, score.score, score.score >= 60)

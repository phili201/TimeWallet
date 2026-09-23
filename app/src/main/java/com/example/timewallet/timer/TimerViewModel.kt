package com.example.timewallet.timer

import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.data.TimeWalletRepository
import com.example.timewallet.data.coins.CoinEntry
import com.example.timewallet.data.session.SessionEntry
import com.example.timewallet.ki.SessionVerifier
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Complete state for the focus session and the user's wallet. */
data class TimerState(
    val coins: Int = 0,
    val socialRemainingMinutes: Int = 0,
    val message: String? = null,
    val isRunning: Boolean = false,
    val remainingMinutes: Int = 0,
    val remainingSeconds: Int = 0,
    val currentTask: String = "",
    val sessionMinutes: Int = 0,
    val elapsedMinutes: Int = 0,
    val elapsedSeconds: Int = 0
)

class TimerViewModel(private val app: TimeWalletApp) : ViewModel() {
    private val repo: TimeWalletRepository = app.repository
    private val prefs = app.getSharedPreferences("active_session", android.content.Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            repo.getCoinBalance().collect { balance ->
                _state.value = _state.value.copy(coins = balance)
            }
        }
        viewModelScope.launch {
            while (true) {
                _state.value = _state.value.copy(socialRemainingMinutes = repo.getSocialRemainingMinutes())
                delay(1_000)
            }
        }
        restoreActiveSession()
    }

    fun startSession(task: String, minutes: Int) {
        val current = _state.value
        if (current.isRunning) {
            _state.value = current.copy(message = "Eine Session läuft bereits.")
            return
        }
        if (current.sessionMinutes > 0) {
            _state.value = current.copy(message = "Bitte zuerst die abgeschlossene Session per Foto bestätigen.")
            return
        }

        val safeMinutes = minutes.coerceIn(1, 180)
        val safeTask = task.trim().ifBlank { "Allgemein" }
        timerJob?.cancel()
        val endAt = System.currentTimeMillis() + safeMinutes * 60_000L
        prefs.edit()
            .putLong(KEY_END_AT, endAt)
            .putLong(KEY_TOTAL_SECONDS, safeMinutes * 60L)
            .putString(KEY_TASK, safeTask)
            .apply()
        repo.startProductivitySession()
        _state.value = current.copy(
            isRunning = true,
            remainingMinutes = safeMinutes,
            remainingSeconds = safeMinutes * 60,
            sessionMinutes = safeMinutes,
            elapsedMinutes = 0,
            elapsedSeconds = 0,
            currentTask = safeTask,
            message = "Session gestartet: $safeTask"
        )
        runTimer(endAt)
    }

    private fun restoreActiveSession() {
        val endAt = prefs.getLong(KEY_END_AT, 0L)
        val totalSeconds = prefs.getLong(KEY_TOTAL_SECONDS, 0L).toInt()
        val task = prefs.getString(KEY_TASK, "Allgemein") ?: "Allgemein"
        if (endAt <= 0L || totalSeconds <= 0) return

        repo.startProductivitySession()
        val remaining = ((endAt - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0)
        if (remaining > 0) {
            val elapsed = (totalSeconds - remaining).coerceAtLeast(0)
            _state.value = _state.value.copy(
                isRunning = true,
                remainingMinutes = remaining / 60,
                remainingSeconds = remaining,
                sessionMinutes = (totalSeconds / 60).coerceAtLeast(1),
                elapsedMinutes = elapsed / 60,
                elapsedSeconds = elapsed,
                currentTask = task,
                message = "Session fortgesetzt: $task"
            )
            runTimer(endAt)
        } else {
            _state.value = _state.value.copy(
                isRunning = false,
                remainingMinutes = 0,
                remainingSeconds = 0,
                sessionMinutes = (totalSeconds / 60).coerceAtLeast(1),
                elapsedMinutes = totalSeconds / 60,
                elapsedSeconds = totalSeconds,
                currentTask = task,
                message = "Session beendet – bitte Foto machen!"
            )
            // Keep the productivity flag until verification succeeds or is rejected.
        }
    }

    private fun runTimer(endAt: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                val remaining = ((endAt - System.currentTimeMillis()) / 1000L).toInt().coerceAtLeast(0)
                val total = prefs.getLong(KEY_TOTAL_SECONDS, 60L).toInt().coerceAtLeast(1)
                val elapsed = (total - remaining).coerceAtLeast(0)
                _state.value = _state.value.copy(
                    remainingMinutes = remaining / 60,
                    remainingSeconds = remaining,
                    elapsedMinutes = elapsed / 60,
                    elapsedSeconds = elapsed
                )
                if (remaining <= 0) {
                    _state.value = _state.value.copy(
                        isRunning = false,
                        remainingMinutes = 0,
                        remainingSeconds = 0,
                        message = "Session beendet – bitte Foto machen!"
                    )
                    break
                }
                delay(1_000)
            }
        }
    }

    fun finishSession(photoPath: String) {
        val current = _state.value
        if (current.sessionMinutes <= 0) {
            _state.value = current.copy(message = "Keine abgeschlossene Session wartet auf Bestätigung.")
            return
        }
        if (current.isRunning) {
            _state.value = current.copy(message = "Bitte die laufende Session vollständig beenden.")
            return
        }
        if (photoPath.isBlank()) {
            _state.value = current.copy(message = "Kein Foto ausgewählt.")
            return
        }

        timerJob?.cancel()
        viewModelScope.launch {
            try {
                val bitmap = decodeSampledBitmap(photoPath)
                    ?: run {
                        _state.value = _state.value.copy(message = "Foto konnte nicht gelesen werden. Bitte erneut aufnehmen.")
                        return@launch
                    }
                if (bitmap == null) {
                    _state.value = _state.value.copy(message = "Foto konnte nicht gelesen werden. Bitte erneut aufnehmen.")
                    return@launch
                }

                val result = SessionVerifier().calculateScore(bitmap, _state.value.currentTask)
                bitmap.recycle()
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

                if (valid) {
                    repo.endProductivitySession()
                    val coins = calculateCoins(minutes)
                    repo.insertCoin(
                        CoinEntry(
                            amount = coins,
                            reason = "Produktive Session: $task",
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    clearPersistedSession()
                    _state.value = _state.value.copy(
                        isRunning = false,
                        remainingMinutes = 0,
                        remainingSeconds = 0,
                        sessionMinutes = 0,
                        elapsedMinutes = minutes,
                        elapsedSeconds = minutes * 60,
                        currentTask = "",
                        message = "Session bestätigt ✔ +$coins Coins • Score: ${result.score}"
                    )
                } else {
                    repo.endProductivitySession()
                    val availableCoins = repo.getCoinBalanceSnapshot()
                    val penalty = minOf(5, availableCoins)
                    if (penalty > 0) {
                        repo.insertCoin(
                            CoinEntry(
                                amount = -penalty,
                                reason = "Ungültige Session / Strafe",
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                    clearPersistedSession()
                    _state.value = _state.value.copy(
                        isRunning = false,
                        remainingMinutes = 0,
                        remainingSeconds = 0,
                        sessionMinutes = 0,
                        elapsedMinutes = minutes,
                        elapsedSeconds = minutes * 60,
                        currentTask = "",
                        message = if (penalty > 0) {
                            "Session abgelehnt ❌ Score: ${result.score}. -$penalty Coins"
                        } else {
                            "Session abgelehnt ❌ Score: ${result.score}. Keine Coins vorhanden."
                        }
                    )
                }
            } catch (exception: Exception) {
                // Do not destroy the pending session when verification fails unexpectedly.
                _state.value = _state.value.copy(
                    message = "Verifizierung fehlgeschlagen. Bitte erneut versuchen."
                )
            }
        }
    }

    fun buySocialTime(minutes: Int, coinCost: Int) {
        viewModelScope.launch {
            val ok = repo.purchaseSocialTime(minutes, coinCost)
            _state.value = _state.value.copy(
                message = if (ok) "$minutes Minuten gekauft ✔" else "Kauf nicht möglich – zu wenige Coins oder ungültige Angaben."
            )
        }
    }

    private fun calculateCoins(minutes: Int): Int =
        minutes + if (minutes >= 60) 10 else if (minutes >= 30) 5 else 0

    private fun decodeSampledBitmap(path: String, maxDimension: Int = 1280): android.graphics.Bitmap? {
        val bounds = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(path, bounds)
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null

        var sample = 1
        while (bounds.outWidth / sample > maxDimension || bounds.outHeight / sample > maxDimension) {
            sample *= 2
        }

        return BitmapFactory.decodeFile(path, android.graphics.BitmapFactory.Options().apply {
            inSampleSize = sample
            inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
        })
    }

    private fun clearPersistedSession() {
        prefs.edit().clear().apply()
    }

    override fun onCleared() {
        timerJob?.cancel()
        super.onCleared()
    }

    companion object {
        private const val KEY_END_AT = "end_at"
        private const val KEY_TOTAL_SECONDS = "total_seconds"
        private const val KEY_TASK = "task"
    }
}

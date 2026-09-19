package com.example.timewallet.data

import com.example.timewallet.data.coins.CoinDao
import com.example.timewallet.data.coins.CoinEntry
import com.example.timewallet.data.session.SessionDao
import com.example.timewallet.data.session.SessionEntry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TimeWalletRepository(
    private val coinDao: CoinDao,
    private val sessionDao: SessionDao
) {

    // --- Heartbeat (Notfallmodus) ---
    @Volatile
    private var lastHeartbeat: Long = 0L

    fun updateHeartbeat() {
        lastHeartbeat = System.currentTimeMillis()
    }

    fun isAppAlive(timeoutMs: Long = 5000L): Boolean {
        val now = System.currentTimeMillis()
        return now - lastHeartbeat <= timeoutMs
    }

    // --- Coins ---
    suspend fun addCoins(amount: Int, reason: String) {
        val entry = CoinEntry(
            amount = amount,
            reason = reason,
            timestamp = System.currentTimeMillis()
        )
        coinDao.insert(entry)
    }

    fun getCoinHistory() = coinDao.getHistory()

    // --- Social Media Minuten ---
    @Volatile
    private var socialMediaMinutes: Int = 0

    private val _socialMinutesFlow = MutableStateFlow(socialMediaMinutes)
    val socialMinutesFlow: StateFlow<Int> = _socialMinutesFlow

    fun consumeMinute() {
        if (socialMediaMinutes > 0) {
            socialMediaMinutes--
            _socialMinutesFlow.value = socialMediaMinutes
        }
    }

    fun isSocialAllowed(): Boolean {
        return socialMediaMinutes > 0 && sessionValid && !sessionRunning
    }

    // --- Session Status ---
    @Volatile var sessionRunning: Boolean = false
    @Volatile var sessionValid: Boolean = false

    fun allowSocialMedia(): Boolean {
        return socialMediaMinutes > 0 && sessionValid && !sessionRunning
    }

    // --- Sessions ---
    suspend fun addSession(minutes: Int, score: Int, valid: Boolean) {
        sessionDao.insert(
            SessionEntry(
                minutes = minutes,
                score = score,
                valid = valid,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    fun getSessions() = sessionDao.getSessions()
}

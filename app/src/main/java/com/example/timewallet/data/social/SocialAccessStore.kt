package com.example.timewallet.data.social

import android.content.Context
import kotlin.math.min

/** Persists purchased social time and tracks only foreground use of target apps. */
class SocialAccessStore(context: Context) {
    private val prefs = context.getSharedPreferences("social_access", Context.MODE_PRIVATE)

    private fun remaining(now: Long = System.currentTimeMillis()): Long {
        val stored = prefs.getLong(KEY_REMAINING, 0L)
        val started = prefs.getLong(KEY_STARTED, 0L)
        if (started <= 0L) return stored
        return (stored - (now - started)).coerceAtLeast(0L)
    }

    fun purchasedRemainingMs(): Long = remaining()

    fun isProductivitySessionRunning(): Boolean = prefs.getBoolean(KEY_PRODUCTIVITY, false)
    fun isAntiAddictionMode(): Boolean = prefs.getBoolean(KEY_ANTI_ADDICTION, false)

    @Synchronized
    fun setProductivitySessionRunning(running: Boolean) {
        stopSocialUse()
        prefs.edit().putBoolean(KEY_PRODUCTIVITY, running).apply()
    }

    @Synchronized
    fun purchaseMinutes(minutes: Int): Boolean {
        if (minutes <= 0) return false
        val current = remaining()
        val requested = minutes * 60_000L
        val maxRemaining = if (isAntiAddictionMode()) MAX_ANTI_ADDICTION_MS else Long.MAX_VALUE
        if (current + requested > maxRemaining) return false

        stopSocialUse()
        val total = min(current + requested, maxRemaining)
        prefs.edit().putLong(KEY_REMAINING, total).putLong(KEY_STARTED, 0L).apply()
        return true
    }

    @Synchronized
    fun removePurchasedMinutes(minutes: Int): Boolean {
        if (minutes <= 0) return false
        val left = (remaining() - minutes * 60_000L).coerceAtLeast(0L)
        prefs.edit().putLong(KEY_REMAINING, left).putLong(KEY_STARTED, 0L).apply()
        return true
    }

    fun startSocialUse() {
        if (isProductivitySessionRunning() || remaining() <= 0L) return
        if (prefs.getLong(KEY_STARTED, 0L) == 0L) {
            prefs.edit().putLong(KEY_STARTED, System.currentTimeMillis()).apply()
        }
    }

    @Synchronized
    fun stopSocialUse() {
        val started = prefs.getLong(KEY_STARTED, 0L)
        if (started <= 0L) return
        val left = (prefs.getLong(KEY_REMAINING, 0L) -
            (System.currentTimeMillis() - started)).coerceAtLeast(0L)
        prefs.edit().putLong(KEY_REMAINING, left).putLong(KEY_STARTED, 0L).apply()
    }

    fun setAntiAddictionMode(enabled: Boolean) {
        stopSocialUse()
        prefs.edit().putBoolean(KEY_ANTI_ADDICTION, enabled).apply()
    }

    fun remainingMinutes(): Int = (remaining() / 60_000L).toInt()

    companion object {
        private const val KEY_REMAINING = "remaining_ms"
        private const val KEY_STARTED = "social_use_started"
        private const val KEY_PRODUCTIVITY = "productivity_session"
        private const val KEY_ANTI_ADDICTION = "anti_addiction"
        private const val MAX_ANTI_ADDICTION_MS = 30 * 60_000L
    }
}
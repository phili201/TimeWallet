package com.example.timewallet.data.social

import android.content.Context

/** Persists purchased social minutes and tracks only foreground use of target apps. */
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

    fun setProductivitySessionRunning(running: Boolean) {
        stopSocialUse()
        prefs.edit().putBoolean(KEY_PRODUCTIVITY, running).apply()
    }

    fun purchaseMinutes(minutes: Int): Boolean {
        if (minutes <= 0) return false
        stopSocialUse()
        val total = remaining() + minutes * 60_000L
        prefs.edit().putLong(KEY_REMAINING, total).apply()
        return true
    }

    fun startSocialUse() {
        if (isProductivitySessionRunning() || remaining() <= 0L) return
        if (prefs.getLong(KEY_STARTED, 0L) == 0L) {
            prefs.edit().putLong(KEY_STARTED, System.currentTimeMillis()).apply()
        }
    }

    fun stopSocialUse() {
        val started = prefs.getLong(KEY_STARTED, 0L)
        if (started <= 0L) return
        val left = (prefs.getLong(KEY_REMAINING, 0L) - (System.currentTimeMillis() - started)).coerceAtLeast(0L)
        prefs.edit().putLong(KEY_REMAINING, left).putLong(KEY_STARTED, 0L).apply()
    }

    fun setAntiAddictionMode(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ANTI_ADDICTION, enabled).apply()
    }

    fun remainingMinutes(): Int = (remaining() / 60_000L).toInt()

    companion object {
        private const val KEY_REMAINING = "remaining_ms"
        private const val KEY_STARTED = "social_use_started"
        private const val KEY_PRODUCTIVITY = "productivity_session"
        private const val KEY_ANTI_ADDICTION = "anti_addiction"
    }
}

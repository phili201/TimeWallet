package com.example.timewallet.data.social

import android.content.Context

class SocialAccessStore(context: Context) {
    private val prefs = context.getSharedPreferences("social_access", Context.MODE_PRIVATE)

    fun purchasedUntil(): Long = prefs.getLong(KEY_UNTIL, 0L)
    fun isProductivitySessionRunning(): Boolean = prefs.getBoolean(KEY_PRODUCTIVITY, false)
    fun isAntiAddictionMode(): Boolean = prefs.getBoolean(KEY_ANTI_ADDICTION, false)
    fun isEmergencyEnabled(): Boolean = prefs.getBoolean(KEY_EMERGENCY, false)

    fun setProductivitySessionRunning(running: Boolean) =
        prefs.edit().putBoolean(KEY_PRODUCTIVITY, running).apply()

    fun purchaseMinutes(minutes: Int, now: Long = System.currentTimeMillis()): Boolean {
        if (minutes <= 0) return false
        val start = maxOf(now, purchasedUntil())
        prefs.edit().putLong(KEY_UNTIL, start + minutes * 60_000L).apply()
        return true
    }

    fun consumeExpired(now: Long = System.currentTimeMillis()) {
        if (purchasedUntil() <= now) prefs.edit().putLong(KEY_UNTIL, 0L).apply()
    }

    fun setAntiAddictionMode(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_ANTI_ADDICTION, enabled).apply()

    fun setEmergencyEnabled(enabled: Boolean) =
        prefs.edit().putBoolean(KEY_EMERGENCY, enabled).apply()

    fun remainingMinutes(now: Long = System.currentTimeMillis()): Int =
        ((purchasedUntil() - now).coerceAtLeast(0L) / 60_000L).toInt()

    companion object {
        private const val KEY_UNTIL = "purchased_until"
        private const val KEY_PRODUCTIVITY = "productivity_session"
        private const val KEY_ANTI_ADDICTION = "anti_addiction"
        private const val KEY_EMERGENCY = "emergency_enabled"
    }
}

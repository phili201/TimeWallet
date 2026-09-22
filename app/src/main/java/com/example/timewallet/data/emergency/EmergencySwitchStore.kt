package com.example.timewallet.data.emergency

import android.content.Context

/** Local recovery switch. When enabled, the Accessibility blocker is disabled. */
class EmergencySwitchStore(context: Context) {
    private val prefs = context.getSharedPreferences("emergency", Context.MODE_PRIVATE)

    fun isEnabled(): Boolean = prefs.getBoolean(KEY_ENABLED, false)

    fun setEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    companion object { private const val KEY_ENABLED = "enabled" }
}

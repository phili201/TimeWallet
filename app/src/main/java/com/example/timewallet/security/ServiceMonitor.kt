package com.example.timewallet.security

import android.content.Context
import android.provider.Settings

class ServiceMonitor {

    fun isBlockerEnabled(context: Context): Boolean {
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )
        return enabled?.contains("com.example.timewallet/.blocker.SocialBlockerService") == true
    }
}

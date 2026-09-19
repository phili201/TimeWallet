package com.example.timewallet.blocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        // Nach Neustart prüfen, ob Accessibility aktiv ist
        val enabled = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        )?.contains(context.packageName) == true

        if (!enabled) {
            // Optional: Hinweis anzeigen oder App öffnen
            val i = context.packageManager.getLaunchIntentForPackage(context.packageName)
            i?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(i)
        }
    }
}

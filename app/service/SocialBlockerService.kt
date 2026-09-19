package com.example.timewallet.service

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.LayoutInflater
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SocialBlockerService : AccessibilityService() {

    private val blockedPackages = listOf(
        "com.instagram.android",
        "com.google.android.youtube"
    )

    private var overlayShown = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val pkg = event.packageName?.toString() ?: return

            if (blockedPackages.contains(pkg)) {
                val app = applicationContext as TimeWalletApp

                CoroutineScope(Dispatchers.Main).launch {
                    val active = app.repository.isSocialUnlockActive()

                    if (!active) {
                        // 1. Toast anzeigen
                        Toast.makeText(
                            applicationContext,
                            "Keine Social-Media-Zeit gekauft!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // 2. Overlay anzeigen
                        showOverlay()

                        // 3. Zurück zum Homescreen
                        performGlobalAction(GLOBAL_ACTION_HOME)
                    }
                }
            }
        }
    }

    override fun onInterrupt() {}

    private fun showOverlay() {
        if (overlayShown) return
        overlayShown = true

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        val view = LayoutInflater.from(this).inflate(R.layout.blocker_overlay, null)

        wm.addView(view, params)

        // Overlay entfernen, wenn man drauf tippt
        view.setOnClickListener {
            wm.removeView(view)
            overlayShown = false
        }
    }
}


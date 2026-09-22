package com.example.timewallet.service

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.example.timewallet.R
import com.example.timewallet.TimeWalletApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SocialBlockerService : AccessibilityService() {

    private val blockedPackages = setOf(
        "com.instagram.android",
        "com.google.android.youtube"
    )

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var overlayView: View? = null
    private var currentBlockedPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        if (packageName in blockedPackages) {
            currentBlockedPackage = packageName
            checkAndBlock(packageName)
        } else {
            removeOverlay()
            currentBlockedPackage = null
        }
    }

    private fun checkAndBlock(packageName: String) {
        val app = applicationContext as? TimeWalletApp ?: return

        serviceScope.launch {
            val active = runCatching { app.repository.isSocialUnlockActive() }
                .getOrDefault(false)

            if (active) {
                removeOverlay()
                return@launch
            }

            if (overlayView == null) {
                Toast.makeText(
                    applicationContext,
                    "Keine Social-Media-Zeit gekauft!",
                    Toast.LENGTH_SHORT
                ).show()
                showOverlay(packageName)
            }
        }
    }

    private fun showOverlay(packageName: String) {
        if (overlayView != null) return

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = LayoutInflater.from(this).inflate(R.layout.blocker_overlay, null)

        // Keep the overlay active. Removing it on a tap would make the blocker
        // immediately bypassable.
        view.contentDescription = when (packageName) {
            "com.instagram.android" -> "Instagram ist blockiert"
            "com.google.android.youtube" -> "YouTube ist blockiert"
            else -> "App ist blockiert"
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        try {
            windowManager.addView(view, params)
            overlayView = view
        } catch (_: SecurityException) {
            overlayView = null
        } catch (_: WindowManager.BadTokenException) {
            overlayView = null
        }
    }

    private fun removeOverlay() {
        val view = overlayView ?: return
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        runCatching { windowManager.removeView(view) }
        overlayView = null
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        removeOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }
}

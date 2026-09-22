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

        // We only need window changes to determine which app is currently visible.
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOWS_CHANGED) {
            return
        }

        val packageName = event.packageName?.toString() ?: return

        if (packageName in blockedPackages) {
            currentBlockedPackage = packageName
            checkAndBlock(packageName)
        } else {
            // Leaving Instagram/YouTube removes the overlay. When the user opens
            // a blocked app again, a fresh window event will trigger the block.
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

        // The overlay deliberately remains in place while a blocked app is open.
        // It must NOT remove itself when tapped; otherwise the user could simply
        // tap once and bypass the blocker.
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
            // Overlay permission is optional at the OS level. The accessibility
            // service still stays alive instead of crashing the whole service.
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

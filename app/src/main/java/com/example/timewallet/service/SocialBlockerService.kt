package com.example.timewallet.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.graphics.PixelFormat
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
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

    private val repository by lazy { (application as TimeWalletApp).repository }

    override fun onServiceConnected() {
        super.onServiceConnected()

        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 50
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val packageName = event.packageName?.toString() ?: return

        if (packageName in blockedPackages) {
            currentBlockedPackage = packageName
            enforceBlock(packageName)
        } else if (packageName != this.packageName) {
            currentBlockedPackage = null
            removeOverlay()
        }
    }

    private fun enforceBlock(packageName: String) {
        serviceScope.launch {
            val shouldBlock = runCatching {
                !repository.isEmergencySwitchEnabled() &&
                    (repository.isProductivitySessionRunning() || !repository.isSocialAllowed())
            }.getOrDefault(false)

            if (shouldBlock) {
                showOverlay(packageName)
            } else {
                removeOverlay()
            }
        }
    }

    private fun showOverlay(packageName: String) {
        if (overlayView != null) return

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = LayoutInflater.from(this).inflate(R.layout.blocker_overlay, null)

        view.contentDescription = when (packageName) {
            "com.instagram.android" -> "Instagram ist blockiert"
            "com.google.android.youtube" -> "YouTube ist blockiert"
            else -> "App ist blockiert"
        }

        // Accessibility overlays are owned by the accessibility service and do not
        // require the separate SYSTEM_ALERT_WINDOW permission.
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        view.isClickable = true
        view.isFocusable = true
        view.setOnTouchListener { _, _ -> true }

        runCatching {
            windowManager.addView(view, params)
            overlayView = view
        }
    }

    private fun removeOverlay() {
        val view = overlayView ?: return
        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        runCatching { windowManager.removeViewImmediate(view) }
        overlayView = null
    }

    override fun onInterrupt() {
        removeOverlay()
        currentBlockedPackage = null
    }

    override fun onDestroy() {
        removeOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }
}

package com.example.timewallet.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.graphics.PixelFormat
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import com.example.timewallet.R
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.ui.block.BlockScreenActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * System-level blocker based on Android AccessibilityService.
 *
 * The service does not need a normal runtime permission. The user must enable
 * it once in Android Accessibility settings.
 */
class SocialBlockerService : AccessibilityService() {
    private val blockedPackages = setOf(
        "com.instagram.android",
        "com.google.android.youtube"
    )

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var monitorJob: Job? = null
    private var overlayView: View? = null
    private var currentBlockedPackage: String? = null
    private var lastBlockScreenLaunchAt = 0L

    private val repository by lazy { (application as TimeWalletApp).repository }

    override fun onServiceConnected() {
        super.onServiceConnected()

        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes =
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED or
                    AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags =
                AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 50
        }

        startMonitor()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val eventPackage = event?.packageName?.toString()

        if (eventPackage in blockedPackages) {
            currentBlockedPackage = eventPackage
            enforceForeground(eventPackage)
            return
        }

        if (eventPackage == packageName) {
            removeOverlay()
            return
        }

        if (eventPackage != null && eventPackage !in blockedPackages) {
            currentBlockedPackage = null
            stopSocialUse()
            removeOverlay()
        }
    }

    private fun startMonitor() {
        if (monitorJob?.isActive == true) return

        monitorJob = serviceScope.launch {
            while (true) {
                enforceCurrentForegroundApp()
                delay(500)
            }
        }
    }

    private fun enforceCurrentForegroundApp() {
        val rootPackage = rootInActiveWindow?.packageName?.toString()
        val activePackage = rootPackage ?: currentBlockedPackage ?: return
        enforceForeground(activePackage)
    }

    private fun enforceForeground(activePackage: String) {
        if (activePackage == packageName) {
            currentBlockedPackage = null
            stopSocialUse()
            removeOverlay()
            return
        }

        if (activePackage !in blockedPackages) {
            currentBlockedPackage = null
            stopSocialUse()
            removeOverlay()
            return
        }

        currentBlockedPackage = activePackage

        val shouldBlock = runCatching {
            !repository.isEmergencySwitchEnabled() &&
                (repository.isProductivitySessionRunning() || !repository.isSocialAllowed())
        }.getOrDefault(true)

        if (shouldBlock) {
            stopSocialUse()
            launchBlockScreen(activePackage)
            showFallbackOverlay(activePackage)
        } else {
            removeOverlay()
            repository.startSocialUse()
        }
    }

    private fun launchBlockScreen(packageName: String) {
        val now = System.currentTimeMillis()
        if (now - lastBlockScreenLaunchAt < 1_000L) return

        lastBlockScreenLaunchAt = now
        runCatching {
            startActivity(
                Intent(this, BlockScreenActivity::class.java)
                    .addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                    )
                    .putExtra(BlockScreenActivity.EXTRA_BLOCKED_PACKAGE, packageName)
                    .putExtra(
                        BlockScreenActivity.EXTRA_REASON,
                        if (repository.isProductivitySessionRunning()) {
                            "Deine Fokus-Session läuft. Social Apps bleiben bis zum Ende gesperrt."
                        } else {
                            "Du hast gerade keine Social-Zeit verfügbar."
                        }
                    )
            )
        }
    }

    /**
     * Fallback in case Android refuses to start the activity from the service.
     * Accessibility overlays do not require the normal SYSTEM_ALERT_WINDOW permission.
     */
    private fun showFallbackOverlay(packageName: String) {
        if (overlayView != null) return

        val windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = LayoutInflater.from(this).inflate(R.layout.blocker_overlay, null)

        view.contentDescription = when (packageName) {
            "com.instagram.android" -> "Instagram ist blockiert"
            "com.google.android.youtube" -> "YouTube ist blockiert"
            else -> "App ist blockiert"
        }

        view.findViewById<View>(R.id.openWalletButton)?.setOnClickListener {
            startActivity(
                Intent(this, com.example.timewallet.ui.MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            )
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        view.isClickable = true
        view.isFocusable = true

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

    private fun stopSocialUse() {
        runCatching { repository.stopSocialUse() }
    }

    override fun onInterrupt() {
        monitorJob?.cancel()
        monitorJob = null
        stopSocialUse()
        removeOverlay()
        currentBlockedPackage = null
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        monitorJob = null
        stopSocialUse()
        removeOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }
}

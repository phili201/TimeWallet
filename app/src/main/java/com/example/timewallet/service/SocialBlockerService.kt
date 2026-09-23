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
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SocialBlockerService : AccessibilityService() {
    private val blockedPackages = setOf("com.instagram.android", "com.google.android.youtube")
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var overlayView: View? = null
    private var currentBlockedPackage: String? = null
    private var monitorJob: Job? = null
    private val repository by lazy { (application as TimeWalletApp).repository }

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 50
        }
        startMonitor()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val packageName = event?.packageName?.toString() ?: return
        if (packageName in blockedPackages) {
            currentBlockedPackage = packageName
            startMonitor()
        } else if (packageName != this.packageName) {
            stopSocialUse()
            currentBlockedPackage = null
            removeOverlay()
        }
    }

    private fun startMonitor() {
        if (monitorJob?.isActive == true) return
        monitorJob = serviceScope.launch {
            while (true) {
                enforceCurrentForegroundApp()
                delay(750)
            }
        }
    }

    private fun enforceCurrentForegroundApp() {
        val activePackage = rootInActiveWindow?.packageName?.toString() ?: currentBlockedPackage
        if (activePackage !in blockedPackages) {
            stopSocialUse()
            currentBlockedPackage = null
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
            showOverlay(activePackage)
        } else {
            removeOverlay()
            repository.startSocialUse()
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
        view.findViewById<View>(R.id.openWalletButton)?.setOnClickListener {
            startActivity(
                android.content.Intent(this, com.example.timewallet.ui.MainActivity::class.java)
                    .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_STABLE,
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
        stopSocialUse()
        removeOverlay()
        currentBlockedPackage = null
    }

    override fun onDestroy() {
        monitorJob?.cancel()
        stopSocialUse()
        removeOverlay()
        serviceScope.cancel()
        super.onDestroy()
    }
}
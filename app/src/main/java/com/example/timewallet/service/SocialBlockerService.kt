package com.example.timewallet.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.ui.block.BlockScreenActivity

class SocialBlockerService : AccessibilityService() {
    private val blockedApps = setOf("com.instagram.android", "com.google.android.youtube")
    private val repo by lazy { (application as TimeWalletApp).repository }
    private val handler = Handler(Looper.getMainLooper())
    private var currentSocialPackage: String? = null

    private val accessMonitor = object : Runnable {
        override fun run() {
            val pkg = currentSocialPackage
            if (pkg != null) {
                if (repo.isEmergencySwitchEnabled()) {
                    repo.stopSocialUse()
                    currentSocialPackage = null
                } else if (repo.isProductivitySessionRunning()) {
                    repo.stopSocialUse()
                    showBlock(pkg, "Produktivsession aktiv")
                    currentSocialPackage = null
                } else if (!repo.isSocialAllowed()) {
                    repo.stopSocialUse()
                    showBlock(pkg, "Deine gekaufte Social-Zeit ist aufgebraucht")
                    currentSocialPackage = null
                }
            }
            handler.postDelayed(this, POLL_INTERVAL_MS)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return

        if (currentSocialPackage != null && pkg != currentSocialPackage) {
            repo.stopSocialUse()
            currentSocialPackage = null
        }
        if (pkg !in blockedApps) return
        if (!repo.isAppAlive() || repo.isEmergencySwitchEnabled()) return

        if (repo.isProductivitySessionRunning()) {
            showBlock(pkg, "Produktivsession aktiv")
            return
        }
        if (!repo.isSocialAllowed()) {
            showBlock(pkg, "Deine gekaufte Social-Zeit ist aufgebraucht")
            return
        }

        currentSocialPackage = pkg
        repo.startSocialUse()
    }

    private fun showBlock(pkg: String, reason: String) {
        startActivity(Intent(this, BlockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(BlockScreenActivity.EXTRA_BLOCKED_PACKAGE, pkg)
            putExtra(BlockScreenActivity.EXTRA_REASON, reason)
        })
    }

    override fun onInterrupt() {
        repo.stopSocialUse()
        currentSocialPackage = null
    }

    override fun onDestroy() {
        handler.removeCallbacks(accessMonitor)
        repo.stopSocialUse()
        currentSocialPackage = null
        super.onDestroy()
    }

    override fun onServiceConnected() {
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
        handler.removeCallbacks(accessMonitor)
        handler.post(accessMonitor)
    }

    companion object {
        private const val POLL_INTERVAL_MS = 1_000L
    }
}

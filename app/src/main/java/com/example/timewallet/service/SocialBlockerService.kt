package com.example.timewallet.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.ui.block.BlockScreenActivity

class SocialBlockerService : AccessibilityService() {
    // Keep the initial block list focused on the apps requested for TimeWallet.
    private val blockedApps = setOf(
        "com.instagram.android",
        "com.google.android.youtube"
    )

    private val repo by lazy { (application as TimeWalletApp).repository }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg !in blockedApps) return
        if (!repo.isAppAlive()) return
        if (repo.isSocialAllowed()) return

        startActivity(Intent(this, BlockScreenActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra(BlockScreenActivity.EXTRA_BLOCKED_PACKAGE, pkg)
        })
    }

    override fun onInterrupt() = Unit

    override fun onServiceConnected() {
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
    }
}

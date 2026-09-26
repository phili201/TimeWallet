package com.example.timewallet.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.ui.MainActivity

/** Android Accessibility layer. The user explicitly enables it in system settings. */
class SocialBlockerService : AccessibilityService() {
    private val repo get() = (application as TimeWalletApp).repository
    private val blocked = setOf("com.instagram.android", "com.google.android.youtube", "com.zhiliaoapp.musically")

    override fun onServiceConnected() {
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            notificationTimeout = 100
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg !in blocked || pkg == packageName) return
        val shouldBlock = !repo.isEmergencySwitchEnabled() && (repo.isProductivitySessionRunning() || !repo.isSocialAllowed())
        if (shouldBlock) {
            performGlobalAction(GLOBAL_ACTION_HOME)
            startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        } else {
            repo.startSocialUse()
        }
    }

    override fun onInterrupt() { repo.stopSocialUse() }
}

package com.example.timewallet.service

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.ui.MainActivity

class SocialBlockerService : AccessibilityService() {
    private val repo get() = (application as TimeWalletApp).repository
    private val exceptions = setOf("com.whatsapp", "com.whatsapp.w4b", "com.snapchat.android")
    override fun onServiceConnected() { serviceInfo = AccessibilityServiceInfo().apply { eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_WINDOWS_CHANGED; feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC; notificationTimeout = 100 } }
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        val pkg = event?.packageName?.toString() ?: return
        if (pkg == packageName || pkg in exceptions) { if (pkg != packageName) repo.stopSocialUse(); return }
        val selected = repo.blockedPackages().contains(pkg)
        if (!selected) { repo.stopSocialUse(); return }
        val allowed = repo.isSocialAllowed() && !repo.isProductivitySessionRunning() && !repo.isEmergencySwitchEnabled()
        if (allowed) repo.startSocialUse() else {
            repo.stopSocialUse()
            performGlobalAction(GLOBAL_ACTION_HOME)
            startActivity(Intent(this, MainActivity::class.java).putExtra("blocked", true).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP))
        }
    }
    override fun onInterrupt() { repo.stopSocialUse() }
}

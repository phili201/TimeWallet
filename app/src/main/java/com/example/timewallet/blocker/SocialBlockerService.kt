package com.example.timewallet.blocker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class SocialBlockerService : AccessibilityService() {

    private val blockedApps = listOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically",   // TikTok
        "com.google.android.youtube",
        
        "com.facebook.katana",
        "com.android.chrome"
    )

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return

        val packageName = event.packageName?.toString() ?: return

        if (blockedApps.contains(packageName)) {
            val intent = Intent(this, BlockScreenActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

    override fun onInterrupt() {}

    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
        }
        serviceInfo = info
    }
}

// --- Heartbeat-Check: Wenn App hängt → Blocker aus ---
val repository = (application as TimeWalletApp).repository
if (!repository.isAppAlive()) {
    // App hängt → Blocker deaktiviert (Notfall)
    return
}

private fun shouldBlock(packageName: String): Boolean {
    val repo = (application as TimeWalletApp).repository

    val socialApps = listOf(
        "com.instagram.android",
        "com.google.android.youtube",
        "com.zhiliaoapp.musically",
        "com.snapchat.android",
        "com.facebook.katana"
    )

    if (packageName !in socialApps) return false

    // App hängt → Blocker aus (Notfall)
    if (!repo.isAppAlive()) return false

    // Social Media erlaubt?
    val allowed = repo.allowSocialMedia()

    return !allowed
}

override fun onAccessibilityEvent(event: AccessibilityEvent?) {
    val pkg = event?.packageName?.toString() ?: return

    if (shouldBlock(pkg)) {
        val intent = Intent(this, BlockScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}

private fun isAccessibilityEnabled(): Boolean {
    val enabled = android.provider.Settings.Secure.getString(
        contentResolver,
        android.provider.Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    ) ?: return false

    return enabled.contains(packageName)
}

override fun onServiceConnected() {
    super.onServiceConnected()
    // Wenn jemand dich deaktiviert → App kann reagieren
    if (!isAccessibilityEnabled()) {
        // Hier könntest du z.B. eine Warnung im UI anzeigen
    }
}

private fun shouldBlock(packageName: String): Boolean {
    val repo = (application as TimeWalletApp).repository

    val socialApps = listOf(
        "com.instagram.android",
        "com.google.android.youtube"
    )

    if (packageName !in socialApps) return false

    // App hängt → Notfall, Blocker aus
    if (!repo.isAppAlive()) return false

    // Social Media nur erlaubt, wenn isSocialAllowed() true
    return !repo.isSocialAllowed()
}

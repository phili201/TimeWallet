package com.example.timewallet.security

import android.app.ActivityManager
import android.content.Context

class TaskWatcher {

    private val blockedApps = listOf(
        "com.instagram.android",
        "com.zhiliaoapp.musically",
        "com.google.android.youtube",
        "com.snapchat.android",
        "com.facebook.katana",
        "com.android.chrome"
    )

    fun isUserTryingToEscape(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val task = am.getRunningTasks(1).firstOrNull()
        val top = task?.topActivity?.packageName ?: return false
        return blockedApps.contains(top)
    }
}

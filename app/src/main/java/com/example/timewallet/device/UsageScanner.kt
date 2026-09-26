package com.example.timewallet.device

import android.app.usage.UsageStatsManager
import android.content.Context
import android.provider.Settings

class UsageScanner(private val context: Context) {
    fun hasUsageAccess(): Boolean = try {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as android.app.AppOpsManager
        appOps.checkOpNoThrow("android:get_usage_stats", android.os.Process.myUid(), context.packageName) == android.app.AppOpsManager.MODE_ALLOWED
    } catch (_: Exception) { false }

    fun foregroundPackage(): String? = if (!hasUsageAccess()) null else runCatching {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 10_000, now).maxByOrNull { it.lastTimeUsed }?.packageName
    }.getOrNull()

    fun openSettings() { context.startActivity(android.content.Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }
}

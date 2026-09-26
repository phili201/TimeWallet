package com.example.timewallet.device

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.concurrent.TimeUnit

class UsageScanner(private val context: Context) {
    fun minutesLast24Hours(): Long {
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        return manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - TimeUnit.DAYS.toMillis(1), now)
            .sumOf { it.totalTimeInForeground } / 60000L
    }
}

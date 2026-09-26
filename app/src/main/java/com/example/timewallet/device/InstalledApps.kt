package com.example.timewallet.device

import android.content.Context
import android.content.Intent

data class AppEntry(val label: String, val packageName: String)

fun installedLaunchableApps(context: Context): List<AppEntry> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return context.packageManager.queryIntentActivities(intent, 0).mapNotNull { info ->
        val app = info.activityInfo?.applicationInfo ?: return@mapNotNull null
        if (app.packageName == context.packageName) null else AppEntry(app.loadLabel(context.packageManager).toString(), app.packageName)
    }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
}

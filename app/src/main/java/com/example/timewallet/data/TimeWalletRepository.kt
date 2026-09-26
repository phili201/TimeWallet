package com.example.timewallet.data

import android.content.Context
import android.os.SystemClock
import org.json.JSONArray
import org.json.JSONObject

class TimeWalletRepository(context: Context) {
    private val prefs = context.getSharedPreferences("timewallet_state", Context.MODE_PRIVATE)
    @Synchronized fun coins() = prefs.getInt("coins", 0)
    @Synchronized fun socialSeconds() = prefs.getLong("social_seconds", 0L)
    @Synchronized fun isProductivitySessionRunning() = prefs.getLong("session_end", 0L) > System.currentTimeMillis()
    @Synchronized fun isSocialAllowed() = !isProductivitySessionRunning() && socialSeconds() > 0
    @Synchronized fun isEmergencySwitchEnabled() = prefs.getBoolean("emergency", false)
    @Synchronized fun setEmergencySwitchEnabled(v: Boolean) { prefs.edit().putBoolean("emergency", v).apply() }
    @Synchronized fun startSession(task: String, minutes: Int) { val m = minutes.coerceIn(1, 180); prefs.edit().putString("session_task", task).putLong("session_start", System.currentTimeMillis()).putLong("session_end", System.currentTimeMillis() + m * 60_000L).putInt("session_minutes", m).putBoolean("session_verified", false).apply() }
    @Synchronized fun endSessionIfAllowed(): Boolean { if (isProductivitySessionRunning() && !currentSessionVerified()) return false; prefs.edit().remove("session_task").remove("session_start").remove("session_end").remove("session_minutes").remove("session_verified").apply(); return true }
    @Synchronized fun remainingSessionSeconds() = ((prefs.getLong("session_end", 0L) - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L)
    @Synchronized fun currentTask() = prefs.getString("session_task", "") ?: ""
    @Synchronized fun verifyCurrentSession() { prefs.edit().putBoolean("session_verified", true).apply() }
    @Synchronized fun currentSessionVerified() = prefs.getBoolean("session_verified", false)
    @Synchronized fun awardCoins(amount: Int, reason: String) { if (amount <= 0) return; prefs.edit().putInt("coins", coins() + amount).apply(); addTransaction(amount, reason) }
    @Synchronized fun buySocialTime(minutes: Int, cost: Int): Boolean { if (cost <= 0 || minutes <= 0 || coins() < cost || isProductivitySessionRunning()) return false; prefs.edit().putInt("coins", coins() - cost).putLong("social_seconds", socialSeconds() + minutes * 60L).apply(); addTransaction(-cost, "$minutes Minuten Social-Zeit gekauft"); return true }
    @Synchronized fun startSocialUse() { prefs.edit().putBoolean("social_running", true).putLong("last_tick", SystemClock.elapsedRealtime()).apply() }
    @Synchronized fun stopSocialUse() { tickSocialUse(); prefs.edit().putBoolean("social_running", false).apply() }
    @Synchronized fun tickSocialUse(): Long { if (!prefs.getBoolean("social_running", false)) return socialSeconds(); val now = SystemClock.elapsedRealtime(); val last = prefs.getLong("last_tick", now); val elapsed = ((now - last) / 1000L).coerceAtLeast(0L); if (elapsed > 0) prefs.edit().putLong("social_seconds", (socialSeconds() - elapsed).coerceAtLeast(0L)).putLong("last_tick", now).apply(); return socialSeconds() }
    @Synchronized fun transactions() = runCatching { JSONArray(prefs.getString("transactions", "[]")) }.getOrElse { JSONArray() }
    private fun addTransaction(amount: Int, reason: String) { val old = transactions(); val out = JSONArray().put(JSONObject().put("amount", amount).put("reason", reason).put("time", System.currentTimeMillis())); for (i in 0 until minOf(old.length(), 49)) out.put(old.optJSONObject(i) ?: JSONObject()); prefs.edit().putString("transactions", out.toString()).apply() }
    fun saveProfile(name: String, email: String) { prefs.edit().putString("name", name).putString("email", email).apply() }
    fun profileName() = prefs.getString("name", "") ?: ""
    fun profileEmail() = prefs.getString("email", "") ?: ""
    fun setBlockedPackages(packages: Set<String>) { prefs.edit().putStringSet("blocked", packages).apply() }
    fun blockedPackages(): Set<String> = prefs.getStringSet("blocked", emptySet()) ?: emptySet()
    fun exportState() = JSONObject().put("coins", coins()).put("social_seconds", socialSeconds()).put("name", profileName()).put("email", profileEmail()).put("blocked_packages", JSONArray(blockedPackages().toList())).put("transactions", transactions()).put("emergency", isEmergencySwitchEnabled()).put("session_running", isProductivitySessionRunning()).put("session_task", currentTask())
    fun importState(s: JSONObject) { prefs.edit().putInt("coins", s.optInt("coins", 0)).putLong("social_seconds", s.optLong("social_seconds", 0L)).putString("name", s.optString("name", "")).putString("email", s.optString("email", "")).putBoolean("emergency", s.optBoolean("emergency", false)).apply(); val arr = s.optJSONArray("blocked_packages"); if (arr != null) { val set = mutableSetOf<String>(); for (i in 0 until arr.length()) set.add(arr.optString(i)); setBlockedPackages(set) } }
}

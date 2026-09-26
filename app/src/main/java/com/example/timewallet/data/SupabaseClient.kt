package com.example.timewallet.data

import android.content.Context
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class SupabaseClient(private val context: Context) {
    companion object {
        const val URL = "https://lnfsytqjjtgunwezzfsv.supabase.co"
        const val PUBLISHABLE_KEY = "sb_publishable_jZuw3EeuzxoFO7yTELD_1Q_ZQI3ej62"
    }
    private val prefs = context.getSharedPreferences("supabase", Context.MODE_PRIVATE)
    private val http = OkHttpClient()
    private val jsonType = "application/json".toMediaType()

    fun accessToken(): String? = prefs.getString("access_token", null)
    fun userId(): String? = prefs.getString("user_id", null)
    fun email(): String? = prefs.getString("email", null)

    fun signUp(email: String, password: String, callback: (Boolean, String) -> Unit) = requestAuth("signup", email, password, callback)
    fun signIn(email: String, password: String, callback: (Boolean, String) -> Unit) = requestAuth("token?grant_type=password", email, password, callback)

    private fun requestAuth(path: String, email: String, password: String, callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = JSONObject().put("email", email).put("password", password).toString().toRequestBody(jsonType)
                val req = Request.Builder().url("$URL/auth/v1/$path").header("apikey", PUBLISHABLE_KEY).header("Authorization", "Bearer $PUBLISHABLE_KEY").post(body).build()
                http.newCall(req).execute().use { r ->
                    val text = r.body?.string().orEmpty(); val obj = runCatching { JSONObject(text) }.getOrNull()
                    if (!r.isSuccessful) { callback(false, obj?.optString("msg") ?: obj?.optString("message") ?: "Anmeldung fehlgeschlagen") ; return@use }
                    val token = obj?.optString("access_token").orEmpty(); val uid = obj?.optString("user").takeIf { !it.isNullOrBlank() } ?: obj?.optJSONObject("user")?.optString("id")
                    if (token.isNotBlank()) prefs.edit().putString("access_token", token).putString("refresh_token", obj?.optString("refresh_token")).putString("user_id", uid).putString("email", email).apply()
                    callback(true, if (path.startsWith("signup")) "Registrierung erfolgreich. Bitte E-Mail bestätigen." else "Login erfolgreich")
                }
            } catch (e: Exception) { callback(false, e.message ?: "Netzwerkfehler") }
        }
    }

    fun resetPassword(email: String, callback: (Boolean, String) -> Unit) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = JSONObject().put("email", email).put("redirect_to", "timewallet://reset-password").toString().toRequestBody(jsonType)
                val req = Request.Builder().url("$URL/auth/v1/recover").header("apikey", PUBLISHABLE_KEY).post(body).build()
                http.newCall(req).execute().use { r -> callback(r.isSuccessful, if (r.isSuccessful) "E-Mail zum Zurücksetzen wurde gesendet." else "Zurücksetzen fehlgeschlagen") }
            } catch (e: Exception) { callback(false, e.message ?: "Netzwerkfehler") }
        }
    }

    fun pushState(state: JSONObject, callback: (Boolean) -> Unit = {}) {
        val uid = userId() ?: return callback(false); val token = accessToken() ?: return callback(false)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val payload = JSONObject().put("user_id", uid).put("state", state).put("updated_at", java.time.Instant.now().toString()).toString().toRequestBody(jsonType)
                val req = Request.Builder().url("$URL/rest/v1/user_state?on_conflict=user_id").header("apikey", PUBLISHABLE_KEY).header("Authorization", "Bearer $token").header("Prefer", "resolution=merge-duplicates,return=minimal").post(payload).build()
                http.newCall(req).execute().use { callback(it.isSuccessful) }
            } catch (_: Exception) { callback(false) }
        }
    }

    fun logout() { prefs.edit().clear().apply() }
}

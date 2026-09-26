package com.example.timewallet.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    private val scope = CoroutineScope(Dispatchers.IO)

    fun accessToken(): String? = prefs.getString("access_token", null)
    fun refreshToken(): String? = prefs.getString("refresh_token", null)
    fun userId(): String? = prefs.getString("user_id", null)
    fun email(): String? = prefs.getString("email", null)
    fun isLoggedIn() = !accessToken().isNullOrBlank() && !userId().isNullOrBlank()

    fun signUp(email: String, password: String, callback: (Boolean, String) -> Unit) = auth("signup", email, password, callback)
    fun signIn(email: String, password: String, callback: (Boolean, String) -> Unit) = auth("token?grant_type=password", email, password, callback)

    private fun auth(path: String, email: String, password: String, callback: (Boolean, String) -> Unit) {
        scope.launch {
            try {
                val body = JSONObject().put("email", email.trim()).put("password", password).toString().toRequestBody(jsonType)
                val req = Request.Builder().url("$URL/auth/v1/$path").header("apikey", PUBLISHABLE_KEY).header("Authorization", "Bearer $PUBLISHABLE_KEY").post(body).build()
                http.newCall(req).execute().use { r ->
                    val text = r.body?.string().orEmpty(); val obj = runCatching { JSONObject(text) }.getOrNull()
                    if (!r.isSuccessful) { callback(false, obj?.optString("msg") ?: obj?.optString("message") ?: "Authentifizierung fehlgeschlagen"); return@use }
                    val user = obj?.optJSONObject("user")
                    val uid = user?.optString("id")?.takeIf { it.isNotBlank() }
                    val token = obj?.optString("access_token")?.takeIf { it.isNotBlank() }
                    if (uid != null) prefs.edit().putString("user_id", uid).putString("email", email.trim()).apply()
                    if (token != null) prefs.edit().putString("access_token", token).putString("refresh_token", obj.optString("refresh_token")).apply()
                    callback(true, if (path == "signup") "Konto erstellt. Bitte bestätige deine E-Mail." else "Login erfolgreich.")
                }
            } catch (e: Exception) { callback(false, e.message ?: "Netzwerkfehler") }
        }
    }

    fun resetPassword(email: String, callback: (Boolean, String) -> Unit) {
        scope.launch {
            try {
                val body = JSONObject().put("email", email.trim()).put("redirect_to", "timewallet://reset-password").toString().toRequestBody(jsonType)
                val req = Request.Builder().url("$URL/auth/v1/recover").header("apikey", PUBLISHABLE_KEY).post(body).build()
                http.newCall(req).execute().use { r -> callback(r.isSuccessful, if (r.isSuccessful) "E-Mail zum Zurücksetzen wurde gesendet." else "Zurücksetzen fehlgeschlagen.") }
            } catch (e: Exception) { callback(false, e.message ?: "Netzwerkfehler") }
        }
    }

    fun pushState(state: JSONObject, callback: (Boolean) -> Unit = {}) {
        val uid = userId() ?: return callback(false); val token = accessToken() ?: return callback(false)
        scope.launch {
            try {
                val payload = JSONObject().put("user_id", uid).put("state", state).put("updated_at", java.time.Instant.now().toString()).toString().toRequestBody(jsonType)
                val req = Request.Builder().url("$URL/rest/v1/user_state?on_conflict=user_id").header("apikey", PUBLISHABLE_KEY).header("Authorization", "Bearer $token").header("Prefer", "resolution=merge-duplicates,return=minimal").post(payload).build()
                http.newCall(req).execute().use { callback(it.isSuccessful) }
            } catch (_: Exception) { callback(false) }
        }
    }

    fun pullState(callback: (JSONObject?) -> Unit) {
        val uid = userId() ?: return callback(null); val token = accessToken() ?: return callback(null)
        scope.launch {
            try {
                val req = Request.Builder().url("$URL/rest/v1/user_state?select=state,updated_at&user_id=eq.$uid&limit=1").header("apikey", PUBLISHABLE_KEY).header("Authorization", "Bearer $token").get().build()
                http.newCall(req).execute().use { r ->
                    if (!r.isSuccessful) { callback(null); return@use }
                    val arr = org.json.JSONArray(r.body?.string().orEmpty()); callback(if (arr.length() == 0) null else arr.getJSONObject(0).optJSONObject("state"))
                }
            } catch (_: Exception) { callback(null) }
        }
    }

    fun logout() { prefs.edit().clear().apply() }
}

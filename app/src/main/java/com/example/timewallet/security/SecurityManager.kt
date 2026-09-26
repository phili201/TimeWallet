package com.example.timewallet.security

import android.content.Context
import android.util.Base64
import androidx.biometric.BiometricManager
import java.security.MessageDigest
import java.security.SecureRandom

class SecurityManager(context: Context) {
    private val p = context.getSharedPreferences("security", Context.MODE_PRIVATE)
    fun hasPin() = !p.getString("pin_hash", null).isNullOrBlank()
    fun biometricEnabled() = p.getBoolean("biometric", false)
    fun setBiometricEnabled(v: Boolean) { p.edit().putBoolean("biometric", v).apply() }
    fun canUseBiometric(context: Context) = BiometricManager.from(context).canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
    fun setPin(pin: String): Boolean {
        if (pin.length !in 4..8 || !pin.all { it.isDigit() }) return false
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hash(pin, salt)
        p.edit().putString("pin_salt", Base64.encodeToString(salt, Base64.NO_WRAP)).putString("pin_hash", hash).apply()
        return true
    }
    fun verifyPin(pin: String): Boolean {
        val salt = p.getString("pin_salt", null)?.let { Base64.decode(it, Base64.NO_WRAP) } ?: return false
        return MessageDigest.isEqual(hash(pin, salt).toByteArray(), (p.getString("pin_hash", "")).toByteArray())
    }
    private fun hash(value: String, salt: ByteArray): String {
        val d = MessageDigest.getInstance("SHA-256"); d.update(salt); return Base64.encodeToString(d.digest(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
    }
}

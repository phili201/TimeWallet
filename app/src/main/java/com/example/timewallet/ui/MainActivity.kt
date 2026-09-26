package com.example.timewallet.ui

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.device.installedLaunchableApps
import java.util.concurrent.Executor

class MainActivity : FragmentActivity() {
    private val app get() = application as TimeWalletApp
    private val repo get() = app.repository
    private lateinit var wallet: TextView
    private lateinit var social: TextView
    private lateinit var session: TextView
    private lateinit var executor: Executor

    private val bg = Color.rgb(10, 12, 16)
    private val card = Color.rgb(21, 24, 30)
    private val card2 = Color.rgb(28, 32, 40)
    private val paletteText = Color.WHITE
    private val muted = Color.rgb(164, 171, 184)
    private val accent = Color.rgb(110, 231, 183)

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        executor = ContextCompat.getMainExecutor(this)
        build()
        refresh()
    }

    override fun onResume() {
        super.onResume()
        repo.tickSocialUse()
        if (::wallet.isInitialized) refresh()
    }

    private fun build() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg) }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(22, 24, 22, 28) }
        scroll.addView(root)
        setContentView(scroll)

        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        val brand = TextView(this).apply { text = "timewallet"; textSize = 25f; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD); setTextColor(paletteText) }
        header.addView(brand, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f))
        header.addView(pill("●  ${if (app.supabase.isLoggedIn()) "Online" else "Offline"}", if (app.supabase.isLoggedIn()) accent else muted))
        root.addView(header); root.addView(space(18))
        root.addView(TextView(this).apply { text = "Deine Zeit, bewusst genutzt."; textSize = 16f; setTextColor(muted) })
        root.addView(space(10))

        val walletCard = cardView()
        walletCard.addView(label("WALLET", muted, 12f))
        wallet = TextView(this).apply { textSize = 42f; typeface = Typeface.DEFAULT_BOLD; setTextColor(paletteText) }
        walletCard.addView(wallet); walletCard.addView(space(4)); walletCard.addView(label("Coins werden durch produktive Aufgaben verdient.", muted, 13f))
        root.addView(walletCard); root.addView(space(12))

        val socialCard = cardView()
        socialCard.addView(label("VERFÜGBARE SOCIAL-ZEIT", muted, 12f))
        social = TextView(this).apply { textSize = 31f; typeface = Typeface.DEFAULT_BOLD; setTextColor(paletteText) }
        socialCard.addView(social); socialCard.addView(space(4)); socialCard.addView(label("2 Coins = 1 Minute", muted, 13f))
        root.addView(socialCard); root.addView(space(14))
        root.addView(actionButton("Zeit kaufen") { buyDialog() }); root.addView(space(9))
        root.addView(secondaryButton("＋  Produktive Aufgabe") { startActivity(Intent(this, EvidenceActivity::class.java)) }); root.addView(space(18))

        val sessionCard = cardView()
        sessionCard.addView(label("FOKUS", muted, 12f))
        session = TextView(this).apply { textSize = 18f; typeface = Typeface.DEFAULT_BOLD; setTextColor(paletteText) }
        sessionCard.addView(session); sessionCard.addView(space(12)); sessionCard.addView(secondaryButton("Session starten") { sessionDialog() })
        root.addView(sessionCard); root.addView(space(18))

        root.addView(label("DEIN TIMEWALLET", muted, 12f)); root.addView(space(8))
        root.addView(menuRow("▣", "Aufgaben & Nachweise", "Produktivität in Coins umwandeln") { startActivity(Intent(this, EvidenceActivity::class.java)) })
        root.addView(menuRow("◉", "Apps & Sperren", "Social Media und Spiele verwalten") { appPicker() })
        root.addView(menuRow("◷", "Nutzungsdaten", "Android Usage Access aktivieren") { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) })
        root.addView(menuRow("▣", "Blocking", "Accessibility-Sperre aktivieren") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) })
        root.addView(menuRow("⌂", "Konto & Sicherheit", "Login, PIN und Biometrie") { accountDialog() })
        root.addView(menuRow("!", "Notfall-Switch", "Sicherheitsmodus") { emergencyDialog() })
        root.addView(menuRow("ⓘ", "Datenschutz & Bedingungen", "Rechtliche Informationen") { legalDialog() })
    }

    private fun refresh() {
        repo.tickSocialUse()
        wallet.text = "${repo.coins()} Coins"
        social.text = formatTime(repo.socialSeconds())
        session.text = if (repo.isProductivitySessionRunning()) "Fokus aktiv  •  ${repo.currentTask()}\nNoch ${formatTime(repo.remainingSessionSeconds())}" else "Bereit für deine nächste Fokus-Session"
    }

    private fun formatTime(seconds: Long): String {
        val s = seconds.coerceAtLeast(0)
        return if (s >= 3600) String.format("%dh %02dm", s / 3600, (s % 3600) / 60) else String.format("%02dm %02ds", s / 60, s % 60)
    }

    private fun cardView() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 18, 20, 18); setBackgroundColor(card); layoutParams = LinearLayout.LayoutParams(-1, ViewGroup.LayoutParams.WRAP_CONTENT) }

    private fun menuRow(icon: String, title: String, sub: String, action: () -> Unit): View {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(14, 14, 14, 14); setBackgroundColor(card); setOnClickListener { action() } }
        row.addView(TextView(this).apply { text = icon; textSize = 21f; setTextColor(accent); gravity = Gravity.CENTER }, LinearLayout.LayoutParams(42, 60))
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(8, 0, 0, 0) }
        box.addView(label(title, paletteText, 16f, true)); box.addView(label(sub, muted, 12f)); row.addView(box, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)); row.addView(label("›", muted, 26f), LinearLayout.LayoutParams(30, ViewGroup.LayoutParams.WRAP_CONTENT))
        return row
    }

    private fun label(value: String, color: Int, size: Float, bold: Boolean = false) = TextView(this).apply { text = value; textSize = size; setTextColor(color); if (bold) typeface = Typeface.DEFAULT_BOLD }
    private fun pill(value: String, color: Int) = TextView(this).apply { text = value; textSize = 12f; setTextColor(color); setPadding(12, 7, 12, 7) }
    private fun actionButton(value: String, action: () -> Unit) = Button(this).apply { text = value; textSize = 16f; typeface = Typeface.DEFAULT_BOLD; setTextColor(Color.rgb(8, 12, 14)); setBackgroundColor(accent); setOnClickListener { action() }; layoutParams = LinearLayout.LayoutParams(-1, 58) }
    private fun secondaryButton(value: String, action: () -> Unit) = Button(this).apply { text = value; textSize = 14f; setTextColor(paletteText); setBackgroundColor(card2); setOnClickListener { action() }; layoutParams = LinearLayout.LayoutParams(-1, 52) }
    private fun space(px: Int) = Space(this).apply { layoutParams = LinearLayout.LayoutParams(1, px) }

    private fun accountDialog() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(8, 0, 8, 0) }
        val e = EditText(this).apply { hint = "E-Mail" }; val p = EditText(this).apply { hint = "Passwort"; inputType = 129 }; box.addView(e); box.addView(p)
        AlertDialog.Builder(this).setTitle("Konto").setView(box).setPositiveButton("Login") { _, _ -> app.supabase.signIn(e.text.toString(), p.text.toString()) { _, msg -> runOnUiThread { toast(msg); refresh() } } }.setNeutralButton("Registrieren") { _, _ -> app.supabase.signUp(e.text.toString(), p.text.toString()) { _, msg -> runOnUiThread { toast(msg) } } }.setNegativeButton("Passwort vergessen") { _, _ -> app.supabase.resetPassword(e.text.toString()) { _, msg -> runOnUiThread { toast(msg) } } }.show()
    }

    private fun appPicker() {
        val apps = installedLaunchableApps(this); val selected = repo.blockedPackages().toMutableSet(); val labels = apps.map { it.label }.toTypedArray(); val checked = apps.map { selected.contains(it.packageName) }.toBooleanArray()
        AlertDialog.Builder(this).setTitle("Apps sperren").setMultiChoiceItems(labels, checked) { _, i, yes -> if (yes) selected.add(apps[i].packageName) else selected.remove(apps[i].packageName) }.setPositiveButton("Speichern") { _, _ -> repo.setBlockedPackages(selected); refresh() }.setNegativeButton("Abbrechen", null).show()
    }

    private fun sessionDialog() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(8, 0, 8, 0) }; val t = EditText(this).apply { hint = "Was möchtest du erledigen?" }; val m = EditText(this).apply { hint = "Minuten"; setText("25"); inputType = 2 }; box.addView(t); box.addView(m)
        AlertDialog.Builder(this).setTitle("Fokus starten").setView(box).setPositiveButton("Start") { _, _ -> repo.startSession(t.text.toString().ifBlank { "Produktive Aufgabe" }, m.text.toString().toIntOrNull()?.coerceIn(1, 180) ?: 25); refresh() }.setNegativeButton("Abbrechen", null).show()
    }

    private fun buyDialog() {
        val options = arrayOf(15 to 30, 30 to 56, 60 to 110)
        AlertDialog.Builder(this).setTitle("Social-Zeit kaufen").setItems(arrayOf("15 Minuten  •  30 Coins", "30 Minuten  •  56 Coins", "60 Minuten  •  110 Coins")) { _, i -> if (!repo.buySocialTime(options[i].first, options[i].second)) toast("Nicht genug Coins oder Fokus aktiv") else refresh() }.show()
    }

    private fun pinDialog() {
        val p = EditText(this).apply { hint = "4–8 Ziffern"; inputType = 2 }
        AlertDialog.Builder(this).setTitle("PIN setzen").setView(p).setPositiveButton("Speichern") { _, _ -> toast(if (app.security.setPin(p.text.toString())) "PIN gespeichert" else "Ungültige PIN") }.setNegativeButton("Abbrechen", null).show()
    }

    private fun biometricDialog() {
        if (!app.security.canUseBiometric()) { toast("Biometrie nicht verfügbar"); return }
        BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() { override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) { app.security.setBiometricEnabled(true); toast("Biometrie aktiviert") } }).authenticate(BiometricPrompt.PromptInfo.Builder().setTitle("TimeWallet Biometrie").setSubtitle("Biometrie aktivieren").setNegativeButtonText("Abbrechen").build())
    }

    private fun emergencyDialog() {
        val enable = !repo.isEmergencySwitchEnabled()
        AlertDialog.Builder(this).setTitle("Notfall-Switch").setMessage(if (enable) "Notfallmodus aktivieren?" else "Notfallmodus deaktivieren?").setPositiveButton("Bestätigen") { _, _ -> repo.setEmergencySwitchEnabled(enable); refresh() }.setNegativeButton("Abbrechen", null).show()
    }

    private fun legalDialog() {
        AlertDialog.Builder(this).setTitle("Datenschutz & Bedingungen").setMessage("TimeWallet verarbeitet Kontodaten für Anmeldung und Synchronisierung. Usage Access und Accessibility sind optionale Android-Systemberechtigungen für Nutzungsanalyse und Sperrfunktionen. Die Rechtstexte dieser Testversion sind Vorlagen und müssen vor Veröffentlichung rechtlich geprüft und an den Betreiber angepasst werden.").setPositiveButton("OK", null).show()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

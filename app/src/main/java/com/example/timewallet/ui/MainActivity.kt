package com.example.timewallet.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.example.timewallet.R
import com.example.timewallet.TimeWalletApp

class MainActivity : ComponentActivity() {
    private val repo get() = (application as TimeWalletApp).repository
    private lateinit var root: LinearLayout
    private lateinit var status: TextView
    private lateinit var wallet: TextView
    private lateinit var social: TextView

    private val bg = Color.rgb(8, 10, 14)
    private val card = Color.rgb(20, 24, 31)
    private val white = Color.rgb(245, 247, 250)
    private val muted = Color.rgb(160, 170, 184)
    private val accent = Color.rgb(0, 220, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        refresh()
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun buildUi() {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(20, 24, 20, 20); setBackgroundColor(bg) }
        val scroll = ScrollView(this).apply { addView(root) }
        setContentView(scroll)
        addText("TIMEWALLET", 14, accent, true)
        addText("Deine Zeit. Dein Wallet.", 30, white, true)
        addText("Produktive Aufgaben erledigen → Coins verdienen → Social-Zeit kaufen.", 14, muted, false)

        val walletCard = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(22, 20, 22, 20); setBackgroundColor(card) }
        wallet = TextView(this).apply { textSize = 34f; setTextColor(white); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        social = TextView(this).apply { textSize = 15f; setTextColor(muted); setPadding(0, 8, 0, 0) }
        walletCard.addView(wallet); walletCard.addView(social)
        root.addView(walletCard, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 24 })

        status = TextView(this).apply { textSize = 14f; setTextColor(muted); setPadding(0, 18, 0, 12) }
        root.addView(status)
        addButton("Produktivitäts-Session starten") { startSessionDialog() }
        addButton("Social-Zeit kaufen") { buyDialog() }
        addButton("Aufgaben & Coin-Werte") { taskDialog() }
        addButton("App-Blocking einrichten") { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        addButton("Profil & Datenschutz") { profileDialog() }
        addButton("Notfall-Switch") { emergencyDialog() }
        addText("WhatsApp und Snapchat bleiben gemäß deiner Vorgabe ausgenommen. Spiele sind standardmäßig nicht als Social-Zeit freigeschaltet.", 12, muted, false)
        addText("TimeWallet • Offline-first • lokale Nachweise • keine Werbe-/KI-API erforderlich", 11, muted, false)
    }

    private fun refresh() {
        val s = repo.remainingSessionSeconds()
        if (s == 0L && repo.isProductivitySessionRunning()) repo.endSession()
        wallet.text = "${repo.coins()} 🪙"
        social.text = "${repo.socialSeconds() / 60} Minuten Social-Zeit verfügbar"
        status.text = if (repo.isProductivitySessionRunning()) "🛡 FOKUS AKTIV • ${repo.currentTask()} • ${repo.remainingSessionSeconds() / 60}:${String.format("%02d", repo.remainingSessionSeconds() % 60)}" else "Bereit für deine nächste produktive Aufgabe."
    }

    private fun startSessionDialog() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(30, 10, 30, 0) }
        val task = EditText(this).apply { hint = "Aufgabe, z. B. Mathe lernen" }
        val minutes = EditText(this).apply { hint = "Dauer in Minuten"; inputType = 2; setText("25") }
        box.addView(task); box.addView(minutes)
        AlertDialogBuilder(this, "Produktivität", box, "Starten") {
            repo.startSession(task.text.toString().ifBlank { "Produktive Aufgabe" }, minutes.text.toString().toIntOrNull()?.coerceIn(1, 180) ?: 25); refresh()
        }
    }

    private fun buyDialog() {
        val options = arrayOf("15 Minuten • 15 Coins", "30 Minuten • 28 Coins", "60 Minuten • 50 Coins")
        AlertDialog.Builder(this).setTitle("Social-Zeit kaufen").setItems(options) { _, which ->
            val values = arrayOf(15 to 15, 30 to 28, 60 to 50); val (min, cost) = values[which]
            if (repo.isProductivitySessionRunning()) Toast.makeText(this, "Während Fokus bleibt Social gesperrt.", Toast.LENGTH_SHORT).show()
            else if (!repo.buySocialTime(min, cost)) Toast.makeText(this, "Nicht genügend Coins.", Toast.LENGTH_SHORT).show()
            refresh()
        }.show()
    }

    private fun taskDialog() {
        AlertDialog.Builder(this).setTitle("Aufgaben & Coin-Werte").setItems(arrayOf("Vokabeln lernen • 25 Coins", "Coden lernen • 45 Coins", "Schulaufgaben • 30 Coins", "Lesen • 25 Coins")) { _, which ->
            val tasks = arrayOf("Vokabeln lernen" to 25, "Coden lernen" to 45, "Schulaufgaben" to 30, "Lesen" to 25); val t = tasks[which]
            repo.startSession(t.first, 25); refresh()
        }.show()
    }

    private fun profileDialog() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(30, 10, 30, 0) }
        val name = EditText(this).apply { hint = "Name"; setText(repo.profileName()) }
        val email = EditText(this).apply { hint = "E-Mail"; setText(repo.profileEmail()) }
        box.addView(name); box.addView(email)
        AlertDialogBuilder(this, "Profil", box, "Speichern") { repo.saveProfile(name.text.toString(), email.text.toString()); Toast.makeText(this, "Profil gespeichert", Toast.LENGTH_SHORT).show() }
    }

    private fun emergencyDialog() {
        val enabled = !repo.isEmergencySwitchEnabled()
        AlertDialog.Builder(this).setTitle("Notfall-Switch").setMessage(if (enabled) "Emergency-Modus aktivieren? Er darf nur als kontrollierte Ausstiegsmöglichkeit dienen." else "Emergency-Modus deaktivieren?").setPositiveButton(if (enabled) "Aktivieren" else "Deaktivieren") { _, _ -> repo.setEmergencySwitchEnabled(enabled); refresh() }.setNegativeButton("Abbrechen", null).show()
    }

    private fun addText(value: String, size: Int, color: Int, bold: Boolean) { root.addView(TextView(this).apply { text = value; textSize = size.toFloat(); setTextColor(color); if (bold) typeface = android.graphics.Typeface.DEFAULT_BOLD; setPadding(0, 6, 0, 4) }) }
    private fun addButton(label: String, action: () -> Unit) { root.addView(Button(this).apply { text = label; setTextColor(white); setOnClickListener { action() }; backgroundTintList = android.content.res.ColorStateList.valueOf(Color.rgb(32, 38, 48)); setPadding(12, 6, 12, 6) }, LinearLayout.LayoutParams(-1, 54).apply { topMargin = 8 }) }
    private fun AlertDialogBuilder(context: android.content.Context, title: String, view: LinearLayout, positive: String, action: () -> Unit) { AlertDialog.Builder(context).setTitle(title).setView(view).setPositiveButton(positive) { _, _ -> action() }.setNegativeButton("Abbrechen", null).show() }
}

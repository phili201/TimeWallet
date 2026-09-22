package com.example.timewallet.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.camera.CameraActivity
import com.example.timewallet.timer.TimerViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val app get() = application as TimeWalletApp
    private val viewModel: TimerViewModel by viewModels { TimerViewModelFactoryCompat(app) }
    private lateinit var content: LinearLayout
    private lateinit var nav: LinearLayout
    private var selected = 0
    private var timerText: TextView? = null
    private var timerStatus: TextView? = null
    private var timerProgress: CircularProgressIndicator? = null
    private var coinsText: TextView? = null
    private var socialText: TextView? = null
    private var messageText: TextView? = null

    private val bg = Color.rgb(5, 6, 8)
    private val surface = Color.rgb(17, 19, 24)
    private val surface2 = Color.rgb(24, 27, 33)
    private val cyan = Color.rgb(0, 229, 255)
    private val text = Color.rgb(245, 247, 250)
    private val secondary = Color.rgb(160, 170, 184)

    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.getStringExtra("photoPath")?.let(viewModel::finishSession)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = surface
        buildShell()
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                coinsText?.text = "${state.coins} 🪙"
                socialText?.text = if (state.isRunning) "Während Fokus gesperrt" else "${state.socialRemainingMinutes} Minuten verfügbar"
                val totalSeconds = state.sessionMinutes * 60
                val remaining = state.remainingSeconds.coerceAtLeast(0)
                timerText?.text = String.format("%02d:%02d", remaining / 60, remaining % 60)
                timerStatus?.text = if (state.isRunning) "Fokus läuft • Social Apps gesperrt" else "Bereit für Fokus"
                timerProgress?.progress = if (totalSeconds > 0) (((state.elapsedSeconds * 100f) / totalSeconds).toInt()).coerceIn(0, 100) else 0
                messageText?.text = state.message.orEmpty()
            }
        }
        showPage(0)
    }

    private fun buildShell() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg) }
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(12), dp(18), dp(12)) }
        val scroll = ScrollView(this).apply { isFillViewport = true; addView(content) }
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        nav = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(6, 6, 6, 6); background = rounded(surface, 0) }
        listOf("⌂" to "Home", "✓" to "Aufgaben", "◉" to "Wallet", "⚙" to "Profil").forEachIndexed { i, pair ->
            val item = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; isClickable = true; setPadding(2, 4, 2, 4); setOnClickListener { showPage(i) } }
            val icon = TextView(this).apply { text = pair.first; textSize = 23f; gravity = Gravity.CENTER; setTextColor(this@MainActivity.text) }
            val label = TextView(this).apply { text = pair.second; textSize = 11f; gravity = Gravity.CENTER; setTextColor(this@MainActivity.text); setPadding(0, 2, 0, 0) }
            item.addView(icon, LinearLayout.LayoutParams(-1, 34.dp)); item.addView(label, LinearLayout.LayoutParams(-1, 22.dp)); nav.addView(item, LinearLayout.LayoutParams(0, 66.dp, 1f))
        }
        root.addView(nav, LinearLayout.LayoutParams(-1, 78.dp))
        setContentView(root)
    }

    private fun showPage(page: Int) {
        selected = page
        content.removeAllViews()
        when (page) { 0 -> homePage(); 1 -> tasksPage(); 2 -> walletPage(); 3 -> profilePage() }
        for (i in 0 until nav.childCount) nav.getChildAt(i).alpha = if (i == selected) 1f else .55f
    }

    private fun homePage() {
        header("Guten Tag", "TimeWallet", true)
        val timerCard = card()
        timerCard.addView(tv("Fokus-Timer", 17, text, true))
        val frame = android.widget.FrameLayout(this)
        timerProgress = CircularProgressIndicator(this).apply { max = 100; progress = 0; isIndeterminate = false; setIndicatorColor(cyan); trackColor = Color.rgb(45, 50, 58); trackThickness = dp(11); indicatorSize = dp(210) }
        frame.addView(timerProgress, android.widget.FrameLayout.LayoutParams(dp(220), dp(220), Gravity.CENTER))
        val center = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER }
        timerText = tv("25:00", 38, text, true).apply { gravity = Gravity.CENTER }
        timerStatus = tv("Bereit für Fokus", 12, secondary, false).apply { gravity = Gravity.CENTER; maxLines = 2 }
        center.addView(timerText); center.addView(timerStatus); frame.addView(center, android.widget.FrameLayout.LayoutParams(dp(190), dp(120), Gravity.CENTER))
        timerCard.addView(frame, LinearLayout.LayoutParams(-1, 250.dp))
        messageText = tv("", 13, secondary, false).apply { gravity = Gravity.CENTER; setPadding(4, 4, 4, 4) }
        timerCard.addView(messageText)
        content.addView(timerCard)

        val taskInput = EditText(this).apply { hint = "z. B. Vokabeln lernen"; setTextColor(this@MainActivity.text); setHintTextColor(secondary); setSingleLine(true) }
        val duration = EditText(this).apply { hint = "Min"; inputType = 2; setTextColor(this@MainActivity.text); setHintTextColor(secondary); setText("25"); setSingleLine(true) }
        content.addView(tv("Schnell starten", 19, text, true).apply { setPadding(0, dp(18), 0, dp(8)) })
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        row.addView(taskInput, LinearLayout.LayoutParams(0, 56.dp, 1f)); row.addView(duration, LinearLayout.LayoutParams(72.dp, 56.dp).apply { leftMargin = dp(8) }); content.addView(row)
        content.addView(button("Session starten") { viewModel.startSession(taskInput.text.toString().ifBlank { "Allgemein" }, duration.text.toString().toIntOrNull() ?: 25) })
        content.addView(button("Foto zur Bestätigung aufnehmen") { cameraLauncher.launch(Intent(this, CameraActivity::class.java)) })
        content.addView(tv("Deine Produktivität", 19, text, true).apply { setPadding(0, dp(18), 0, dp(8)) })
        val status = card(); status.addView(tv("🛡  Fokus schützt deine Zeit", 16, text, true)); status.addView(tv("Während einer Session bleiben Instagram und YouTube gesperrt – auch wenn Social-Zeit gekauft wurde.", 13, secondary, false).apply { setPadding(0, dp(6), 0, 0) }); content.addView(status)
    }

    private fun tasksPage() {
        header("Produktivität", "Deine Aufgaben", false)
        content.addView(tv("Verdiene Coins, indem du echte Zeit in produktive Aufgaben investierst.", 14, secondary, false).apply { setPadding(0, 0, 0, dp(12)) })
        listOf("📚  Vokabeln lernen" to 25, "💻  Coden lernen" to 45, "🎓  Schulaufgaben" to 30, "📖  Lesen" to 25).forEach { (name, mins) ->
            val c = card(); c.addView(button("$name   •   $mins Min") { viewModel.startSession(name.substringAfter("  "), mins); showPage(0) }); content.addView(c)
        }
        val custom = card(); custom.addView(tv("Eigene Aufgabe", 16, text, true)); val input = EditText(this).apply { hint = "Aufgabe"; setTextColor(this@MainActivity.text); setHintTextColor(secondary); setSingleLine() }; val min = EditText(this).apply { hint = "Minuten"; inputType = 2; setTextColor(this@MainActivity.text); setHintTextColor(secondary); setSingleLine() }; custom.addView(input); custom.addView(min); custom.addView(button("Eigene Session starten") { viewModel.startSession(input.text.toString().ifBlank { "Eigene Aufgabe" }, min.text.toString().toIntOrNull() ?: 25); showPage(0) }); content.addView(custom)
    }

    private fun walletPage() {
        header("Dein Guthaben", "Social Wallet", false)
        val balance = card(); coinsText = tv("0 🪙", 34, text, true); balance.addView(coinsText); socialText = tv("0 Minuten verfügbar", 14, secondary, false); balance.addView(socialText); content.addView(balance)
        content.addView(tv("Zeit kaufen", 19, text, true).apply { setPadding(0, dp(18), 0, dp(8)) })
        listOf(15 to 15, 30 to 28, 60 to 50).forEach { (minutes, cost) -> content.addView(button("$minutes Minuten     •     $cost 🪙") { if (app.repository.isProductivitySessionRunning()) Toast.makeText(this, "Während Fokus bleibt Social gesperrt.", Toast.LENGTH_SHORT).show() else viewModel.buySocialTime(minutes, cost) }) }
        content.addView(button("Coin-Verlauf anzeigen") { startActivity(Intent(this, com.example.timewallet.ui.history.CoinHistoryActivity::class.java)) })
        content.addView(tv("Jeder Kauf wird lokal in deiner Wallet gespeichert. Während Fokus wird gekaufte Zeit nicht verbraucht.", 13, secondary, false).apply { setPadding(0, dp(12), 0, 0) })
    }

    private fun profilePage() {
        header("Persönlich", "Profil & Einstellungen", false)
        val prefs = getSharedPreferences("profile", MODE_PRIVATE)
        val c = card(); c.addView(tv("Dein Profil", 18, text, true)); val name = EditText(this).apply { hint = "Dein Name"; setText(prefs.getString("name", "")); setTextColor(this@MainActivity.text); setHintTextColor(secondary); setSingleLine() }; c.addView(name); c.addView(button("Profil speichern") { prefs.edit().putString("name", name.text.toString().trim()).apply(); Toast.makeText(this, "Profil gespeichert", Toast.LENGTH_SHORT).show() }); content.addView(c)
        val security = card(); security.addView(tv("Sicherheit & Anti-Sucht", 18, text, true)); val anti = SwitchMaterial(this).apply { text = "Anti-Sucht-Modus"; isChecked = app.repository.isAntiAddictionModeEnabled(); setTextColor(this@MainActivity.text); setOnCheckedChangeListener { _, checked -> app.repository.setAntiAddictionMode(checked) } }; security.addView(anti); val emergency = SwitchMaterial(this).apply { text = "Notfall-Switch aktiv"; isChecked = app.repository.isEmergencySwitchEnabled(); setTextColor(this@MainActivity.text); setOnCheckedChangeListener { _, checked -> app.repository.setEmergencySwitchEnabled(checked) } }; security.addView(emergency); security.addView(tv("Der Notfall-Switch ist eine Sicherheitsausstiegsmöglichkeit und darf nie zum dauerhaften Aussperren vom eigenen Gerät führen.", 12, secondary, false).apply { setPadding(0, dp(8), 0, 0) }); content.addView(security)
        content.addView(button("Rechtliches") { startActivity(Intent(this, com.example.timewallet.ui.legal.LegalActivity::class.java)) })
        content.addView(tv("TimeWallet 1.0 • Material 3 Dark", 12, secondary, false).apply { gravity = Gravity.CENTER; setPadding(0, dp(20), 0, dp(20)) })
    }

    private fun header(kicker: String, title: String, showCoins: Boolean) {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        val logo = TextView(this).apply { text = "TW"; textSize = 17f; gravity = Gravity.CENTER; setTextColor(bg); background = rounded(cyan, 18); typeface = android.graphics.Typeface.DEFAULT_BOLD }
        row.addView(logo, LinearLayout.LayoutParams(48.dp, 48.dp)); val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12), 0, 0, 0) }; col.addView(tv(kicker, 12, secondary, false)); col.addView(tv(title, 24, text, true)); row.addView(col, LinearLayout.LayoutParams(0, -2, 1f)); if (showCoins) { coinsText = tv("0 🪙", 17, cyan, true).apply { gravity = Gravity.CENTER; background = rounded(surface2, 18); setPadding(dp(12), 0, dp(12), 0) }; row.addView(coinsText, LinearLayout.LayoutParams(-2, 44.dp)) }; content.addView(row); content.addView(View(this).apply { setBackgroundColor(Color.TRANSPARENT) }, LinearLayout.LayoutParams(1, dp(12)))
    }

    private fun card(): LinearLayout = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)); background = rounded(surface, 24); layoutParams = lp(1, 10) }
    private fun button(label: String, action: () -> Unit): MaterialButton = MaterialButton(this).apply { text = label; setTextSize(14f); isAllCaps = false; setTextColor(bg); backgroundTintList = android.content.res.ColorStateList.valueOf(cyan); cornerRadius = dp(16); minHeight = dp(52); setOnClickListener { action() }; layoutParams = lp(1, 8) }
    private fun tv(value: String, size: Int, color: Int, bold: Boolean): TextView = TextView(this).apply { text = value; textSize = size.toFloat(); setTextColor(color); if (bold) typeface = android.graphics.Typeface.DEFAULT_BOLD }
    private fun rounded(color: Int, radius: Int) = GradientDrawable().apply { setColor(color); cornerRadius = dp(radius).toFloat() }
    private fun lp(w: Int, margin: Int) = LinearLayout.LayoutParams(if (w == 1) -1 else w, -2).apply { topMargin = dp(margin) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private val Int.dp get() = dp(this)
}

// Kept as a tiny local adapter so MainActivity has no dependency on deprecated factories.
private class TimerViewModelFactoryCompat(private val app: TimeWalletApp) : androidx.lifecycle.ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimerViewModel(app) as T
        }
        throw IllegalArgumentException("Unknown ViewModel: ${modelClass.name}")
    }
}

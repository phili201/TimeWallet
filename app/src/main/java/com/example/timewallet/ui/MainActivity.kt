package com.example.timewallet.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.timewallet.R
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.databinding.ActivityMainBinding
import com.example.timewallet.timer.TimerViewModel
import com.example.timewallet.timer.TimerViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var binding: ActivityMainBinding
    private val app get() = application as TimeWalletApp
    private val viewModel: TimerViewModel by viewModels { TimerViewModelFactory(app) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        lifecycleScope.launch { viewModel.state.collect { state ->
            binding.coinsText.text = "🪙 ${state.coins} Coins"
            binding.socialTimeText.text = if (state.isRunning) "Social-Zeit: während Session gesperrt" else "Social-Zeit: ${state.socialRemainingMinutes} Min"
            binding.sessionTimeText.text = if (state.isRunning) "${state.currentTask}: ${state.remainingMinutes} Min verbleiben" else if (state.sessionMinutes > 0) "Letzte Session: ${state.sessionMinutes} Min" else "Keine aktive Session"
            binding.messageText.text = state.message ?: ""
            binding.startSessionButton.isEnabled = !state.isRunning
            binding.finishSessionButton.isEnabled = !state.isRunning && state.sessionMinutes > 0
        } }
        binding.startSessionButton.setOnClickListener {
            viewModel.startSession(binding.taskInput.text.toString().ifBlank { "Allgemein" }, binding.durationInput.text.toString().toIntOrNull() ?: 25)
        }
        binding.finishSessionButton.setOnClickListener { startActivityForResult(Intent(this, com.example.timewallet.camera.CameraActivity::class.java), REQUEST_PHOTO) }
        binding.buy15Button.setOnClickListener { buy(15, 15) }
        binding.buy30Button.setOnClickListener { buy(30, 28) }
        binding.buy60Button.setOnClickListener { buy(60, 50) }
        binding.antiAddictionSwitch.setOnCheckedChangeListener { _, checked -> app.repository.setAntiAddictionMode(checked) }
        binding.emergencyButton.setOnClickListener { Toast.makeText(this, "Notfall-Modus: Android-Einstellungen → Bedienungshilfen → TimeWallet deaktivieren, falls der Blocker festhängt.", Toast.LENGTH_LONG).show() }
        binding.bottomNav.root.findViewById<android.view.View>(R.id.navHome)?.setOnClickListener { binding.root.scrollTo(0, 0) }
        binding.bottomNav.root.findViewById<android.view.View>(R.id.navStats)?.setOnClickListener { startActivity(Intent(this, com.example.timewallet.ui.history.CoinHistoryActivity::class.java)) }
        binding.bottomNav.root.findViewById<android.view.View>(R.id.navSettings)?.setOnClickListener { startActivity(Intent(this, com.example.timewallet.ui.settings.SettingsActivity::class.java)) }
        binding.bottomNav.root.findViewById<android.view.View>(R.id.navLegal)?.setOnClickListener { startActivity(Intent(this, com.example.timewallet.ui.legal.LegalActivity::class.java)) }
    }

    private fun buy(minutes: Int, cost: Int) { lifecycleScope.launch {
        if (app.repository.isProductivitySessionRunning()) { Toast.makeText(this@MainActivity, "Während der Session ist der Kauf gesperrt.", Toast.LENGTH_SHORT).show(); return@launch }
        val ok = app.repository.purchaseSocialTime(minutes, cost)
        Toast.makeText(this@MainActivity, if (ok) "$minutes Minuten gekauft." else "Nicht genug Coins.", Toast.LENGTH_SHORT).show()
    } }

    @Deprecated("Use Activity Result APIs for new code")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) { super.onActivityResult(requestCode, resultCode, data); if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) data?.getStringExtra("photoPath")?.let(viewModel::finishSession) }
    companion object { private const val REQUEST_PHOTO = 1001 }
}

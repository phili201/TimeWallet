package com.example.timewallet.ui

import android.content.Intent
import android.os.Bundle
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
    private val viewModel: TimerViewModel by viewModels { TimerViewModelFactory(application as TimeWalletApp) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = 0xFF0D0D0D.toInt()
        window.statusBarColor = 0xFF0D0D0D.toInt()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.coinsText.text = "🪙 ${state.coins} Coins"
                binding.socialTimeText.text = "Social-Zeit: ${state.socialRemainingMinutes} Min"
                binding.sessionTimeText.text = if (state.isRunning) {
                    "${state.currentTask}: ${state.remainingMinutes} Min verbleiben"
                } else if (state.sessionMinutes > 0) {
                    "Letzte Session: ${state.sessionMinutes} Min"
                } else "Keine aktive Session"
                binding.messageText.text = state.message ?: ""
                binding.startSessionButton.isEnabled = !state.isRunning
                binding.finishSessionButton.isEnabled = !state.isRunning && state.sessionMinutes > 0
            }
        }

        binding.startSessionButton.setOnClickListener {
            val task = binding.taskInput.text.toString().ifBlank { "Allgemein" }
            val minutes = binding.durationInput.text.toString().toIntOrNull() ?: 25
            viewModel.startSession(task, minutes)
        }

        binding.finishSessionButton.setOnClickListener {
            startActivityForResult(Intent(this, com.example.timewallet.camera.CameraActivity::class.java), REQUEST_PHOTO)
        }

        val bottomNav = binding.bottomNav.root
        bottomNav.findViewById<android.view.View>(R.id.navHome)?.setOnClickListener { binding.root.scrollTo(0, 0) }
        bottomNav.findViewById<android.view.View>(R.id.navStats)?.setOnClickListener {
            startActivity(Intent(this, com.example.timewallet.ui.history.CoinHistoryActivity::class.java))
        }
        bottomNav.findViewById<android.view.View>(R.id.navSettings)?.setOnClickListener {
            startActivity(Intent(this, com.example.timewallet.ui.settings.SettingsActivity::class.java))
        }
        bottomNav.findViewById<android.view.View>(R.id.navLegal)?.setOnClickListener {
            startActivity(Intent(this, com.example.timewallet.ui.legal.LegalActivity::class.java))
        }
    }

    @Deprecated("Use Activity Result APIs for new code")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PHOTO && resultCode == RESULT_OK) {
            data?.getStringExtra("photoPath")?.let(viewModel::finishSession)
        }
    }

    companion object { private const val REQUEST_PHOTO = 1001 }
}

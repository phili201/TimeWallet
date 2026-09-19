package com.example.timewallet.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.databinding.ActivityMainBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: TimerViewModel by viewModels {
        val app = application as TimeWalletApp
        TimerViewModelFactory(app.repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // State beobachten
        lifecycleScope.launch {
            viewModel.state.collectLatest { state ->
                binding.coinsText.text = "Coins: ${state.coins}"
                binding.messageText.text = state.message ?: ""
            }
        }

        viewModel.loadCoins()

        // Session starten
        binding.startSessionButton.setOnClickListener {
            val task = binding.taskInput.text.toString().ifBlank { "Allgemein" }
            val duration = binding.durationInput.text.toString().toIntOrNull() ?: 25
            viewModel.startSession(task, duration)
        }

        // Session beenden -> Kamera öffnen
        binding.finishSessionButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivityForResult(intent, 1001)
        }

        // Social-Media-Zeit kaufen
        binding.buyTimeButton.setOnClickListener {
            val minutes = binding.buyMinutesInput.text.toString().toIntOrNull() ?: 30
            viewModel.buySocialTime(minutes)
        }
    }

    // Foto-Ergebnis von CameraActivity abholen
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val photoPath = data?.getStringExtra("photoPath") ?: return
            viewModel.finishSession(photoPath)
        }
    }
}


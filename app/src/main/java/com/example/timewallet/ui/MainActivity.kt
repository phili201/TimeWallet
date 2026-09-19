package com.example.timewallet.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import com.example.timewallet.databinding.ActivityMainBinding
import com.example.timewallet.timer.TimerViewModel
import com.example.timewallet.timer.TimerViewModelFactory
import com.example.timewallet.camera.CameraActivity

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: TimerViewModel by viewModels {
        TimerViewModelFactory(application as com.example.timewallet.TimeWalletApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Coins laden
        viewModel.loadCoins()

        // UI-Updates
        viewModel.state.observe(this) { state ->
            binding.coinText.text = "${state.coins} Coins"
            binding.statusText.text = state.message ?: ""
            binding.timerText.text = "${state.remainingMinutes} min"
        }

        // Session starten
        binding.startSessionButton.setOnClickListener {
            val task = binding.taskInput.text.toString()
            val minutes = binding.minutesInput.text.toString().toIntOrNull() ?: 0
            viewModel.startSession(task, minutes)
        }

        // Session beenden → Kamera öffnen
        binding.endSessionButton.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivityForResult(intent, 1001)
        }

        // Social Media kaufen
        binding.buySocialButton.setOnClickListener {
            val minutes = binding.buyMinutesInput.text.toString().toIntOrNull() ?: 0
            viewModel.buySocialTime(minutes)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val photoPath = data?.getStringExtra("photoPath") ?: return
            viewModel.finishSession(photoPath)
        }
    }
}

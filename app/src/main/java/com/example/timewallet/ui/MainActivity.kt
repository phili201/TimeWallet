package com.example.timewallet.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.databinding.ActivityMainBinding
import com.example.timewallet.timer.TimerViewModel
import com.example.timewallet.timer.TimerViewModelFactory
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: TimerViewModel by viewModels {
        TimerViewModelFactory(application as TimeWalletApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tesla Dark Bars
        window.navigationBarColor = 0xFF0D0D0D.toInt()
        window.statusBarColor = 0xFF0D0D0D.toInt()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.coinsText.text = "Coins: ${state.coins}"
                binding.messageText.text = state.message ?: ""
            }
        }

        binding.startSessionButton.setOnClickListener {
            val task = binding.taskInput.text.toString().ifBlank { "Allgemein" }
            val minutes = binding.durationInput.text.toString().toIntOrNull() ?: 25
            viewModel.startSession(task, minutes)
        }

        binding.finishSessionButton.setOnClickListener {
            val intent = Intent(this, com.example.timewallet.camera.CameraActivity::class.java)
            startActivityForResult(intent, 1001)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val path = data?.getStringExtra("photoPath") ?: return
            viewModel.finishSession(path)
        }
    }
}

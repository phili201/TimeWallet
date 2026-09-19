package com.example.timewallet.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.databinding.ActivityMainBinding
import com.example.timewallet.timer.TimerViewModel
import com.example.timewallet.timer.TimerViewModelFactory
import kotlinx.coroutines.launch
import android.view.animation.AnimationUtils
import java.io.File

class MainActivity : ComponentActivity() {


    private val viewModel: TimerViewModel by viewModels {
        val app = application as TimeWalletApp
        TimerViewModelFactory(app.repository)
    }

    private var lastPhotoUri: Uri? = null

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success && lastPhotoUri != null) {
                viewModel.finishSession(lastPhotoUri!!.path ?: "")
            } else {
                binding.messageText.text = "Foto fehlgeschlagen."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // -------------------------------
        // SESSION STARTEN
        // -------------------------------
        binding.startSessionButton.setOnClickListener {
            val task = binding.taskInput.text.toString()
            val minutesText = binding.durationInput.text.toString()

            if (task.isBlank() || minutesText.isBlank()) {
                binding.messageText.text = "Bitte Aufgabe und Dauer eingeben."
                return@setOnClickListener
            }

            val minutes = minutesText.toIntOrNull()
            if (minutes == null || minutes <= 0) {
                binding.messageText.text = "Ungültige Minutenangabe."
                return@setOnClickListener
            }

            viewModel.startSession(task, minutes)
        }

        // -------------------------------
        // SESSION BEENDEN → FOTO MACHEN
        // -------------------------------
        binding.finishSessionButton.setOnClickListener {
            openCamera()
        }

        // -------------------------------
        // SOCIAL MEDIA ZEIT KAUFEN
        // -------------------------------
        binding.buyTimeButton.setOnClickListener {
            val minutesText = binding.buyMinutesInput.text.toString()
            val minutes = minutesText.toIntOrNull()

            if (minutes == null || minutes <= 0) {
                binding.messageText.text = "Ungültige Minutenangabe."
                return@setOnClickListener
            }

            viewModel.buySocialMediaTime(minutes)
        }

        // -------------------------------
        // TIMER + COINS + MESSAGE LIVE UPDATEN
        // -------------------------------
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                binding.timerText.text = state.remainingMinutesFormatted
                binding.coinsText.text = "Coins: ${state.coins}"
                binding.messageText.text = state.message

                // Coin-Animation

                // Timer-Animation
            }
        }
    }

    private fun openCamera() {
        val photoFile = File(getExternalFilesDir(null), "session_photo_${System.currentTimeMillis()}.jpg")
        lastPhotoUri = FileProvider.getUriForFile(this, "${packageName}.provider", photoFile)
        cameraLauncher.launch(lastPhotoUri)
    }
}

// --- Heartbeat-Sender (App-Lebenszeichen) ---
lifecycleScope.launch {
    val app = application as TimeWalletApp
    val repository = app.repository

    while (true) {
        repository.updateHeartbeat()
        kotlinx.coroutines.delay(2000L)
    }
}

val bounce = AnimationUtils.loadAnimation(this@MainActivity, R.anim.coin_bounce)
binding.coinsText.startAnimation(bounce)

private fun updateScoreUI(score: Int, reason: String) {
    val emoji = when {
        score >= 80 -> "🔥"
        score >= 60 -> "💪"
        score >= 40 -> "🙂"
        score >= 20 -> "😕"
        else -> "❌"
    }

    val color = when {
        score >= 80 -> android.graphics.Color.GREEN
        score >= 60 -> android.graphics.Color.parseColor("#8BC34A")
        score >= 40 -> android.graphics.Color.YELLOW
        score >= 20 -> android.graphics.Color.parseColor("#FFA000")
        else -> android.graphics.Color.RED
    }

    binding.scoreEmoji.text = emoji
    binding.scoreBar.progress = score
    binding.scoreBar.progressTintList =
        android.content.res.ColorStateList.valueOf(color)
}

lifecycleScope.launch {
    viewModel.score.collect { s ->
        viewModel.scoreReason.collect { r ->
            updateScoreUI(s, r)
        }
    }
}

lifecycleScope.launch {
    repository.socialMinutesFlow.collect { m ->
        binding.socialTimer.text = "$m min"
    }
}

lifecycleScope.launch {
    viewModel.score.collect { s ->
        viewModel.scoreReason.collect { r ->
            updateScoreUI(s, r)
        }
    }
}

lifecycleScope.launch {
    repository.socialMinutesFlow.collect { m ->
        binding.socialTimer.text = "$m min"
    }
}

binding.scoreBar.startAnimation(
    android.view.animation.AnimationUtils.loadAnimation(this, R.anim.neon_pulse)
)

package com.example.timewallet.ui.block

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.timewallet.databinding.ActivityBlockScreenBinding

class BlockScreenActivity : ComponentActivity() {
    companion object { const val EXTRA_BLOCKED_PACKAGE = "blockedPackage" }
    private lateinit var binding: ActivityBlockScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = 0xFF0D0D0D.toInt()
        window.statusBarColor = 0xFF0D0D0D.toInt()
        binding = ActivityBlockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pkg = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE).orEmpty()
        binding.blockCountdown.text = when (pkg) {
            "com.instagram.android" -> "Instagram ist blockiert"
            "com.google.android.youtube" -> "YouTube ist blockiert"
            else -> "Zugang blockiert"
        }
        binding.backButton.setOnClickListener { finishAndRemoveTask() }
    }
}

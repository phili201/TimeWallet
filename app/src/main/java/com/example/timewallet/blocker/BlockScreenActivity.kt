package com.example.timewallet.ui.block

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.timewallet.databinding.ActivityBlockScreenBinding

class BlockScreenActivity : ComponentActivity() {

    private lateinit var binding: ActivityBlockScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBlockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.blockCountdown.text = "Zugang blockiert"
        binding.backButton.setOnClickListener { finish() }
    }
}

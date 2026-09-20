package com.example.timewallet.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.timewallet.databinding.ActivitySettingsBinding

class SettingsActivity : ComponentActivity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Tesla Dark Bars
        window.navigationBarColor = 0xFF0D0D0D.toInt()
        window.statusBarColor = 0xFF0D0D0D.toInt()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }
    }
}

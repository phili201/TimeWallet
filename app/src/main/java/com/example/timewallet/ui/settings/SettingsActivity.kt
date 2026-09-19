package com.example.timewallet.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.timewallet.databinding.ActivitySettingsBinding

class SettingsActivity : ComponentActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}

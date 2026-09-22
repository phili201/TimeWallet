package com.example.timewallet.ui.settings

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.databinding.ActivitySettingsBinding

class SettingsActivity : ComponentActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.navigationBarColor = 0xFF0D0D0D.toInt()
        window.statusBarColor = 0xFF0D0D0D.toInt()
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repo = (application as TimeWalletApp).repository
        binding.emergencySwitch.isChecked = repo.isEmergencySwitchEnabled()
        binding.antiAddictionSwitch.isChecked = false
        binding.emergencySwitch.setOnCheckedChangeListener { _, enabled -> repo.setEmergencySwitchEnabled(enabled) }
        binding.antiAddictionSwitch.setOnCheckedChangeListener { _, enabled -> repo.setAntiAddictionMode(enabled) }
        binding.backButton.setOnClickListener { finish() }
    }
}

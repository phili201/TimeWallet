package com.example.timewallet.ui.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.example.timewallet.R
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.databinding.ActivitySettingsBinding
import com.example.timewallet.ui.legal.LegalActivity

class SettingsActivity : ComponentActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = getColor(R.color.tesla_black)
        window.navigationBarColor = getColor(R.color.tesla_surface)

        val prefs = getSharedPreferences("profile", MODE_PRIVATE)
        binding.nameInput.setText(prefs.getString("name", ""))
        val repo = (application as TimeWalletApp).repository
        binding.emergencySwitch.isChecked = repo.isEmergencySwitchEnabled()
        binding.antiAddictionSwitch.isChecked = repo.isAntiAddictionModeEnabled()
        binding.saveButton.setOnClickListener {
            prefs.edit().putString("name", binding.nameInput.text.toString().trim()).apply()
            Toast.makeText(this, "Profil gespeichert", Toast.LENGTH_SHORT).show()
        }
        binding.emergencySwitch.setOnCheckedChangeListener { _, enabled -> repo.setEmergencySwitchEnabled(enabled) }
        binding.antiAddictionSwitch.setOnCheckedChangeListener { _, enabled -> repo.setAntiAddictionMode(enabled) }
        binding.legalButton.setOnClickListener { startActivity(Intent(this, LegalActivity::class.java)) }
    }
}

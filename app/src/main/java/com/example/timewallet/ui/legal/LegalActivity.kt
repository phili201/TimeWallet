package com.example.timewallet.ui.legal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.timewallet.databinding.ActivityLegalBinding

class LegalActivity : ComponentActivity() {

    private lateinit var binding: ActivityLegalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = 0xFF0D0D0D.toInt()
        window.statusBarColor = 0xFF0D0D0D.toInt()

        binding = ActivityLegalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.privacyButton.setOnClickListener {
            startActivity(Intent(this, LegalViewerActivity::class.java).apply {
                putExtra("title", "Datenschutz")
                putExtra("content", "Hier stehen deine Datenschutzinformationen…")
            })
        }

        binding.imprintButton.setOnClickListener {
            startActivity(Intent(this, LegalViewerActivity::class.java).apply {
                putExtra("title", "Impressum")
                putExtra("content", "Hier steht dein Impressum…")
            })
        }

        binding.backButton.setOnClickListener { finish() }
    }
}

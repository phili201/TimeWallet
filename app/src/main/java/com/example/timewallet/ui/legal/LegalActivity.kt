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

        binding.privacyButton.setOnClickListener { openAsset("Datenschutz", "legal/datenschutz.txt") }
        binding.imprintButton.setOnClickListener { openAsset("Haftungsausschluss", "legal/haftungsausschluss.txt") }
        binding.backButton.setOnClickListener { finish() }
    }

    private fun openAsset(title: String, path: String) {
        val content = runCatching {
            assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }.getOrElse { "Der Dokumentinhalt konnte nicht geladen werden." }

        startActivity(Intent(this, LegalViewerActivity::class.java).apply {
            putExtra("title", title)
            putExtra("content", content)
        })
    }
}
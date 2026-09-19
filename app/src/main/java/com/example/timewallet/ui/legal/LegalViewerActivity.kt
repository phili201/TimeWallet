package com.example.timewallet.ui.legal

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.timewallet.databinding.ActivityLegalViewerBinding
import androidx.activity.viewModels
import androidx.lifecycle.observe

class LegalViewerActivity : ComponentActivity() {

    private lateinit var binding: ActivityLegalViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLegalViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileName = intent.getStringExtra("file") ?: ""
        val title = intent.getStringExtra("title") ?: ""

        binding.legalTitle.text = title

        val text = assets.open("legal/$fileName").bufferedReader().use { it.readText() }
        binding.legalText.text = text

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}

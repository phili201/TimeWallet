package com.example.timewallet.ui.legal

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.timewallet.databinding.ActivityLegalViewerBinding

class LegalViewerActivity : ComponentActivity() {

    private lateinit var binding: ActivityLegalViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = 0xFF0D0D0D.toInt()
        window.statusBarColor = 0xFF0D0D0D.toInt()

        binding = ActivityLegalViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val title = intent.getStringExtra("title") ?: ""
        val content = intent.getStringExtra("content") ?: ""

        binding.titleText.text = title
        binding.contentText.text = content

        binding.backButton.setOnClickListener { finish() }
    }
}

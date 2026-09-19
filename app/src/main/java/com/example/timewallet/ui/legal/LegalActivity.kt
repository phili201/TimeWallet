package com.example.timewallet.ui.legal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.timewallet.databinding.ActivityLegalBinding

class LegalActivity : ComponentActivity() {

    private lateinit var binding: ActivityLegalBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLegalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnHaftung.setOnClickListener {
            openDoc("haftungsausschluss.txt", "Haftungsausschluss")
        }

        binding.btnNutzung.setOnClickListener {
            openDoc("nutzungsbedingungen.txt", "Nutzungsbedingungen")
        }

        binding.btnDatenschutz.setOnClickListener {
            openDoc("datenschutz.txt", "Datenschutzerklärung")
        }

        binding.btnLizenz.setOnClickListener {
            openDoc("LICENSE", "Lizenz")
        }
    }

    private fun openDoc(file: String, title: String) {
        val intent = Intent(this, LegalViewerActivity::class.java)
        intent.putExtra("file", file)
        intent.putExtra("title", title)
        startActivity(intent)
    }
}

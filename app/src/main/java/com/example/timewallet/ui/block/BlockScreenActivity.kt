package com.example.timewallet.ui.block

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import com.example.timewallet.databinding.ActivityBlockScreenBinding
import com.example.timewallet.ui.MainActivity

class BlockScreenActivity : ComponentActivity() {
    companion object {
        const val EXTRA_BLOCKED_PACKAGE = "blockedPackage"
        const val EXTRA_REASON = "reason"
    }

    private lateinit var binding: ActivityBlockScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = Color.rgb(5, 7, 10)
        window.navigationBarColor = Color.rgb(5, 7, 10)

        binding = ActivityBlockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pkg = intent.getStringExtra(EXTRA_BLOCKED_PACKAGE).orEmpty()
        val reason = intent.getStringExtra(EXTRA_REASON).orEmpty()

        binding.blockCountdown.text = when (pkg) {
            "com.instagram.android" -> "Instagram ist blockiert"
            "com.google.android.youtube" -> "YouTube ist blockiert"
            else -> "Zugang blockiert"
        }

        binding.blockReason.text = reason.ifBlank { "Produktivität zuerst." }

        binding.backButton.setOnClickListener {
            openWallet()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    openWallet()
                }
            }
        )
    }

    private fun openWallet() {
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
        finishAndRemoveTask()
    }
}

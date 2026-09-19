package com.example.timewallet.ui.block

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.databinding.ActivityBlockScreenBinding
import kotlinx.coroutines.launch
import androidx.activity.viewModels
import androidx.lifecycle.observe

class BlockScreenActivity : ComponentActivity() {

    private lateinit var binding: ActivityBlockScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBlockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repo = (application as TimeWalletApp).repository

        lifecycleScope.launch {
            repo.socialMinutesFlow.collect { m ->
                binding.blockCountdown.text = "Noch $m Minuten"
            }
        }
    }
}

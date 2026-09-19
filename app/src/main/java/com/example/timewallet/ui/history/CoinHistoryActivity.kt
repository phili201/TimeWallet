package com.example.timewallet.ui.history

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.timewallet.databinding.ActivityCoinHistoryBinding
import kotlinx.coroutines.launch

class CoinHistoryActivity : ComponentActivity() {

    private lateinit var binding: ActivityCoinHistoryBinding
    private val viewModel: CoinHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCoinHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            viewModel.history.collect { list ->
                binding.historyText.text = list.joinToString("\n") {
                    "${it.amount} Coins – ${it.reason} (${java.util.Date(it.timestamp)})"
                }
            }
        }
    }
}

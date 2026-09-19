package com.example.timewallet.ui.history

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.databinding.ActivityCoinHistoryBinding
import kotlinx.coroutines.launch

class CoinHistoryActivity : ComponentActivity() {

    private lateinit var binding: ActivityCoinHistoryBinding

    private val viewModel by lazy {
        CoinHistoryViewModel(application as TimeWalletApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCoinHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = CoinHistoryAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.history.collect { list ->
                adapter.submitList(list)
            }
        }
    }
}

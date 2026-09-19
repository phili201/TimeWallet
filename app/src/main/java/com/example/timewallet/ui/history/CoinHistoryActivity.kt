package com.example.timewallet.ui.history

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.databinding.ActivityCoinHistoryBinding
import androidx.lifecycle.observe


class CoinHistoryActivity : ComponentActivity() {

    private lateinit var binding: ActivityCoinHistoryBinding

    private val viewModel: CoinHistoryViewModel by viewModels {
        CoinHistoryViewModelFactory(application as TimeWalletApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCoinHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = CoinHistoryAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.history.collect { list ->
            adapter.submitList(list)
        }
    }
}

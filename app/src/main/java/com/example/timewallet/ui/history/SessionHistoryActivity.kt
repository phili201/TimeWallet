package com.example.timewallet.ui.history

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.databinding.ActivitySessionHistoryBinding

class SessionHistoryActivity : ComponentActivity() {

    private lateinit var binding: ActivitySessionHistoryBinding

    private val viewModel: SessionHistoryViewModel by viewModels {
        SessionHistoryViewModelFactory(application as TimeWalletApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySessionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = SessionHistoryAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        viewModel.history.collect { list ->
            adapter.submitList(list)
        }
    }
}

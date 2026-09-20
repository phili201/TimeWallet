package com.example.timewallet.ui.history

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.databinding.ActivitySessionHistoryBinding
import kotlinx.coroutines.launch

class SessionHistoryActivity : ComponentActivity() {

    private lateinit var binding: ActivitySessionHistoryBinding

    private val viewModel by lazy {
        SessionHistoryViewModel(application as TimeWalletApp)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = 0xFF0D0D0D.toInt()
        window.statusBarColor = 0xFF0D0D0D.toInt()

        binding = ActivitySessionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = SessionHistoryAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            viewModel.history.collect { list ->
                adapter.submitList(list)
            }
        }
    }
}

package com.example.timewallet.ui.history

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.timewallet.databinding.ActivitySessionHistoryBinding
import kotlinx.coroutines.launch

class SessionHistoryActivity : ComponentActivity() {

    private lateinit var binding: ActivitySessionHistoryBinding
    private val viewModel: SessionHistoryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySessionHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            viewModel.sessions.collect { list ->
                binding.sessionList.text = list.joinToString("\n") {
                    "⏱ ${it.minutes} min | Score ${it.score} | ${if (it.valid) "✔" else "❌"} | ${java.util.Date(it.timestamp)}"
                }
            }
        }
    }
}

package com.example.timewallet.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.example.timewallet.databinding.ActivityTasksBinding

class TasksActivity : ComponentActivity() {
    private lateinit var binding: ActivityTasksBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = 0xFF050608.toInt()
        window.navigationBarColor = 0xFF111318.toInt()

        binding.vocabButton.setOnClickListener { launchTask("Vokabeln lernen", 25) }
        binding.codingButton.setOnClickListener { launchTask("Coden lernen", 45) }
        binding.schoolButton.setOnClickListener { launchTask("Schulaufgaben", 30) }
        binding.readingButton.setOnClickListener { launchTask("Lesen", 25) }
        binding.customButton.setOnClickListener {
            val task = binding.customTaskInput.text.toString().ifBlank { "Eigene Aufgabe" }
            val minutes = binding.customMinutesInput.text.toString().toIntOrNull()?.coerceIn(1, 180) ?: 25
            launchTask(task, minutes)
        }
    }

    private fun launchTask(task: String, minutes: Int) {
        startActivity(Intent(this, MainActivity::class.java).apply {
            putExtra("task", task)
            putExtra("minutes", minutes)
        })
        finish()
    }
}

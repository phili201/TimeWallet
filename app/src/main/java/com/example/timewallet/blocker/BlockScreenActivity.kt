package com.example.timewallet.blocker

import android.app.Activity
import android.os.Bundle
import com.example.timewallet.databinding.ActivityBlockScreenBinding

class BlockScreenActivity : Activity() {

    private lateinit var binding: ActivityBlockScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBlockScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }
    }
}

package com.example.timewallet.ui

import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.activity.ComponentActivity
import com.example.timewallet.databinding.ActivitySplashBinding

class SplashActivity : ComponentActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_TimeWallet_Splash)
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.logo.startAnimation(
            AnimationUtils.loadAnimation(this, R.anim.fade_in)
        )

        binding.root.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 1500)
    }
}

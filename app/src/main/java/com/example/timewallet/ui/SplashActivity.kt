package com.example.timewallet.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.timewallet.R
import com.example.timewallet.service.SocialBlockerService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {

    private var openedAccessibilitySettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.navigationBarColor = 0xFF0D0D0D.toInt()
        window.statusBarColor = 0xFF0D0D0D.toInt()
        setContentView(R.layout.activity_splash)

        val logo = findViewById<ImageView>(R.id.logo)
        logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))
        logo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_up))

        lifecycleScope.launch {
            delay(1200)
            continueAfterSplash()
        }
    }

    override fun onResume() {
        super.onResume()
        if (openedAccessibilitySettings) {
            openedAccessibilitySettings = false
            continueAfterSplash()
        }
    }

    private fun continueAfterSplash() {
        if (!isAccessibilityServiceEnabled()) {
            openedAccessibilitySettings = true
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            return
        }
        openMain()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = ComponentName(this, SocialBlockerService::class.java)
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false

        return enabled.split(':').any { entry ->
            ComponentName.unflattenFromString(entry) == expected
        }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

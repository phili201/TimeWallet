package com.example.timewallet.ui

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.example.timewallet.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : ComponentActivity() {

    private var openedPermissionSettings = false

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
        if (openedPermissionSettings) {
            openedPermissionSettings = false
            continueAfterSplash()
        }
    }

    private fun continueAfterSplash() {
        when {
            !Settings.canDrawOverlays(this) -> {
                openedPermissionSettings = true
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    data = Uri.parse("package:$packageName")
                })
            }
            !isAccessibilityServiceEnabled() -> {
                openedPermissionSettings = true
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
            else -> openMain()
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val expected = ComponentName(this, com.example.timewallet.service.SocialBlockerService::class.java)
        val enabled = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabled.split(':').any { ComponentName.unflattenFromString(it) == expected }
    }

    private fun openMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

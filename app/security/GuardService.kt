package com.example.timewallet.security

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder

class GuardService : Service() {

    override fun onCreate() {
        super.onCreate()

        val channelId = "timewallet_guard"
        val channel = NotificationChannel(
            channelId,
            "TimeWallet Schutz",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = Notification.Builder(this, channelId)
            .setContentTitle("TimeWallet aktiv")
            .setContentText("Schutz läuft")
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .build()

        startForeground(1, notification)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}

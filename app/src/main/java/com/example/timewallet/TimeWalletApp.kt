package com.example.timewallet

import android.app.Application
import com.example.timewallet.data.TimeWalletRepository

class TimeWalletApp : Application() {
    lateinit var repository: TimeWalletRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = TimeWalletRepository(this)
    }
}

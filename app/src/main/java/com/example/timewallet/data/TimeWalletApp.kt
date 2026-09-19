package com.example.timewallet

import android.app.Application
import androidx.room.Room
import com.example.timewallet.data.AppDatabase

class TimeWalletApp : Application() {

    lateinit var database: AppDatabase
        private set

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "timewallet.db"
        ).build()
    }
}

package com.example.timewallet

import android.app.Application
import androidx.room.Room
import com.example.timewallet.data.TimeWalletRepository
import com.example.timewallet.data.db.AppDatabase

class TimeWalletApp : Application() {

    lateinit var database: AppDatabase
    lateinit var repository: TimeWalletRepository

    override fun onCreate() {
        super.onCreate()

        database = Room.databaseBuilder(
            this,
            AppDatabase::class.java,
            "timewallet.db"
        ).build()

        repository = TimeWalletRepository(
            database.coinDao(),
            database.sessionDao()
        )
    }
}
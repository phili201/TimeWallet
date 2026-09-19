package com.example.timewallet

import android.app.Application
import com.example.timewallet.data.AppDatabase
import com.example.timewallet.data.Repository

class TimeWalletApp : Application() {

    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.getInstance(this)
        repository = Repository(db.walletDao(), db.sessionDao(), db.socialUnlockDao())
    }
}

package com.example.timewallet

import android.app.Application
import com.example.timewallet.data.SupabaseClient
import com.example.timewallet.data.TimeWalletRepository
import com.example.timewallet.security.SecurityManager

class TimeWalletApp : Application() {
    lateinit var repository: TimeWalletRepository
        private set
    lateinit var supabase: SupabaseClient
        private set
    lateinit var security: SecurityManager
        private set

    override fun onCreate() {
        super.onCreate()
        repository = TimeWalletRepository(this)
        supabase = SupabaseClient(this)
        security = SecurityManager(this)
    }
}

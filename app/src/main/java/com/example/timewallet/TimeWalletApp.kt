package com.example.timewallet

import android.app.Application
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.timewallet.data.TimeWalletRepository
import com.example.timewallet.data.db.AppDatabase

class TimeWalletApp : Application() {
    lateinit var database: AppDatabase
    lateinit var repository: TimeWalletRepository

    override fun onCreate() {
        super.onCreate()
        val migration1to2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE sessions ADD COLUMN task TEXT NOT NULL DEFAULT ''")
            }
        }
        database = Room.databaseBuilder(this, AppDatabase::class.java, "timewallet.db")
            .addMigrations(migration1to2)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
        repository = TimeWalletRepository(this, database.coinDao(), database.sessionDao())
    }
}

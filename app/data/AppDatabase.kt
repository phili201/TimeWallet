package com.example.timewallet.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Wallet::class, Session::class, SocialUnlock::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun walletDao(): WalletDao
    abstract fun sessionDao(): SessionDao
    abstract fun socialUnlockDao(): SocialUnlockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "time_wallet_db"
                ).build()
                INSTANCE = inst
                inst
            }
        }
    }
}

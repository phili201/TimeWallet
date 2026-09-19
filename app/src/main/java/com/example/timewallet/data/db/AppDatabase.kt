package com.example.timewallet.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.timewallet.data.coins.CoinDao
import com.example.timewallet.data.coins.CoinEntry
import com.example.timewallet.data.session.SessionDao
import com.example.timewallet.data.session.SessionEntry

@Database(
    entities = [
        CoinEntry::class,
        SessionEntry::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun coinDao(): CoinDao
    abstract fun sessionDao(): SessionDao
}

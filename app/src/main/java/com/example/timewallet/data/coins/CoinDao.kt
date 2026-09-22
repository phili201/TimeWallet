package com.example.timewallet.data.coins

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CoinDao {
    @Insert
    suspend fun insert(entry: CoinEntry)

    @Query("SELECT * FROM coins ORDER BY timestamp DESC")
    fun getHistory(): Flow<List<CoinEntry>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM coins")
    fun getBalance(): Flow<Int>
}

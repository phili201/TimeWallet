package com.example.timewallet.data

import androidx.room.*

@Dao
interface CoinDao {

    @Query("SELECT * FROM coin_balance WHERE id = 0 LIMIT 1")
    suspend fun getBalance(): CoinBalance?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBalance(balance: CoinBalance)

    @Update
    suspend fun updateBalance(balance: CoinBalance)
}

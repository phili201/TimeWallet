package com.example.timewallet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coin_balance")
data class CoinBalance(
    @PrimaryKey val id: Int = 0,
    val coins: Int
)

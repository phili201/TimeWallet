package com.example.timewallet.data.coins

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "coins")
data class CoinEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val amount: Int,
    val reason: String,
    val timestamp: Long
)
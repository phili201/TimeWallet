package com.example.timewallet.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet")
data class Wallet(
    @PrimaryKey val id: Int = 0,
    val coins: Int
)

@Entity(tableName = "session")
data class Session(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val taskType: String,
    val durationMinutes: Int,
    val status: String, // "running", "verified", "rejected"
    val aiScore: Float?
)

@Entity(tableName = "social_unlock")
data class SocialUnlock(
    @PrimaryKey val id: Int = 0,
    val unlockUntilMillis: Long
)

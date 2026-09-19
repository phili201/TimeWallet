package com.example.timewallet.data.session

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessions")
data class SessionEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val minutes: Int,
    val score: Int,
    val valid: Boolean,
    val timestamp: Long
)
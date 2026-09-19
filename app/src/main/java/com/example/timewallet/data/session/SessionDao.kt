package com.example.timewallet.data.session

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(entry: SessionEntry)

    @Query("SELECT * FROM sessions ORDER BY timestamp DESC")
    fun getSessions(): Flow<List<SessionEntry>>
}

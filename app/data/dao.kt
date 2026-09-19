package com.example.timewallet.data

import androidx.room.*

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet WHERE id = 0")
    suspend fun getWallet(): Wallet?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWallet(wallet: Wallet)
}

@Dao
interface SessionDao {
    @Insert
    suspend fun insertSession(session: Session): Long

    @Query("SELECT * FROM session ORDER BY id DESC")
    suspend fun getAllSessions(): List<Session>
}

@Dao
interface SocialUnlockDao {
    @Query("SELECT * FROM social_unlock WHERE id = 0")
    suspend fun getUnlock(): SocialUnlock?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUnlock(unlock: SocialUnlock)
}

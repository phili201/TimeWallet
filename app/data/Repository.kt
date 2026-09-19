package com.example.timewallet.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class Repository(
    private val walletDao: WalletDao,
    private val sessionDao: SessionDao,
    private val socialUnlockDao: SocialUnlockDao
) {

    suspend fun getCoins(): Int = withContext(Dispatchers.IO) {
        walletDao.getWallet()?.coins ?: 0
    }

    suspend fun addCoins(delta: Int) = withContext(Dispatchers.IO) {
        val current = walletDao.getWallet()?.coins ?: 0
        walletDao.saveWallet(Wallet(id = 0, coins = current + delta))
    }

    suspend fun createSession(taskType: String, durationMinutes: Int): Long =
        withContext(Dispatchers.IO) {
            sessionDao.insertSession(
                Session(
                    taskType = taskType,
                    durationMinutes = durationMinutes,
                    status = "running",
                    aiScore = null
                )
            )
        }

    suspend fun finishSessionVerified(sessionId: Long, aiScore: Float, earnedCoins: Int) =
        withContext(Dispatchers.IO) {
            // hier vereinfachte Variante: neu speichern statt updaten
            addCoins(earnedCoins)
        }

    suspend fun setSocialUnlock(minutes: Int) = withContext(Dispatchers.IO) {
        val unlockUntil = System.currentTimeMillis() + minutes * 60_000L
        socialUnlockDao.saveUnlock(SocialUnlock(id = 0, unlockUntilMillis = unlockUntil))
    }

    suspend fun isSocialUnlockActive(): Boolean = withContext(Dispatchers.IO) {
        val unlock = socialUnlockDao.getUnlock() ?: return@withContext false
        unlock.unlockUntilMillis > System.currentTimeMillis()
    }
}

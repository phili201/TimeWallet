package com.example.timewallet.data

import android.content.Context
import com.example.timewallet.data.coins.CoinDao
import com.example.timewallet.data.coins.CoinEntry
import com.example.timewallet.data.session.SessionDao
import com.example.timewallet.data.session.SessionEntry
import com.example.timewallet.data.social.SocialAccessStore
import kotlinx.coroutines.flow.Flow

class TimeWalletRepository(
    context: Context,
    private val coinDao: CoinDao,
    private val sessionDao: SessionDao
) {
    private val socialStore = SocialAccessStore(context.applicationContext)

    fun getCoinHistory(): Flow<List<CoinEntry>> = coinDao.getHistory()
    fun getCoinBalance(): Flow<Int> = coinDao.getBalance()
    fun getSessionHistory(): Flow<List<SessionEntry>> = sessionDao.getSessions()
    suspend fun insertCoin(entry: CoinEntry) = coinDao.insert(entry)
    suspend fun insertSession(entry: SessionEntry) = sessionDao.insert(entry)

    fun startProductivitySession() = socialStore.setProductivitySessionRunning(true)
    fun endProductivitySession() = socialStore.setProductivitySessionRunning(false)
    fun setAntiAddictionMode(enabled: Boolean) = socialStore.setAntiAddictionMode(enabled)
    fun isProductivitySessionRunning(): Boolean = socialStore.isProductivitySessionRunning()
    fun getSocialRemainingMinutes(): Int = socialStore.remainingMinutes()

    fun isSocialAllowed(): Boolean {
        socialStore.consumeExpired()
        if (socialStore.isProductivitySessionRunning()) return false
        return socialStore.purchasedUntil() > System.currentTimeMillis()
    }

    suspend fun purchaseSocialTime(minutes: Int, coinCost: Int): Boolean {
        if (minutes <= 0 || coinCost <= 0) return false
        if (coinDao.getBalanceOnce() < coinCost) return false
        coinDao.insert(CoinEntry(-coinCost, "Social-Zeit gekauft", System.currentTimeMillis()))
        socialStore.purchaseMinutes(minutes)
        return true
    }

    fun isAppAlive(): Boolean = true
}

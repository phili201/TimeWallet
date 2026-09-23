package com.example.timewallet.data

import android.content.Context
import com.example.timewallet.data.coins.CoinDao
import com.example.timewallet.data.coins.CoinEntry
import com.example.timewallet.data.emergency.EmergencySwitchStore
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
    private val emergencyStore = EmergencySwitchStore(context.applicationContext)

    fun getCoinHistory(): Flow<List<CoinEntry>> = coinDao.getHistory()
    fun getCoinBalance(): Flow<Int> = coinDao.getBalance()
    suspend fun getCoinBalanceSnapshot(): Int = coinDao.getBalanceOnce()
    fun getSessionHistory(): Flow<List<SessionEntry>> = sessionDao.getSessions()
    suspend fun insertCoin(entry: CoinEntry) = coinDao.insert(entry)
    suspend fun insertSession(entry: SessionEntry) = sessionDao.insert(entry)

    fun startProductivitySession() = socialStore.setProductivitySessionRunning(true)
    fun endProductivitySession() = socialStore.setProductivitySessionRunning(false)
    fun setAntiAddictionMode(enabled: Boolean) = socialStore.setAntiAddictionMode(enabled)
    fun isAntiAddictionModeEnabled(): Boolean = socialStore.isAntiAddictionMode()
    fun isProductivitySessionRunning(): Boolean = socialStore.isProductivitySessionRunning()
    fun getSocialRemainingMinutes(): Int = socialStore.remainingMinutes()
    fun startSocialUse() = socialStore.startSocialUse()
    fun stopSocialUse() = socialStore.stopSocialUse()

    fun isEmergencySwitchEnabled(): Boolean = emergencyStore.isEnabled()
    fun setEmergencySwitchEnabled(enabled: Boolean) = emergencyStore.setEnabled(enabled)

    fun isSocialAllowed(): Boolean {
        if (emergencyStore.isEnabled()) return true
        if (socialStore.isProductivitySessionRunning()) return false
        return socialStore.purchasedRemainingMs() > 0L
    }

    suspend fun purchaseSocialTime(minutes: Int, coinCost: Int): Boolean {
        if (minutes <= 0 || coinCost <= 0) return false
        if (socialStore.isAntiAddictionMode() &&
            socialStore.purchasedRemainingMs() + minutes * 60_000L > 30 * 60_000L
        ) {
            return false
        }

        if (coinDao.getBalanceOnce() < coinCost) return false
        if (!socialStore.purchaseMinutes(minutes)) return false

        return try {
            coinDao.insert(
                CoinEntry(
                    amount = -coinCost,
                    reason = "Social-Zeit gekauft",
                    timestamp = System.currentTimeMillis()
                )
            )
            true
        } catch (exception: Exception) {
            socialStore.removePurchasedMinutes(minutes)
            false
        }
    }

    fun isAppAlive(): Boolean = true
}
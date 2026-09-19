package com.example.timewallet.data

import com.example.timewallet.data.coins.CoinDao
import com.example.timewallet.data.coins.CoinEntry
import com.example.timewallet.data.session.SessionDao
import com.example.timewallet.data.session.SessionEntry
import kotlinx.coroutines.flow.Flow

class TimeWalletRepository(
    private val coinDao: CoinDao,
    private val sessionDao: SessionDao
) {

    fun getCoinHistory(): Flow<List<CoinEntry>> = coinDao.getHistory()
    fun getSessionHistory(): Flow<List<SessionEntry>> = sessionDao.getSessions()

    suspend fun insertCoin(entry: CoinEntry) = coinDao.insert(entry)
    suspend fun insertSession(entry: SessionEntry) = sessionDao.insert(entry)

    fun isAppAlive(): Boolean = true
    fun isSocialAllowed(): Boolean = false
}
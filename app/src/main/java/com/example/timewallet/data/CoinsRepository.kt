package com.example.timewallet.data

class CoinsRepository(private val dao: CoinDao) {

    suspend fun getBalance(): Int {
        return dao.getBalance()?.coins ?: 0
    }

    suspend fun addCoins(amount: Int) {
        val current = getBalance()
        dao.insertBalance(CoinBalance(coins = current + amount))
    }

    suspend fun removeCoins(amount: Int): Boolean {
        val current = getBalance()
        if (current < amount) return false
        dao.insertBalance(CoinBalance(coins = current - amount))
        return true
    }
}

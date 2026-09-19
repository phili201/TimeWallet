package com.example.timewallet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.data.TimeWalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoinsViewModel(app: TimeWalletApp) : ViewModel() {

    private val repo: TimeWalletRepository = app.repository

    private val _coins = MutableStateFlow(0)
    val coins: StateFlow<Int> = _coins

    init {
        loadCoins()
    }

    private fun loadCoins() {
        viewModelScope.launch {
            val history = repo.getCoinHistory()
            history.collect { list ->
                _coins.value = list.sumOf { it.amount }
            }
        }
    }

    fun addCoins(amount: Int) {
        viewModelScope.launch {
            repo.insertCoin(
                com.example.timewallet.data.coins.CoinEntry(
                    amount = amount,
                    reason = "Manuell hinzugefügt",
                    timestamp = System.currentTimeMillis()
                )
            )
            loadCoins()
        }
    }

    fun removeCoins(amount: Int): Boolean {
        var success = false
        viewModelScope.launch {
            val current = _coins.value
            if (current >= amount) {
                repo.insertCoin(
                    com.example.timewallet.data.coins.CoinEntry(
                        amount = -amount,
                        reason = "Manuell entfernt",
                        timestamp = System.currentTimeMillis()
                    )
                )
                success = true
            }
            loadCoins()
        }
        return success
    }
}

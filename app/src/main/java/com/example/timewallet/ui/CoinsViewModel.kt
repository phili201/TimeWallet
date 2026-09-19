package com.example.timewallet.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.timewallet.TimeWalletApp
import com.example.timewallet.data.CoinsRepository

class CoinsViewModel(app: TimeWalletApp) : ViewModel() {

    private val repo = CoinsRepository(app.database.coinDao())

    private val _coins = MutableStateFlow(0)
    val coins: StateFlow<Int> = _coins

    init {
        loadCoins()
    }

    private fun loadCoins() {
        viewModelScope.launch {
            _coins.value = repo.getBalance()
        }
    }

    fun addCoins(amount: Int) {
        viewModelScope.launch {
            repo.addCoins(amount)
            loadCoins()
        }
    }

    fun removeCoins(amount: Int): Boolean {
        var success = false
        viewModelScope.launch {
            success = repo.removeCoins(amount)
            loadCoins()
        }
        return success
    }
}

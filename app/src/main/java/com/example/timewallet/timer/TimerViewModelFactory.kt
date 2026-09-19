package com.example.timewallet.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.timewallet.TimeWalletApp

class TimerViewModelFactory(
    private val app: TimeWalletApp
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TimerViewModel(app) as T
    }
}

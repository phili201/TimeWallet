package com.example.timewallet.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.timewallet.TimeWalletApp

class SessionHistoryViewModelFactory(
    private val app: TimeWalletApp
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SessionHistoryViewModel(app) as T
    }
}
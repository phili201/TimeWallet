package com.example.timewallet.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewallet.TimeWalletApp
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn

class SessionHistoryViewModel(app: TimeWalletApp) : ViewModel() {

    private val repo = app.repository

    val history = repo.getSessionHistory()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

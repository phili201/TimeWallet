package com.example.timewallet.ui.history

import androidx.lifecycle.ViewModel
import com.example.timewallet.TimeWalletApp
import kotlinx.coroutines.flow.Flow

class SessionHistoryViewModel : ViewModel() {
    private val repo = TimeWalletApp.instance.repository
    val sessions: Flow<List<com.example.timewallet.data.session.SessionEntry>> = repo.getSessions()
}

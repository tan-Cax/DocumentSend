package com.example.documentsend.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.documentsend.data.AppDatabase
import com.example.documentsend.data.History
import com.example.documentsend.repository.HistoryRepository
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) :
    AndroidViewModel(application) {
    private val historyDao = AppDatabase.getDatabase(application).historyDao()
    private val repository = HistoryRepository(historyDao)

    var historyList by mutableStateOf<List<History>>(emptyList())
        private set

    fun loadHistory() {
        viewModelScope.launch {
            repository.getAllHistory().collect { historyList = it }
        }
    }

    suspend fun insertHistoryItem(item: History) =
        repository.insertHistory(item)

    fun clearHistory() {
        viewModelScope.launch {
            repository.deleteAllHistory()
        }
    }
}

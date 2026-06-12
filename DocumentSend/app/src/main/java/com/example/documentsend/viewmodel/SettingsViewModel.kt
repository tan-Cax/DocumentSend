package com.example.documentsend.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.documentsend.data.SettingsState
import com.example.documentsend.repository.SettingsRepository
import com.example.documentsend.repository.dataStore
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) :
    AndroidViewModel(application) {

    private val settingsRepository = SettingsRepository(application.dataStore)

    var settingsState by mutableStateOf(SettingsState())
        private set

    init {
        viewModelScope.launch {
            settingsRepository.userNameFlow.collect { userName ->
                settingsState = settingsState.copy(userName = userName)
            }
        }
        viewModelScope.launch {
            settingsRepository.themeModeFlow.collect { mode ->
                settingsState = settingsState.copy(themeMode = mode)
            }
        }
        viewModelScope.launch {
            settingsRepository.autoSaveFlow.collect { enabled ->
                settingsState = settingsState.copy(autoSave = enabled)
            }
        }
        viewModelScope.launch {
            settingsRepository.colorSchemeFlow.collect { scheme ->
                settingsState = settingsState.copy(colorScheme = scheme)
            }
        }
        viewModelScope.launch {
            settingsRepository.saveToHistoryFlow.collect { enabled ->
                settingsState = settingsState.copy(saveToHistory = enabled)
            }
        }
        viewModelScope.launch {
            settingsRepository.sendPortFlow.collect { port ->
                settingsState = settingsState.copy(sendPort = port)
            }
        }
        viewModelScope.launch {
            settingsRepository.receivePortFlow.collect { port ->
                settingsState = settingsState.copy(receivePort = port)
            }
        }
        viewModelScope.launch {
            settingsRepository.savePathFlow.collect { path ->
                settingsState = settingsState.copy(savePath = path)
            }
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            settingsRepository.setUserName(name)
        }
    }

    fun updateThemeMode(mode: Int) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    fun updateAutoSave(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setAutoSave(enabled)
        }
    }

    fun updateColorScheme(scheme: String) {
        viewModelScope.launch {
            settingsRepository.setColorScheme(scheme)
        }
    }

    fun updateSaveToHistory(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setSaveToHistory(enabled)
        }
    }

    fun updateSendPort(port: Int) {
        viewModelScope.launch {
            settingsRepository.setSendPort(port)
        }
    }

    fun updateReceivePort(port: Int) {
        viewModelScope.launch {
            settingsRepository.setReceivePort(port)
        }
    }

    fun updateSavePath(path: String) {
        viewModelScope.launch {
            settingsRepository.setSavePath(path)
        }
    }

    fun resetSavePath() {
        viewModelScope.launch {
            settingsRepository.setSavePath("")
        }
    }
}

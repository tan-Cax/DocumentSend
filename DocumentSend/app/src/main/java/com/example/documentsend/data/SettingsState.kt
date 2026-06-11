package com.example.documentsend.data

data class SettingsState(
    val userName: String = "默认用户",
    val themeMode: Int = 0,       // 0=跟随系统, 1=浅色, 2=深色
    val autoSave: Boolean = false,
    val colorScheme: String = "默认",
    val saveToHistory: Boolean = true
)

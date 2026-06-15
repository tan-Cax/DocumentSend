package com.example.documentsend.data

data class SettingsState(
    val isSettingsLoaded: Boolean = false,  // DataStore 是否已加载完成
    val userName: String = "默认用户",
    val themeMode: Int = 0,           // 0=跟随系统, 1=浅色, 2=深色
    val autoSave: Boolean = false,
    val colorScheme: String = "默认",
    val saveToHistory: Boolean = true,
    val sendPort: Int = 6666,         // 发送端口
    val receivePort: Int = 50000,     // 接收端口
    val savePath: String = "",        // 存储路径（空=默认路径）
    val isFirstLaunch: Int = 1,       // 是否首次启动
    val targetIp: String = ""         // 目标IP地址
)

package com.example.documentsend.utils

import android.content.Context
import android.os.Environment
import com.example.documentsend.repository.SettingsRepository
import com.example.documentsend.repository.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * 网络配置管理工具
 * 统一管理发送端口、接收端口、存储路径的默认值和持久化读取
 */
object NetworkConfigUtils {

    // ==================== 默认值 ====================
    const val DEFAULT_SEND_PORT = 6666
    const val DEFAULT_RECEIVE_PORT = 50000
    private const val DEFAULT_DIR_NAME = "DocumentSend"

    // ==================== 端口 ====================

    /** 获取发送端口（优先读取持久化配置） */
    fun getSendPort(context: Context): Int {
        val repository = SettingsRepository(context.dataStore)
        return runBlocking {
            repository.sendPortFlow.first()
        }
    }

    /** 获取接收端口（优先读取持久化配置） */
    fun getReceivePort(context: Context): Int {
        val repository = SettingsRepository(context.dataStore)
        return runBlocking {
            repository.receivePortFlow.first()
        }
    }

    // ==================== 存储路径 ====================

    /** 获取默认存储路径 */
    fun getDefaultSavePath(context: Context): File {
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            DEFAULT_DIR_NAME
        )
    }

    /** 获取当前存储路径（优先自定义路径，默认路径） */
    fun getCurrentSavePath(context: Context): File {
        val repository = SettingsRepository(context.dataStore)
        val customPath = runBlocking {
            repository.savePathFlow.first()
        }
        return if (!customPath.isNullOrEmpty()) {
            File(customPath)
        } else {
            getDefaultSavePath(context)
        }
    }

    /** 获取当前存储路径的显示文本 */
    fun getSavePathDisplay(context: Context): String {
        return getCurrentSavePath(context).absolutePath
    }

    /** 是否有自定义存储路径 */
    fun hasCustomSavePath(context: Context): Boolean {
        val repository = SettingsRepository(context.dataStore)
        val customPath = runBlocking {
            repository.savePathFlow.first()
        }
        return !customPath.isNullOrEmpty()
    }
}

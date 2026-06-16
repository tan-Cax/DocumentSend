package com.example.documentsend.utils

import android.content.Context
import android.os.Environment
import com.example.documentsend.repository.SettingsRepository
import com.example.documentsend.repository.dataStore
import kotlinx.coroutines.flow.first
import java.io.File

object NetworkConfigUtils {

    const val DEFAULT_SEND_PORT = 6666
    const val DEFAULT_RECEIVE_PORT = 50000
    private const val DEFAULT_DIR_NAME = "DocumentSend"

    suspend fun getSendPort(context: Context): Int {
        val repository = SettingsRepository(context.dataStore)
        return repository.sendPortFlow.first()
    }

    suspend fun getReceivePort(context: Context): Int {
        val repository = SettingsRepository(context.dataStore)
        return repository.receivePortFlow.first()
    }

    fun getDefaultSavePath(context: Context): File {
        return File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            DEFAULT_DIR_NAME
        )
    }

    suspend fun getCurrentSavePath(context: Context): File {
        val repository = SettingsRepository(context.dataStore)
        val customPath = repository.savePathFlow.first()
        return if (!customPath.isNullOrEmpty()) {
            File(customPath)
        } else {
            getDefaultSavePath(context)
        }
    }

    suspend fun getSavePathDisplay(context: Context): String {
        return getCurrentSavePath(context).absolutePath
    }

    suspend fun hasCustomSavePath(context: Context): Boolean {
        val repository = SettingsRepository(context.dataStore)
        val customPath = repository.savePathFlow.first()
        return !customPath.isNullOrEmpty()
    }
}

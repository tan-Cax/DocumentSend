package com.example.documentsend.utils

import android.content.Context
import com.example.documentsend.repository.SettingsRepository
import com.example.documentsend.repository.dataStore
import kotlinx.coroutines.flow.first
import java.util.UUID

object DeviceUuidUtils {

    suspend fun getOrCreate(context: Context): String {
        val repository = SettingsRepository(context.dataStore)
        val saved = repository.deviceUuidFlow.first()
        return if (saved.isNotEmpty()) {
            saved
        } else {
            val uuid = UUID.randomUUID().toString()
            repository.setDeviceUuid(uuid)
            uuid
        }
    }
}

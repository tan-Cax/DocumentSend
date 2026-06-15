package com.example.documentsend.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore实例唯一
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    companion object {
        val THEME_MODE_KEY = intPreferencesKey("theme_mode")
        val USER_NAME_KEY = stringPreferencesKey("user_name")
        val IS_FRIST_LAUNCH_KEY = intPreferencesKey("is_first_launch")
        val AUTO_SAVE_KEY = booleanPreferencesKey("auto_save")
        val COLOR_SCHEME_KEY = stringPreferencesKey("color_scheme")
        val SAVE_TO_HISTORY_KEY = booleanPreferencesKey("save_to_history")
        val SEND_PORT_KEY = intPreferencesKey("send_port")
        val RECEIVE_PORT_KEY = intPreferencesKey("receive_port")
        val SAVE_PATH_KEY = stringPreferencesKey("save_path")
        val TARGET_IP_KEY = stringPreferencesKey("target_ip")
    }

    val themeModeFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: 0
    }

    val userNameFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY] ?: "默认用户"
    }

    val isFirstLaunchFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[IS_FRIST_LAUNCH_KEY] ?: 1
    }

    val autoSaveFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[AUTO_SAVE_KEY] ?: false
    }

    val colorSchemeFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[COLOR_SCHEME_KEY] ?: "默认"
    }

    val saveToHistoryFlow: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[SAVE_TO_HISTORY_KEY] ?: true
    }

    val sendPortFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[SEND_PORT_KEY] ?: 6666
    }

    val receivePortFlow: Flow<Int> = dataStore.data.map { preferences ->
        preferences[RECEIVE_PORT_KEY] ?: 50000
    }

    val savePathFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[SAVE_PATH_KEY] ?: ""
    }

    val targetIpFlow: Flow<String> = dataStore.data.map { preferences ->
        preferences[TARGET_IP_KEY] ?: ""
    }

    suspend fun setThemeMode(mode: Int) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }

    suspend fun setUserName(name: String) {
        dataStore.edit { preferences ->
            preferences[USER_NAME_KEY] = name
        }
    }

    suspend fun setIsFirstLaunch(isFirst: Int) {
        dataStore.edit { preferences ->
            preferences[IS_FRIST_LAUNCH_KEY] = isFirst
        }
    }

    suspend fun setAutoSave(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_SAVE_KEY] = enabled
        }
    }

    suspend fun setColorScheme(scheme: String) {
        dataStore.edit { preferences ->
            preferences[COLOR_SCHEME_KEY] = scheme
        }
    }

    suspend fun setSaveToHistory(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[SAVE_TO_HISTORY_KEY] = enabled
        }
    }

    suspend fun setSendPort(port: Int) {
        dataStore.edit { preferences ->
            preferences[SEND_PORT_KEY] = port
        }
    }

    suspend fun setReceivePort(port: Int) {
        dataStore.edit { preferences ->
            preferences[RECEIVE_PORT_KEY] = port
        }
    }

    suspend fun setSavePath(path: String) {
        dataStore.edit { preferences ->
            preferences[SAVE_PATH_KEY] = path
        }
    }

    suspend fun setTargetIp(ip: String) {
        dataStore.edit { preferences ->
            preferences[TARGET_IP_KEY] = ip
        }
    }
}

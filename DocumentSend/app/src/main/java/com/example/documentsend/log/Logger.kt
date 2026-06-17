package com.example.documentsend.log

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {

    var isDebug = true
    private const val TAG = "DocumentSend"
    private const val MAX_LOG_ENTRIES = 1000
    private const val RETENTION_DAYS = 7

    private val logs = mutableListOf<String>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())
    private val fileDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private var logDir: File? = null
    private var logFile: File? = null

    fun init(context: Context) {
        logDir = File(context.filesDir, "log").also { it.mkdirs() }
        cleanOldLogs()
        synchronized(logs) { logs.clear() }
        log("I", TAG, "日志系统初始化完成")
    }

    fun d(message: String) {
        if (!isDebug) return
        log("D", message)
    }

    fun d(tag: String, message: String) {
        if (!isDebug) return
        log("D", tag, message)
    }

    fun i(message: String) {
        log("I", message)
    }

    fun i(tag: String, message: String) {
        log("I", tag, message)
    }

    fun w(message: String) {
        log("W", message)
    }

    fun w(tag: String, message: String) {
        log("W", tag, message)
    }

    fun e(message: String, throwable: Throwable? = null) {
        log("E", message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log("E", tag, message, throwable)
    }

    fun logDebug(module: String, event: String, detail: String) {
        log("D", module, "[$event] $detail")
    }

    fun logInfo(module: String, event: String, detail: String) {
        log("I", module, "[$event] $detail")
    }

    fun logWarn(module: String, event: String, detail: String) {
        log("W", module, "[$event] $detail")
    }

    fun logError(module: String, event: String, exception: Throwable) {
        log("E", module, "[$event] Failed", exception)
    }

    private fun log(level: String, message: String, throwable: Throwable? = null) {
        log(level, TAG, message, throwable)
    }

    private fun log(level: String, tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val logEntry = if (throwable != null) {
            "$timestamp [$level] $tag: $message\n${throwable.stackTraceToString()}"
        } else {
            "$timestamp [$level] $tag: $message"
        }

        if (isDebug) {
            when (level) {
                "D" -> Log.d(tag, message, throwable)
                "I" -> Log.i(tag, message, throwable)
                "W" -> Log.w(tag, message, throwable)
                "E" -> Log.e(tag, message, throwable)
            }
        }

        synchronized(logs) {
            logs.add(logEntry)
            if (logs.size > MAX_LOG_ENTRIES) {
                logs.removeAt(0)
            }
        }
    }

    fun getLogs(): List<String> {
        synchronized(logs) {
            return logs.toList()
        }
    }

    fun getLogsAsString(): String {
        synchronized(logs) {
            return logs.joinToString("\n")
        }
    }

    fun saveToFile(): Boolean {
        return try {
            val file = logFile ?: run {
                val dir = logDir ?: return false
                val dateStr = fileDateFormat.format(Date())
                File(dir, "app_$dateStr.log")
            }
            synchronized(logs) {
                file.appendText(logs.joinToString("\n") + "\n")
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "保存日志失败", e)
            false
        }
    }

    fun loadFromFile(): List<String> {
        val file = logFile ?: return emptyList()
        if (!file.exists()) return emptyList()
        return file.readLines()
    }

    fun cleanOldLogs() {
        val dir = logDir ?: return
        val cutoff = System.currentTimeMillis() - RETENTION_DAYS * 24 * 60 * 60 * 1000L
        val cutoffDateStr = fileDateFormat.format(Date(cutoff))

        dir.listFiles()?.forEach { file ->
            val name = file.name
            if (name.startsWith("app_") && name.endsWith(".log")) {
                val fileDateStr = name.removePrefix("app_").removeSuffix(".log")
                if (fileDateStr < cutoffDateStr) {
                    file.delete()
                }
            }
        }
    }

    fun clear() {
        synchronized(logs) { logs.clear() }
        logDir?.listFiles()?.forEach { it.delete() }
    }
}

package com.example.documentsend.log

import android.content.Context
import android.util.Log
//import com.example.documentsend.BuildConfig
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Logger {

    //由于无法找到用于检测调试模式的方法，这里自定义一个变量用于控制开发和输出环境
    //在开发环境中设置为true，在生产环境中设置为false
    var isDebug = true
    private const val TAG = "DocumentSend"

    private val logs = mutableListOf<String>()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

    private var logFile: File? = null

    /** 初始化日志文件路径，在 Application.onCreate 中调用 */
    fun init(context: Context) {
        logFile = File(context.filesDir, "app.log")
    }

    //基础日志类型用于生成自定义日志
    //后续的结构化日志类型用于生成便于调取和分析的结构化日志

    /** 输出 DEBUG 级别日志，使用默认 TAG */
    fun d(message: String) {
        log("D", message)
    }

    /** 输出 DEBUG 级别日志，使用自定义 TAG */
    fun d(tag: String, message: String) {
        log("D", tag, message)
    }

    /** 输出 INFO 级别日志，使用默认 TAG */
    fun i(message: String) {
        log("I", message)
    }

    /** 输出 INFO 级别日志，使用自定义 TAG */
    fun i(tag: String, message: String) {
        log("I", tag, message)
    }

    /** 输出 WARN 级别日志，使用默认 TAG */
    fun w(message: String) {
        log("W", message)
    }

    /** 输出 WARN 级别日志，使用自定义 TAG */
    fun w(tag: String, message: String) {
        log("W", tag, message)
    }

    /** 输出 ERROR 级别日志，使用默认 TAG，可附带异常堆栈 */
    fun e(message: String, throwable: Throwable? = null) {
        log("E", message, throwable)
    }

    /** 输出 ERROR 级别日志，使用自定义 TAG，可附带异常堆栈 */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        log("E", tag, message, throwable)
    }

    /**
     * 结构化调试日志
     * @param module 模块名 (如: Network, Database)
     * @param event 事件名 (如: Connect, SaveUser)
     * @param detail 详细描述
     */
    fun logDebug(module: String, event: String, detail: String) {
        log("D", module, "[$event] $detail")
    }

    /**
     * 结构化普通日志
     * @param module 模块名
     * @param event 事件名
     * @param detail 详细描述
     */
    fun logInfo(module: String, event: String, detail: String) {
        log("I", module, "[$event] $detail")
    }

    /**
     * 结构化错误日志
     * @param module 模块名
     * @param event 事件名
     * @param exception 异常对象
     */
    fun logError(module: String, event: String, exception: Throwable) {
        log("E", module, "[$event] Failed", exception)
    }

    private fun log(level: String, message: String, throwable: Throwable? = null) {
        log(level, TAG, message, throwable)
    }

    /**
     * 核心日志方法：格式化时间戳和日志内容
     * - debug 模式写入 Logcat
     * - 始终保存到内存列表以便用户查看
     */
    private fun log(level: String, tag: String, message: String, throwable: Throwable? = null) {
        val timestamp = dateFormat.format(Date())
        val logEntry = if (throwable != null) {
            "$timestamp [$level] $tag: $message\n${throwable.stackTraceToString()}"
        } else {
            "$timestamp [$level] $tag: $message"
        }

        // Debug 模式下输出到 Logcat
        if (isDebug) {
            when (level) {
                "D" -> Log.d(tag, message, throwable)
                "I" -> Log.i(tag, message, throwable)
                "W" -> Log.w(tag, message, throwable)
                "E" -> Log.e(tag, message, throwable)
            }
        }

        // 始终保存到内存
        synchronized(logs) {
            logs.add(logEntry)
        }
    }

    /** 获取内存中所有日志（线程安全） */
    fun getLogs(): List<String> {
        synchronized(logs) {
            return logs.toList()
        }
    }

    /** 获取内存中所有日志的纯文本拼接（线程安全） */
    fun getLogsAsString(): String {
        synchronized(logs) {
            return logs.joinToString("\n")
        }
    }

    /** 将内存日志写入文件，返回是否写入成功 */
    fun saveToFile(): Boolean {
        return try {
            val file = logFile ?: return false
            synchronized(logs) {
                file.writeText(logs.joinToString("\n"))
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "保存日志失败", e)
            false
        }
    }

    /** 从文件读取日志到内存 */
    fun loadFromFile(): List<String> {
        return try {
            val file = logFile ?: return emptyList()
            if (file.exists()) {
                file.readLines()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "读取日志失败", e)
            emptyList()
        }
    }

    /** 清空内存日志并删除日志文件 */
    fun clear() {
        synchronized(logs) {
            logs.clear()
        }
        logFile?.delete()
    }

    //定期清理日志文件，保留最近7天的日志
    fun cleanOldLogs() {
        val file = logFile ?: return
        if (file.exists()) {
            val cutoff = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L
            if (file.lastModified() < cutoff) {
                file.delete()
            }
        }
    }
}

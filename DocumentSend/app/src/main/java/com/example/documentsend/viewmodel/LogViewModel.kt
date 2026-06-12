package com.example.documentsend.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.documentsend.log.Logger
import java.util.regex.Pattern

data class LogEntry(
    val timestamp: String,
    val level: String,
    val tag: String,
    val message: String
)

class LogViewModel : ViewModel() {
    private val _logs = mutableStateListOf<LogEntry>()
    val logs: List<LogEntry> get() = _logs

    fun loadLogs() {
        _logs.clear()
        val rawLogs = Logger.getLogs()

        // Log format: yyyy-MM-dd HH:mm:ss.SSS [LEVEL] TAG: MESSAGE
        // We use a regex to parse it.
        // Example: 2026-06-12 10:00:00.000 [I] DocumentSend: Hello World
        val regex = Pattern.compile("""^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}) \[([DIWE])] (.*?): (.*)$""", Pattern.DOTALL)

        rawLogs.forEach { rawLog ->
            val matcher = regex.matcher(rawLog)
            if (matcher.find()) {
                val timestamp = matcher.group(1) ?: ""
                val level = matcher.group(2) ?: ""
                val tag = matcher.group(3) ?: ""
                val message = matcher.group(4) ?: ""
                _logs.add(LogEntry(timestamp, level, tag, message))
            } else {
                // Fallback for logs that don't match (e.g. stack traces)
                // We might want to append these to the previous log entry's message
                if (_logs.isNotEmpty()) {
                    val last = _logs.last()
                    _logs[_logs.size - 1] = last.copy(message = last.message + "\n" + rawLog)
                }
            }
        }
    }
}


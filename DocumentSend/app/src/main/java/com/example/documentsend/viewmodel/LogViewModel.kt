package com.example.documentsend.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateSetOf
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

    val allTags = mutableStateListOf<String>()
    val selectedTags = mutableStateSetOf<String>()

    val filteredLogs: List<LogEntry>
        get() = if (selectedTags.isEmpty()) {
            logs
        } else {
            logs.filter { it.tag in selectedTags }
        }

    fun loadLogs() {
        _logs.clear()
        allTags.clear()
        selectedTags.clear()

        val rawLogs = Logger.getLogs()

        val regex = Pattern.compile("""^(\d{4}-\d{2}-\d{2} \d{2}:\d{2}:\d{2}\.\d{3}) \[([DIWE])] (.*?): (.*)$""", Pattern.DOTALL)

        val tagSet = mutableSetOf<String>()

        rawLogs.forEach { rawLog ->
            val matcher = regex.matcher(rawLog)
            if (matcher.find()) {
                val timestamp = matcher.group(1) ?: ""
                val level = matcher.group(2) ?: ""
                val tag = matcher.group(3) ?: ""
                val message = matcher.group(4) ?: ""
                _logs.add(LogEntry(timestamp, level, tag, message))
                tagSet.add(tag)
            } else {
                if (_logs.isNotEmpty()) {
                    val last = _logs.last()
                    _logs[_logs.size - 1] = last.copy(message = last.message + "\n" + rawLog)
                }
            }
        }

        allTags.addAll(tagSet.sorted())
        selectedTags.addAll(tagSet)
    }

    fun toggleTag(tag: String) {
        if (tag in selectedTags) {
            selectedTags.remove(tag)
        } else {
            selectedTags.add(tag)
        }
    }

    fun selectAllTags() {
        selectedTags.clear()
        selectedTags.addAll(allTags)
    }

    fun clearAllTags() {
        selectedTags.clear()
    }
}

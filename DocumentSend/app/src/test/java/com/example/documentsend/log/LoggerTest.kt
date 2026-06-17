package com.example.documentsend.log

import android.util.Log
import io.mockk.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File

class LoggerTest {

    @Before
    fun setup() {
        Logger.isDebug = false
        Logger.clear()
        mockkStatic(Log::class)
        every { Log.d(any(), any(), any()) } returns 0
        every { Log.i(any(), any(), any()) } returns 0
        every { Log.w(any(), any(), any()) } returns 0
        every { Log.e(any(), any(), any()) } returns 0
        every { Log.e(any(), any<String>(), any()) } returns 0
    }

    @After
    fun cleanup() {
        Logger.clear()
        unmockkStatic(Log::class)
    }

    // ==================== 基础日志函数 ====================

    @Test
    fun `d should add debug log to memory`() {
        Logger.d("debug message")

        val logs = Logger.getLogs()
        assertEquals(1, logs.size)
        assertTrue(logs[0].contains("[D]"))
        assertTrue(logs[0].contains("DocumentSend: debug message"))
    }

    @Test
    fun `d with tag should add debug log with custom tag`() {
        Logger.d("MyTag", "tagged message")

        val logs = Logger.getLogs()
        assertEquals(1, logs.size)
        assertTrue(logs[0].contains("[D]"))
        assertTrue(logs[0].contains("MyTag: tagged message"))
    }

    @Test
    fun `i should add info log to memory`() {
        Logger.i("info message")

        val logs = Logger.getLogs()
        assertEquals(1, logs.size)
        assertTrue(logs[0].contains("[I]"))
        assertTrue(logs[0].contains("info message"))
    }

    @Test
    fun `i with tag should add info log with custom tag`() {
        Logger.i("MyTag", "info tagged")

        val logs = Logger.getLogs()
        assertTrue(logs[0].contains("MyTag: info tagged"))
    }

    @Test
    fun `w should add warn log to memory`() {
        Logger.w("warn message")

        val logs = Logger.getLogs()
        assertEquals(1, logs.size)
        assertTrue(logs[0].contains("[W]"))
        assertTrue(logs[0].contains("warn message"))
    }

    @Test
    fun `w with tag should add warn log with custom tag`() {
        Logger.w("MyTag", "warn tagged")

        val logs = Logger.getLogs()
        assertTrue(logs[0].contains("MyTag: warn tagged"))
    }

    @Test
    fun `e should add error log to memory`() {
        Logger.e("error message")

        val logs = Logger.getLogs()
        assertEquals(1, logs.size)
        assertTrue(logs[0].contains("[E]"))
        assertTrue(logs[0].contains("error message"))
    }

    @Test
    fun `e with throwable should include stack trace`() {
        val exception = RuntimeException("test error")
        Logger.e("error with exception", exception)

        val logs = Logger.getLogs()
        assertEquals(1, logs.size)
        assertTrue(logs[0].contains("[E]"))
        assertTrue(logs[0].contains("error with exception"))
        assertTrue(logs[0].contains("test error"))
    }

    @Test
    fun `e with tag should add error log with custom tag`() {
        Logger.e("MyTag", "error tagged", null)

        val logs = Logger.getLogs()
        assertTrue(logs[0].contains("MyTag: error tagged"))
    }

    // ==================== 结构化日志函数 ====================

    @Test
    fun `logDebug should format as module event detail`() {
        Logger.logDebug("Network", "Connect", "connecting to server")

        val logs = Logger.getLogs()
        assertEquals(1, logs.size)
        assertTrue(logs[0].contains("[D]"))
        assertTrue(logs[0].contains("Network: [Connect] connecting to server"))
    }

    @Test
    fun `logInfo should format as module event detail`() {
        Logger.logInfo("Database", "SaveUser", "user saved")

        val logs = Logger.getLogs()
        assertEquals(1, logs.size)
        assertTrue(logs[0].contains("[I]"))
        assertTrue(logs[0].contains("Database: [SaveUser] user saved"))
    }

    @Test
    fun `logError should format as module event Failed with throwable`() {
        val exception = RuntimeException("db error")
        Logger.logError("Database", "Query", exception)

        val logs = Logger.getLogs()
        assertEquals(1, logs.size)
        assertTrue(logs[0].contains("[E]"))
        assertTrue(logs[0].contains("Database: [Query] Failed"))
        assertTrue(logs[0].contains("db error"))
    }

    // ==================== getLogs / getLogsAsString ====================

    @Test
    fun `getLogs should return all logs`() {
        Logger.d("first")
        Logger.i("second")
        Logger.w("third")

        val logs = Logger.getLogs()
        assertEquals(3, logs.size)
    }

    @Test
    fun `getLogs should return a copy not the original list`() {
        Logger.d("original")
        val logs1 = Logger.getLogs()
        val logs2 = Logger.getLogs()

        assertNotSame(logs1, logs2)
        assertEquals(logs1, logs2)
    }

    @Test
    fun `getLogsAsString should join logs with newline`() {
        Logger.d("first")
        Logger.i("second")

        val result = Logger.getLogsAsString()
        assertTrue(result.contains("first"))
        assertTrue(result.contains("second"))
        assertTrue(result.contains("\n"))
    }

    // ==================== clear ====================

    @Test
    fun `clear should remove all logs`() {
        Logger.d("one")
        Logger.i("two")
        Logger.w("three")

        Logger.clear()

        assertEquals(0, Logger.getLogs().size)
    }

    // ==================== saveToFile / loadFromFile ====================

    @Test
    fun `saveToFile should return false when logFile is null`() {
        Logger.clear()
        val result = Logger.saveToFile()
        assertFalse(result)
    }

    @Test
    fun `saveToFile and loadFromFile should roundtrip`() {
        val tempDir = File(System.getProperty("java.io.tmpdir"), "logger_test_${System.nanoTime()}")
        tempDir.mkdirs()
        val logFile = File(tempDir, "app.log")

        try {
            // Inject logFile via reflection
            val field = Logger::class.java.getDeclaredField("logFile")
            field.isAccessible = true
            field.set(null, logFile as Any)

            Logger.d("saved message")
            Logger.i("another message")

            val saved = Logger.saveToFile()
            assertTrue(saved)
            assertTrue(logFile.exists())

            // Load from file directly without clearing (clear deletes the file)
            val loaded = Logger.loadFromFile()
            assertEquals(2, loaded.size)
            assertTrue(loaded[0].contains("saved message"))
            assertTrue(loaded[1].contains("another message"))
        } finally {
            logFile.delete()
            tempDir.delete()
            val field = Logger::class.java.getDeclaredField("logFile")
            field.isAccessible = true
            field.set(null, null)
        }
    }

    // ==================== isDebug ====================

    @Test
    fun `isDebug should control Logcat output`() {
        Logger.isDebug = true
        Logger.d("debug on")

        verify { Log.d(any(), eq("debug on"), any()) }

        Logger.clear()
        Logger.isDebug = false
        Logger.d("debug off")

        verify(exactly = 0) { Log.d(any(), eq("debug off"), any()) }
    }

    // ==================== timestamp ====================

    @Test
    fun `log entry should contain timestamp`() {
        Logger.d("timestamp test")

        val logs = Logger.getLogs()
        // Format: yyyy-MM-dd HH:mm:ss.SSS
        assertTrue(logs[0].matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}.*")))
    }
}

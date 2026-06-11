package com.example.documentsend.utils

import android.net.Uri
import com.example.documentsend.data.History
import com.example.documentsend.data.HistoryType
import com.example.documentsend.network.PacketType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HistoryUtilsTest {

    @Before
    fun setup() {
        mockkStatic(Uri::class)
    }

    @After
    fun cleanup() {
        unmockkStatic(Uri::class)
    }

    // ==================== createSendRecord ====================

    @Test
    fun `createSendRecord should create history with correct fields`() {
        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://media/1"

        val history = HistoryUtils.createSendRecord(
            fileName = "test.txt",
            uri = uri,
            fileType = PacketType.FILE,
            targetIp = "192.168.1.100",
            totalLength = 1024L,
            offset = 0L
        )

        assertEquals("test.txt", history.name)
        assertEquals("content://media/1", history.uriString)
        assertEquals("FILE", history.filetypeString)
        assertEquals(HistoryType.SEND, history.typeString)
        assertEquals("192.168.1.100", history.targetIp)
        assertEquals(1024L, history.totalLength)
        assertEquals(0L, history.offset)
    }

    @Test
    fun `createSendRecord with offset should set offset correctly`() {
        val uri = mockk<Uri>()
        every { uri.toString() } returns "content://media/2"

        val history = HistoryUtils.createSendRecord(
            fileName = "large.bin",
            uri = uri,
            fileType = PacketType.FILE,
            targetIp = "10.0.0.1",
            totalLength = 5000L,
            offset = 1000L
        )

        assertEquals(1000L, history.offset)
        assertEquals(5000L, history.totalLength)
    }

    // ==================== createTextSendRecoder ====================

    @Test
    fun `createTextSendRecoder should create text history`() {
        val history = HistoryUtils.createTextSendRecoder(
            content = "Hello World",
            targetIp = "192.168.1.1"
        )

        assertEquals("", history.name)
        assertEquals("", history.uriString)
        assertEquals("TEXT", history.filetypeString)
        assertEquals(HistoryType.SEND, history.typeString)
        assertEquals("192.168.1.1", history.targetIp)
        assertEquals("Hello World", history.text)
        assertEquals("Hello World".toByteArray().size.toLong(), history.totalLength)
    }

    @Test
    fun `createTextSendRecoder with empty content should set totalLength to 0`() {
        val history = HistoryUtils.createTextSendRecoder(content = "", targetIp = "1.2.3.4")

        assertEquals(0L, history.totalLength)
    }

    // ==================== createReceiveRecord ====================

    @Test
    fun `createReceiveRecord should create receive history`() {
        val history = HistoryUtils.createReceiveRecord(
            fileName = "photo.jpg",
            savePath = "/storage/emulated/0/Download/photo.jpg",
            senderIp = "192.168.1.200",
            totalLength = 2048L
        )

        assertEquals("photo.jpg", history.name)
        assertEquals("/storage/emulated/0/Download/photo.jpg", history.uriString)
        assertEquals("FILE", history.filetypeString)
        assertEquals(HistoryType.RECEIVE, history.typeString)
        assertEquals("192.168.1.200", history.targetIp)
        assertEquals(2048L, history.totalLength)
    }

    // ==================== getUri ====================

    @Test
    fun `getUri should parse uriString correctly`() {
        val uriString = "content://media/123"
        every { Uri.parse(uriString) } returns mockk<Uri>()

        val history = History(uriString = uriString)
        val result = HistoryUtils.getUri(history)

        assertNotNull(result)
    }

    @Test
    fun `getUri should return null for empty uriString`() {
        val history = History(uriString = "")
        val result = HistoryUtils.getUri(history)

        assertNull(result)
    }

    // ==================== getPacketType ====================

    @Test
    fun `getPacketType should return correct type for valid string`() {
        val history = History(filetypeString = "FILE")
        assertEquals(PacketType.FILE, HistoryUtils.getPacketType(history))
    }

    @Test
    fun `getPacketType should return TEXT for TEXT string`() {
        val history = History(filetypeString = "TEXT")
        assertEquals(PacketType.TEXT, HistoryUtils.getPacketType(history))
    }

    @Test
    fun `getPacketType should return null for invalid string`() {
        val history = History(filetypeString = "INVALID")
        assertNull(HistoryUtils.getPacketType(history))
    }

    @Test
    fun `getPacketType should return null for empty string`() {
        val history = History(filetypeString = "")
        assertNull(HistoryUtils.getPacketType(history))
    }

    // ==================== isSend / isReceive ====================

    @Test
    fun `isSend should return true for send history`() {
        val history = History(typeString = HistoryType.SEND)
        assertTrue(HistoryUtils.isSend(history))
        assertFalse(HistoryUtils.isReceive(history))
    }

    @Test
    fun `isReceive should return true for receive history`() {
        val history = History(typeString = HistoryType.RECEIVE)
        assertTrue(HistoryUtils.isReceive(history))
        assertFalse(HistoryUtils.isSend(history))
    }

    // ==================== isCompleted ====================

    @Test
    fun `isCompleted should return true when offset equals totalLength`() {
        val history = History(offset = 100L, totalLength = 100L)
        assertTrue(HistoryUtils.isCompleted(history))
    }

    @Test
    fun `isCompleted should return true when offset greater than totalLength`() {
        val history = History(offset = 200L, totalLength = 100L)
        assertTrue(HistoryUtils.isCompleted(history))
    }

    @Test
    fun `isCompleted should return false when offset less than totalLength`() {
        val history = History(offset = 50L, totalLength = 100L)
        assertFalse(HistoryUtils.isCompleted(history))
    }

    @Test
    fun `isCompleted should return true when both are zero`() {
        val history = History(offset = 0L, totalLength = 0L)
        assertTrue(HistoryUtils.isCompleted(history))
    }

    // ==================== getProgress ====================

    @Test
    fun `getProgress should return correct ratio`() {
        val history = History(offset = 50L, totalLength = 100L)
        assertEquals(0.5f, HistoryUtils.getProgress(history), 0.001f)
    }

    @Test
    fun `getProgress should return 0 for zero totalLength`() {
        val history = History(offset = 0L, totalLength = 0L)
        assertEquals(0f, HistoryUtils.getProgress(history), 0.001f)
    }

    @Test
    fun `getProgress should return 1 for completed transfer`() {
        val history = History(offset = 100L, totalLength = 100L)
        assertEquals(1.0f, HistoryUtils.getProgress(history), 0.001f)
    }

    @Test
    fun `getProgress should return 0 for empty history`() {
        val history = History()
        assertEquals(0f, HistoryUtils.getProgress(history), 0.001f)
    }

    // ==================== formatTimestamp ====================

    @Test
    fun `formatTimestamp should format date correctly`() {
        val timestamp = 1705312200000L
        val result = HistoryUtils.formatTimestamp(timestamp)

        // Verify format is yyyy-MM-dd HH:mm
        assertTrue(result.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}")))
        assertTrue(result.contains("2024"))
    }
}

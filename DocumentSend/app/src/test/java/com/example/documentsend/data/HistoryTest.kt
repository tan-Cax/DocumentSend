package com.example.documentsend.data

import org.junit.Assert.*
import org.junit.Test

class HistoryTest {

    @Test
    fun `default values should be correct`() {
        val history = History()

        assertEquals(0, history.id)
        assertEquals("", history.name)
        assertEquals("", history.uriString)
        assertEquals("", history.filetypeString)
        assertEquals(HistoryType.SEND, history.typeString)
        assertEquals("", history.targetIp)
        assertEquals(0L, history.offset)
        assertEquals(0L, history.totalLength)
        assertTrue(history.timestamp > 0)
        assertEquals("", history.text)
    }

    @Test
    fun `should create with custom values`() {
        val history = History(
            id = 1,
            name = "test.txt",
            uriString = "content://media/1",
            filetypeString = "FILE",
            typeString = HistoryType.RECEIVE,
            targetIp = "192.168.1.1",
            offset = 500L,
            totalLength = 1000L,
            timestamp = 1234567890L,
            text = "hello"
        )

        assertEquals(1, history.id)
        assertEquals("test.txt", history.name)
        assertEquals("content://media/1", history.uriString)
        assertEquals("FILE", history.filetypeString)
        assertEquals(HistoryType.RECEIVE, history.typeString)
        assertEquals("192.168.1.1", history.targetIp)
        assertEquals(500L, history.offset)
        assertEquals(1000L, history.totalLength)
        assertEquals(1234567890L, history.timestamp)
        assertEquals("hello", history.text)
    }

    @Test
    fun `equals should work correctly`() {
        val h1 = History(id = 1, name = "a.txt")
        val h2 = History(id = 1, name = "a.txt")
        val h3 = History(id = 2, name = "a.txt")

        assertEquals(h1, h2)
        assertNotEquals(h1, h3)
    }

    @Test
    fun `copy should create new instance`() {
        val original = History(id = 1, name = "original.txt")
        val copied = original.copy(name = "copied.txt")

        assertEquals(1, copied.id)
        assertEquals("copied.txt", copied.name)
        assertNotEquals(original, copied)
    }

    @Test
    fun `hashCode should be equal for equal objects`() {
        val h1 = History(id = 1, name = "test.txt")
        val h2 = History(id = 1, name = "test.txt")

        assertEquals(h1.hashCode(), h2.hashCode())
    }

    @Test
    fun `toString should contain all fields`() {
        val history = History(id = 5, name = "demo.txt")
        val str = history.toString()

        assertTrue(str.contains("id=5"))
        assertTrue(str.contains("name=demo.txt"))
    }

    @Test
    fun `timestamp should be auto-generated`() {
        val before = System.currentTimeMillis()
        val history = History()
        val after = System.currentTimeMillis()

        assertTrue(history.timestamp in before..after)
    }
}

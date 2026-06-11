package com.example.documentsend.data

import org.junit.Assert.*
import org.junit.Test

class HistoryTypeTest {

    @Test
    fun `SEND constant should be send`() {
        assertEquals("send", HistoryType.SEND)
    }

    @Test
    fun `RECEIVE constant should be receive`() {
        assertEquals("receive", HistoryType.RECEIVE)
    }

    @Test
    fun `SEND and RECEIVE should be different`() {
        assertNotEquals(HistoryType.SEND, HistoryType.RECEIVE)
    }

    @Test
    fun `constants should not be empty`() {
        assertTrue(HistoryType.SEND.isNotEmpty())
        assertTrue(HistoryType.RECEIVE.isNotEmpty())
    }

    @Test
    fun `constants should be lowercase`() {
        assertEquals(HistoryType.SEND, HistoryType.SEND.lowercase())
        assertEquals(HistoryType.RECEIVE, HistoryType.RECEIVE.lowercase())
    }
}

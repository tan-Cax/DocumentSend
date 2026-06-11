package com.example.documentsend.network

import org.junit.Assert.*
import org.junit.Test

class PacketTypeTest {

    @Test
    fun `TEXT should have value 1`() {
        assertEquals(1.toByte(), PacketType.TEXT.value)
    }

    @Test
    fun `IMAGE should have value 2`() {
        assertEquals(2.toByte(), PacketType.IMAGE.value)
    }

    @Test
    fun `VIDEO should have value 3`() {
        assertEquals(3.toByte(), PacketType.VIDEO.value)
    }

    @Test
    fun `FILE should have value 4`() {
        assertEquals(4.toByte(), PacketType.FILE.value)
    }

    @Test
    fun `ARCHIVE should have value 5`() {
        assertEquals(5.toByte(), PacketType.ARCHIVE.value)
    }

    @Test
    fun `fromValue should return correct PacketType for valid values`() {
        assertEquals(PacketType.TEXT, PacketType.fromValue(1))
        assertEquals(PacketType.IMAGE, PacketType.fromValue(2))
        assertEquals(PacketType.VIDEO, PacketType.fromValue(3))
        assertEquals(PacketType.FILE, PacketType.fromValue(4))
        assertEquals(PacketType.ARCHIVE, PacketType.fromValue(5))
    }

    @Test
    fun `fromValue should return null for invalid value`() {
        assertNull(PacketType.fromValue(0))
        assertNull(PacketType.fromValue(6))
        assertNull(PacketType.fromValue(-1))
        assertNull(PacketType.fromValue(100))
    }

    @Test
    fun `all packet types should have unique values`() {
        val values = PacketType.entries.map { it.value }
        assertEquals(values.size, values.toSet().size)
    }
}

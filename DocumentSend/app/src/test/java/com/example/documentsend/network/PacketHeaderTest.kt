package com.example.documentsend.network

import org.junit.Assert.*
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

class PacketHeaderTest {

    @Test
    fun `MAGIC_NUMBER should be 0xCAFE`() {
        assertEquals(0xCAFE.toShort(), PacketHeader.MAGIC_NUMBER)
    }

    @Test
    fun `HEADER_LENGTH should be 29`() {
        assertEquals(29, PacketHeader.HEADER_LENGTH)
    }

    @Test
    fun `writeTo and readFrom should roundtrip correctly`() {
        val header = PacketHeader(
            type = PacketType.FILE.value,
            nameLength = 10,
            bodyLength = 1024L,
            offset = 0L,
            totalLength = 1024L
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        header.writeTo(dos)

        val bais = ByteArrayInputStream(baos.toByteArray())
        val dis = DataInputStream(bais)
        val readHeader = PacketHeader.readFrom(dis)

        assertEquals(header.type, readHeader.type)
        assertEquals(header.nameLength, readHeader.nameLength)
        assertEquals(header.bodyLength, readHeader.bodyLength)
        assertEquals(header.offset, readHeader.offset)
        assertEquals(header.totalLength, readHeader.totalLength)
    }

    @Test
    fun `writeTo should write exactly HEADER_LENGTH bytes`() {
        val header = PacketHeader(
            type = PacketType.TEXT.value,
            nameLength = 0,
            bodyLength = 100L,
            offset = 0L,
            totalLength = 100L
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        header.writeTo(dos)

        assertEquals(PacketHeader.HEADER_LENGTH, baos.size())
    }

    @Test
    fun `readFrom should throw IOException for wrong magic number`() {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        dos.writeShort(0xFFFF.toInt()) // wrong magic number
        dos.writeByte(1)
        dos.writeShort(0)
        dos.writeLong(100L)
        dos.writeLong(0L)
        dos.writeLong(100L)

        val bais = ByteArrayInputStream(baos.toByteArray())
        val dis = DataInputStream(bais)

        assertThrows(IOException::class.java) {
            PacketHeader.readFrom(dis)
        }
    }

    @Test
    fun `header with offset should roundtrip correctly`() {
        val header = PacketHeader(
            type = PacketType.FILE.value,
            nameLength = 5,
            bodyLength = 2048L,
            offset = 512L,
            totalLength = 2048L
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        header.writeTo(dos)

        val bais = ByteArrayInputStream(baos.toByteArray())
        val dis = DataInputStream(bais)
        val readHeader = PacketHeader.readFrom(dis)

        assertEquals(512L, readHeader.offset)
        assertEquals(2048L, readHeader.totalLength)
    }

    @Test
    fun `header with zero nameLength should roundtrip correctly`() {
        val header = PacketHeader(
            type = PacketType.TEXT.value,
            nameLength = 0,
            bodyLength = 50L,
            offset = 0L,
            totalLength = 50L
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        header.writeTo(dos)

        val bais = ByteArrayInputStream(baos.toByteArray())
        val dis = DataInputStream(bais)
        val readHeader = PacketHeader.readFrom(dis)

        assertEquals(0.toShort(), readHeader.nameLength)
    }

    @Test
    fun `header with large bodyLength should roundtrip correctly`() {
        val largeSize = 5L * 1024 * 1024 * 1024 // 5GB
        val header = PacketHeader(
            type = PacketType.FILE.value,
            nameLength = 20,
            bodyLength = largeSize,
            offset = 0L,
            totalLength = largeSize
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        header.writeTo(dos)

        val bais = ByteArrayInputStream(baos.toByteArray())
        val dis = DataInputStream(bais)
        val readHeader = PacketHeader.readFrom(dis)

        assertEquals(largeSize, readHeader.bodyLength)
    }

    @Test
    fun `data class equals should work correctly`() {
        val h1 = PacketHeader(type = 1, nameLength = 10, bodyLength = 100L, offset = 0L, totalLength = 100L)
        val h2 = PacketHeader(type = 1, nameLength = 10, bodyLength = 100L, offset = 0L, totalLength = 100L)
        val h3 = PacketHeader(type = 2, nameLength = 10, bodyLength = 100L, offset = 0L, totalLength = 100L)

        assertEquals(h1, h2)
        assertNotEquals(h1, h3)
    }
}

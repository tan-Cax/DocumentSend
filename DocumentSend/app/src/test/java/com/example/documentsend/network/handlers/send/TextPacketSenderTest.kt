package com.example.documentsend.network.handlers.send

import com.example.documentsend.network.PacketHeader
import com.example.documentsend.network.PacketType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class TextPacketSenderTest {

    private lateinit var sender: TextPacketSender

    @Before
    fun setup() {
        sender = TextPacketSender()
    }

    @Test
    fun `send should write header and body correctly`() = runTest {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        val content = SendContent.Text("Hello, World!")

        val result = sender.send(dos, content, PacketType.TEXT)

        assertTrue(result.isSuccess)

        // Verify written bytes
        val bytes = baos.toByteArray()
        val bais = ByteArrayInputStream(bytes)
        val dis = DataInputStream(bais)

        // Read header
        val header = PacketHeader.readFrom(dis)
        assertEquals(PacketType.TEXT.value, header.type)
        assertEquals(0.toShort(), header.nameLength) // text has no filename
        assertEquals(13L, header.bodyLength) // "Hello, World!" is 13 bytes UTF-8

        // Read body
        val bodyBytes = ByteArray(header.bodyLength.toInt())
        dis.readFully(bodyBytes)
        assertEquals("Hello, World!", String(bodyBytes, Charsets.UTF_8))
    }

    @Test
    fun `send with empty message should succeed`() = runTest {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        val content = SendContent.Text("")

        val result = sender.send(dos, content, PacketType.TEXT)

        assertTrue(result.isSuccess)
        assertEquals(0L, baos.toByteArray().size - PacketHeader.HEADER_LENGTH.toLong())
    }

    @Test
    fun `send with unicode message should succeed`() = runTest {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        val content = SendContent.Text("你好世界🌍")

        val result = sender.send(dos, content, PacketType.TEXT)

        assertTrue(result.isSuccess)

        val bytes = baos.toByteArray()
        val bais = ByteArrayInputStream(bytes)
        val dis = DataInputStream(bais)
        val header = PacketHeader.readFrom(dis)

        val bodyBytes = ByteArray(header.bodyLength.toInt())
        dis.readFully(bodyBytes)
        assertEquals("你好世界🌍", String(bodyBytes, Charsets.UTF_8))
    }

    @Test
    fun `send with wrong content type should return failure`() = runTest {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        val content = SendContent.File("test.txt", 100L, ByteArrayInputStream(ByteArray(100)))

        val result = sender.send(dos, content, PacketType.FILE)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `header nameLength should be 0 for text packets`() = runTest {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        val content = SendContent.Text("test")

        sender.send(dos, content, PacketType.TEXT)

        val bais = ByteArrayInputStream(baos.toByteArray())
        val dis = DataInputStream(bais)
        val header = PacketHeader.readFrom(dis)

        assertEquals(0.toShort(), header.nameLength)
    }

    @Test
    fun `header offset and totalLength should match message size`() = runTest {
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        val message = "测试消息长度"
        val content = SendContent.Text(message)
        val expectedSize = message.toByteArray(Charsets.UTF_8).size.toLong()

        sender.send(dos, content, PacketType.TEXT)

        val bais = ByteArrayInputStream(baos.toByteArray())
        val dis = DataInputStream(bais)
        val header = PacketHeader.readFrom(dis)

        assertEquals(0L, header.offset)
        assertEquals(expectedSize, header.totalLength)
    }
}

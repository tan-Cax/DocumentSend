package com.example.documentsend.network.handlers.receive

import com.example.documentsend.network.PacketHeader
import com.example.documentsend.network.PacketType
import com.example.documentsend.network.handlers.INetworkListener
import com.example.documentsend.repository.HistoryRepository
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class TextPacketReceiverTest {

    private lateinit var receiver: TextPacketReceiver
    private lateinit var listener: INetworkListener
    private lateinit var historyRepository: HistoryRepository

    @Before
    fun setup() {
        historyRepository = mockk(relaxed = true)
        receiver = TextPacketReceiver(historyRepository)
        listener = mockk(relaxed = true)
    }

    @Test
    fun `receive should call onTextMessage with correct text`() = runTest {
        val text = "Hello, World!"
        val textBytes = text.toByteArray(Charsets.UTF_8)
        val header = PacketHeader(
            type = PacketType.TEXT.value,
            nameLength = 0,
            bodyLength = textBytes.size.toLong(),
            offset = 0,
            totalLength = textBytes.size.toLong()
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        dos.write(textBytes)

        val dis = DataInputStream(ByteArrayInputStream(baos.toByteArray()))
        val result = receiver.receive(header, dis, listener, "192.168.1.100")

        assertTrue(result.isSuccess)
        verify { listener.onTextMessage(text) }
    }

    @Test
    fun `receive should call onTextMessage with empty string`() = runTest {
        val header = PacketHeader(
            type = PacketType.TEXT.value,
            nameLength = 0,
            bodyLength = 0L,
            offset = 0,
            totalLength = 0L
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        val dis = DataInputStream(ByteArrayInputStream(baos.toByteArray()))
        val result = receiver.receive(header, dis, listener, "192.168.1.100")

        assertTrue(result.isSuccess)
        verify { listener.onTextMessage("") }
    }

    @Test
    fun `receive should skip nameLength bytes if nameLength is positive`() = runTest {
        val text = "test"
        val textBytes = text.toByteArray(Charsets.UTF_8)
        val nameBytes = "filename.txt".toByteArray(Charsets.UTF_8)
        val header = PacketHeader(
            type = PacketType.TEXT.value,
            nameLength = nameBytes.size.toShort(),
            bodyLength = textBytes.size.toLong(),
            offset = 0,
            totalLength = textBytes.size.toLong()
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        dos.write(nameBytes)
        dos.write(textBytes)

        val dis = DataInputStream(ByteArrayInputStream(baos.toByteArray()))
        val result = receiver.receive(header, dis, listener, "192.168.1.100")

        assertTrue(result.isSuccess)
        verify { listener.onTextMessage(text) }
    }

    @Test
    fun `receive should handle unicode text`() = runTest {
        val text = "你好世界🌍"
        val textBytes = text.toByteArray(Charsets.UTF_8)
        val header = PacketHeader(
            type = PacketType.TEXT.value,
            nameLength = 0,
            bodyLength = textBytes.size.toLong(),
            offset = 0,
            totalLength = textBytes.size.toLong()
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        dos.write(textBytes)

        val dis = DataInputStream(ByteArrayInputStream(baos.toByteArray()))
        val result = receiver.receive(header, dis, listener, "192.168.1.100")

        assertTrue(result.isSuccess)
        verify { listener.onTextMessage(text) }
    }

    @Test
    fun `receive should return failure on IOException`() = runTest {
        val header = PacketHeader(
            type = PacketType.TEXT.value,
            nameLength = 0,
            bodyLength = 100L, // claim 100 bytes but provide 0
            offset = 0,
            totalLength = 100L
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)

        val dis = DataInputStream(ByteArrayInputStream(baos.toByteArray()))
        val result = receiver.receive(header, dis, listener, "192.168.1.100")

        assertTrue(result.isFailure)
    }
}

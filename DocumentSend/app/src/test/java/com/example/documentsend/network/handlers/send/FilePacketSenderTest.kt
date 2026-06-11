package com.example.documentsend.network.handlers.send

import com.example.documentsend.manager.TransferManager
import com.example.documentsend.network.PacketHeader
import com.example.documentsend.network.PacketType
import com.example.documentsend.repository.HistoryRepository
import io.mockk.*
import io.mockk.coVerify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

class FilePacketSenderTest {

    private lateinit var sender: FilePacketSender
    private lateinit var transferManager: TransferManager
    private lateinit var historyRepository: HistoryRepository

    @Before
    fun setup() {
        transferManager = mockk(relaxed = true)
        historyRepository = mockk(relaxed = true)
        sender = FilePacketSender(transferManager, historyRepository)
    }

    @Test
    fun `send should write header and body correctly`() = runTest {
        val fileData = ByteArray(1024) { it.toByte() }
        val inputStream = ByteArrayInputStream(fileData)
        val content = SendContent.File(
            fileName = "test.txt",
            fileLength = fileData.size.toLong(),
            inputStream = inputStream,
            offset = 0,
            historyId = null
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        val result = sender.send(dos, content, PacketType.FILE)

        assertTrue(result.isSuccess)

        val bytes = baos.toByteArray()
        val bais = ByteArrayInputStream(bytes)
        val dis = DataInputStream(bais)
        val header = PacketHeader.readFrom(dis)

        assertEquals(PacketType.FILE.value, header.type)
        assertEquals("test.txt".toByteArray(Charsets.UTF_8).size.toShort(), header.nameLength)
        assertEquals(fileData.size.toLong(), header.bodyLength)
        assertEquals(0L, header.offset)

        val nameBytes = ByteArray(header.nameLength.toInt())
        dis.readFully(nameBytes)
        assertEquals("test.txt", String(nameBytes, Charsets.UTF_8))

        val bodyBytes = ByteArray(header.bodyLength.toInt())
        dis.readFully(bodyBytes)
        assertArrayEquals(fileData, bodyBytes)
    }

    @Test
    fun `send with offset should skip bytes`() = runTest {
        val fileData = ByteArray(1024) { it.toByte() }
        val inputStream = ByteArrayInputStream(fileData)
        val offset = 100L
        val content = SendContent.File(
            fileName = "test.bin",
            fileLength = fileData.size.toLong(),
            inputStream = inputStream,
            offset = offset,
            historyId = null
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        val result = sender.send(dos, content, PacketType.FILE)

        assertTrue(result.isSuccess)

        val bytes = baos.toByteArray()
        val bais = ByteArrayInputStream(bytes)
        val dis = DataInputStream(bais)
        val header = PacketHeader.readFrom(dis)

        assertEquals(offset, header.offset)
        assertEquals(fileData.size.toLong(), header.totalLength)
    }

    @Test
    fun `send should update transferManager progress`() = runTest {
        val fileData = ByteArray(100) { it.toByte() }
        val inputStream = ByteArrayInputStream(fileData)
        val content = SendContent.File(
            fileName = "progress.txt",
            fileLength = fileData.size.toLong(),
            inputStream = inputStream,
            offset = 0,
            historyId = null
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        sender.send(dos, content, PacketType.FILE)

        verify { transferManager.setIdle() }
    }

    @Test
    fun `send with historyId should update offset in repository`() = runTest {
        val fileData = ByteArray(100) { it.toByte() }
        val inputStream = ByteArrayInputStream(fileData)
        val content = SendContent.File(
            fileName = "saved.txt",
            fileLength = fileData.size.toLong(),
            inputStream = inputStream,
            offset = 0,
            historyId = 42
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        sender.send(dos, content, PacketType.FILE)

        coVerify { historyRepository.updateOffset(42, fileData.size.toLong()) }
    }

    @Test
    fun `send with wrong content type should return failure`() = runTest {
        val content = SendContent.Text("wrong type")
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        val result = sender.send(dos, content, PacketType.TEXT)

        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is IllegalArgumentException)
    }

    @Test
    fun `send should call setIdle on exception`() = runTest {
        val badStream = object : java.io.InputStream() {
            override fun read(): Int = throw java.io.IOException("Stream error")
        }
        val content = SendContent.File(
            fileName = "bad.txt",
            fileLength = 100L,
            inputStream = badStream,
            offset = 0,
            historyId = null
        )

        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        val result = sender.send(dos, content, PacketType.FILE)

        assertTrue(result.isFailure)
        verify { transferManager.setIdle() }
    }
}

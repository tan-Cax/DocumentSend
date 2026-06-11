package com.example.documentsend.repository

import com.example.documentsend.data.History
import com.example.documentsend.data.HistoryDao
import com.example.documentsend.data.HistoryType
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class HistoryRepositoryTest {

    private lateinit var dao: HistoryDao
    private lateinit var repository: HistoryRepository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = HistoryRepository(dao)
    }

    @Test
    fun `getAllHistory should delegate to dao`() = runTest {
        val historyList = listOf(
            History(id = 1, name = "a.txt"),
            History(id = 2, name = "b.txt")
        )
        every { dao.getAllHistory() } returns flowOf(historyList)

        val result = repository.getAllHistory().toList()

        assertEquals(1, result.size)
        assertEquals(2, result[0].size)
        assertEquals("a.txt", result[0][0].name)
    }

    @Test
    fun `insertHistory should call dao insertHistory`() = runTest {
        val history = History(name = "test.txt")

        repository.insertHistory(history)

        coVerify { dao.insertHistory(history) }
    }

    @Test
    fun `deleteHistory should call dao deleteHistory`() = runTest {
        repository.deleteHistory(42)

        coVerify { dao.deleteHistory(42) }
    }

    @Test
    fun `getUnfinishedTransfer with fileName should return dao result`() = runTest {
        val expected = History(id = 1, name = "partial.txt", offset = 100L, totalLength = 200L)
        coEvery { dao.getUnfinishedTransfer("partial.txt") } returns expected

        val result = repository.getUnfinishedTransfer("partial.txt")

        assertEquals(expected, result)
    }

    @Test
    fun `getUnfinishedTransfer with fileName and targetIp should return dao result`() = runTest {
        val expected = History(id = 2, name = "file.bin", targetIp = "192.168.1.1")
        coEvery { dao.getUnfinishedTransfer("file.bin", "192.168.1.1") } returns expected

        val result = repository.getUnfinishedTransfer("file.bin", "192.168.1.1")

        assertEquals(expected, result)
    }

    @Test
    fun `getUnfinishedTransfer should return null when not found`() = runTest {
        coEvery { dao.getUnfinishedTransfer("missing.txt") } returns null

        val result = repository.getUnfinishedTransfer("missing.txt")

        assertNull(result)
    }

    @Test
    fun `updateOffset should call dao updateHistoryOffset`() = runTest {
        repository.updateOffset(5, 500L)

        coVerify { dao.updateHistoryOffset(5, 500L) }
    }

    @Test
    fun `getHistoryById should return dao result`() = runTest {
        val expected = History(id = 3, name = "found.txt")
        coEvery { dao.getHistoryById(3) } returns expected

        val result = repository.getHistoryById(3)

        assertEquals(expected, result)
    }

    @Test
    fun `getHistoryById should return null when not found`() = runTest {
        coEvery { dao.getHistoryById(99) } returns null

        val result = repository.getHistoryById(99)

        assertNull(result)
    }
}

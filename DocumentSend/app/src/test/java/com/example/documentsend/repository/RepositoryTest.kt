package com.example.documentsend.repository

import com.example.documentsend.data.IpAddress
import com.example.documentsend.data.IpAddressDao
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RepositoryTest {

    private lateinit var dao: IpAddressDao
    private lateinit var repository: Repository

    @Before
    fun setup() {
        dao = mockk(relaxed = true)
        repository = Repository(dao)
    }

    @Test
    fun `getIpHistory should delegate to dao`() = runTest {
        val ipList = listOf(
            IpAddress(id = 1, ip = "192.168.1.1"),
            IpAddress(id = 2, ip = "10.0.0.1")
        )
        every { dao.getAllIpAddresses() } returns flowOf(ipList)

        val result = repository.getIpHistory().toList()

        assertEquals(1, result.size)
        assertEquals(2, result[0].size)
        assertEquals("192.168.1.1", result[0][0].ip)
    }

    @Test
    fun `insertIP should call dao insertIPAddress`() = runTest {
        val ip = IpAddress(ip = "172.16.0.1")

        repository.insertIP(ip)

        coVerify { dao.insertIPAddress(ip) }
    }

    @Test
    fun `updateIP should call dao updateIPAddress`() = runTest {
        val ip = IpAddress(id = 5, ip = "updated.ip")

        repository.updateIP(ip)

        coVerify { dao.updateIPAddress(ip) }
    }

    @Test
    fun `getIpHistory should return empty list when dao returns empty`() = runTest {
        every { dao.getAllIpAddresses() } returns flowOf(emptyList())

        val result = repository.getIpHistory().toList()

        assertEquals(1, result.size)
        assertTrue(result[0].isEmpty())
    }
}

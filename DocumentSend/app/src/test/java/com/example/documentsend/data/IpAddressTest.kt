package com.example.documentsend.data

import org.junit.Assert.*
import org.junit.Test

class IpAddressTest {

    @Test
    fun `default id should be 0`() {
        val ip = IpAddress(ip = "192.168.1.1")
        assertEquals(0, ip.id)
    }

    @Test
    fun `should store ip correctly`() {
        val ip = IpAddress(ip = "10.0.0.1")
        assertEquals("10.0.0.1", ip.ip)
    }

    @Test
    fun `should create with custom id`() {
        val ip = IpAddress(id = 5, ip = "172.16.0.1")
        assertEquals(5, ip.id)
        assertEquals("172.16.0.1", ip.ip)
    }

    @Test
    fun `equals should work correctly`() {
        val ip1 = IpAddress(id = 1, ip = "192.168.1.1")
        val ip2 = IpAddress(id = 1, ip = "192.168.1.1")
        val ip3 = IpAddress(id = 2, ip = "192.168.1.1")

        assertEquals(ip1, ip2)
        assertNotEquals(ip1, ip3)
    }

    @Test
    fun `copy should create new instance`() {
        val original = IpAddress(id = 1, ip = "old.ip")
        val copied = original.copy(ip = "new.ip")

        assertEquals(1, copied.id)
        assertEquals("new.ip", copied.ip)
        assertNotEquals(original, copied)
    }

    @Test
    fun `hashCode should be equal for equal objects`() {
        val ip1 = IpAddress(id = 1, ip = "1.2.3.4")
        val ip2 = IpAddress(id = 1, ip = "1.2.3.4")

        assertEquals(ip1.hashCode(), ip2.hashCode())
    }

    @Test
    fun `should handle empty ip string`() {
        val ip = IpAddress(ip = "")
        assertEquals("", ip.ip)
    }

    @Test
    fun `should handle ipv6 address`() {
        val ip = IpAddress(ip = "2001:0db8:85a3:0000:0000:8a2e:0370:7334")
        assertEquals("2001:0db8:85a3:0000:0000:8a2e:0370:7334", ip.ip)
    }
}

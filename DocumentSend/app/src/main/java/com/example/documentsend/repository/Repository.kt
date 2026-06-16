package com.example.documentsend.repository

import com.example.documentsend.data.IpAddress
import com.example.documentsend.data.IpAddressDao

class Repository(private val dao: IpAddressDao) {

    fun getIpHistory() = dao.getAllIpAddresses()

    suspend fun insertIP(ipAddress: IpAddress) {
        val exists = dao.countByIp(ipAddress.ip) > 0
        if (!exists) {
            dao.insertIPAddress(ipAddress)
        }
    }

    suspend fun updateIP(ipAddress: IpAddress) {
        dao.updateIPAddress(ipAddress)
    }
}

package com.example.documentsend.udp

import com.example.documentsend.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface

class UdpSender(
    private val uuid: String,
    private val tcpPort: Int,
    private val deviceName: String
) {

    fun broadcast() {
        Logger.logInfo("UDP", "ForceBroadcast", "手动发送UDP广播")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val s = DatagramSocket().apply { broadcast = true }
                val broadcastAddress = getBroadcastAddress() ?: run {
                    Logger.logWarn("UDP", "ForceBroadcast", "无法获取广播地址")
                    return@launch
                }
                val announce = UdpAnnounce(
                    uuid = uuid,
                    device = "android",
                    tcpPort = tcpPort,
                    deviceName = deviceName,
                    reply = false
                )
                val data = announce.toJson().toByteArray()
                val packet = DatagramPacket(data, data.size, broadcastAddress, UdpAnnounce.UDP_PORT)
                s.send(packet)
                s.close()
            } catch (_: Exception) {
            }
        }
    }

    fun replyTo(targetIp: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val s = DatagramSocket()
                val targetAddress = InetAddress.getByName(targetIp)
                val announce = UdpAnnounce(
                    uuid = uuid,
                    device = "android",
                    tcpPort = tcpPort,
                    deviceName = deviceName,
                    reply = true
                )
                val data = announce.toJson().toByteArray()
                val packet = DatagramPacket(data, data.size, targetAddress, UdpAnnounce.UDP_PORT)
                s.send(packet)
                s.close()
                Logger.logInfo("UDP", "ReplySent", "回复设备: $targetIp")
            } catch (_: Exception) {
            }
        }
    }

    fun stop() {
        Logger.logInfo("UDP", "SenderStop", "UDP广播发送器停止")
    }

    private fun getBroadcastAddress(): InetAddress? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue

                val interfaceAddresses = networkInterface.interfaceAddresses
                for (interfaceAddress in interfaceAddresses) {
                    val address = interfaceAddress.address
                    if (address is Inet4Address) {
                        val prefixLength = interfaceAddress.networkPrefixLength
                        val mask = prefixLengthToMask(prefixLength)
                        val ip = address.address
                        val broadcast = ByteArray(4)
                        for (i in 0..3) {
                            broadcast[i] = (ip[i].toInt() or (mask[i].toInt() xor 0xFF)).toByte()
                        }
                        return InetAddress.getByAddress(broadcast)
                    }
                }
            }
        } catch (_: Exception) {
        }
        return null
    }

    private fun prefixLengthToMask(prefixLength: Short): ByteArray {
        val mask = ByteArray(4)
        val fullBytes = prefixLength.toInt() / 8
        val remainingBits = prefixLength.toInt() % 8
        for (i in 0 until fullBytes) {
            mask[i] = 0xFF.toByte()
        }
        if (fullBytes < 4 && remainingBits > 0) {
            mask[fullBytes] = (0xFF shl (8 - remainingBits)).toByte()
        }
        return mask
    }
}

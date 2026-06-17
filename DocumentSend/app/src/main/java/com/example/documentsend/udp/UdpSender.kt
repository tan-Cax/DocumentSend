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
            val addresses = getBroadcastAddresses()
            if (addresses.isEmpty()) {
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
            for (addr in addresses) {
                try {
                    val s = DatagramSocket().apply { broadcast = true }
                    val packet = DatagramPacket(data, data.size, addr, UdpAnnounce.UDP_PORT)
                    s.send(packet)
                    s.close()
                    Logger.logInfo("UDP", "BroadcastSent", "广播发送到: ${addr.hostAddress}")
                } catch (e: Exception) {
                    Logger.logError("UDP", "BroadcastFailed", e)
                }
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

    private fun getBroadcastAddresses(): List<InetAddress> {
        val result = mutableListOf<InetAddress>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return result
            while (interfaces.hasMoreElements()) {
                val ni = interfaces.nextElement()
                if (!ni.isUp || ni.isLoopback || ni.isVirtual) continue
                for (ia in ni.interfaceAddresses) {
                    val broadcast = ia.broadcast ?: continue
                    if (broadcast is Inet4Address) {
                        result.add(broadcast)
                        Logger.logInfo("UDP", "BroadcastAddr", "发现广播地址: ${broadcast.hostAddress}")
                    }
                }
            }
        } catch (e: Exception) {
            Logger.logError("UDP", "BroadcastAddrError", e)
        }
        return result
    }
}

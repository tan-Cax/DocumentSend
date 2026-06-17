package com.example.documentsend.udp

import com.example.documentsend.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket

class UdpReceiver {

    private var scope: CoroutineScope? = null
    private var socket: DatagramSocket? = null

    private val _received = MutableSharedFlow<UdpAnnounce>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val received = _received.asSharedFlow()

    fun start() {
        if (scope != null) return
        Logger.logInfo("UDP", "ReceiverStart", "UDP广播接收器启动, 端口: ${UdpAnnounce.UDP_PORT}")
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope!!.launch {
            try {
                //socket = DatagramSocket(UdpAnnounce.UDP_PORT)
                socket = DatagramSocket(UdpAnnounce.UDP_PORT).apply {
                    broadcast = true  // 允许接收广播数据包
                }
                val buffer = ByteArray(4096)
                while (true) {
                    val packet = DatagramPacket(buffer, buffer.size)
                    try {
                        socket?.receive(packet)
                        val json = String(packet.data, 0, packet.length)
                        val announce = UdpAnnounce.fromJson(json)?.copy(senderIp = packet.address.hostAddress ?: "") ?: continue
                        Logger.logInfo("UDP", "PacketReceived", "来自: ${announce.senderIp}, uuid=${announce.uuid}, reply=${announce.reply}")
                        _received.tryEmit(announce)
                    } catch (e: Exception) {
                        Logger.logError("UDP", "ReceiveError", e)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    fun stop() {
        Logger.logInfo("UDP", "ReceiverStop", "UDP广播接收器停止")
        scope?.cancel()
        scope = null
        socket?.close()
        socket = null
    }
}

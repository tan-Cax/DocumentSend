package com.example.documentsend.network

import android.content.Context
import com.example.documentsend.manager.TransferManager
import com.example.documentsend.network.handlers.INetworkListener
import com.example.documentsend.network.handlers.receive.IPacketReceiver
import com.example.documentsend.network.handlers.receive.TextPacketReceiver
import com.example.documentsend.network.handlers.receive.FilePacketReceiver
import com.example.documentsend.repository.HistoryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket

class SocketServer(
    private val context: Context,
    private val transferManager: TransferManager,
    private val historyRepository: HistoryRepository,
    private val autoSave: Boolean = true
) {

    private var serverSocket: ServerSocket? = null
    private var isRunning = false

    private val receivers = mapOf<PacketType, IPacketReceiver>(
        PacketType.TEXT to TextPacketReceiver(historyRepository),
        PacketType.IMAGE to FilePacketReceiver(context, transferManager, historyRepository, autoSave),
        PacketType.VIDEO to FilePacketReceiver(context, transferManager, historyRepository, autoSave),
        PacketType.FILE to FilePacketReceiver(context, transferManager, historyRepository, autoSave),
        PacketType.ARCHIVE to FilePacketReceiver(context, transferManager, historyRepository, autoSave)
    )

    fun start(port: Int, listener: INetworkListener) {
        if (isRunning) {
            listener.onError("服务端已经在运行中")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true

                while (isRunning) {
                    val socket = serverSocket!!.accept()
                    launch {
                        handleConnection(socket, listener)
                    }
                }
            } catch (e: IOException) {
                if (isRunning) {
                    listener.onError("服务端异常: ${e.message}")
                }
            }
        }
    }

    private suspend fun handleConnection(socket: Socket, listener: INetworkListener) {
        val clientIp = socket.inetAddress.hostAddress ?: "未知"
        listener.onConnected(clientIp)

        try {
            val dis = DataInputStream(socket.getInputStream())
            while (isRunning) {
                val header = PacketHeader.readFrom(dis)
                val type = PacketType.fromValue(header.type)

                if (type == null) {
                    skipBytesFully(dis, header.nameLength.toLong() + header.bodyLength)
                    continue
                }

                val receiver = receivers[type]
                if (receiver != null) {
                    receiver.receive(header, dis, listener, clientIp)
                } else {
                    skipBytesFully(dis, header.nameLength.toLong() + header.bodyLength)
                }
            }
        } catch (e: Exception) {
            // 连接断开
        } finally {
            listener.onDisconnected()
            socket.close()
        }
    }

    private fun skipBytesFully(dis: DataInputStream, bytesToSkip: Long) {
        var remaining = bytesToSkip
        while (remaining > 0) {
            val skipped = dis.skip(remaining)
            if (skipped <= 0) break
            remaining -= skipped
        }
    }

    fun stop() {
        isRunning = false
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }
    }
}

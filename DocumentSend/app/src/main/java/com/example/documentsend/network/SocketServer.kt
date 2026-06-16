package com.example.documentsend.network

import android.content.Context
import com.example.documentsend.manager.TransferManager
import com.example.documentsend.network.handlers.INetworkListener
import com.example.documentsend.network.handlers.receive.IPacketReceiver
import com.example.documentsend.network.handlers.receive.TextPacketReceiver
import com.example.documentsend.network.handlers.receive.FilePacketReceiver
import com.example.documentsend.repository.HistoryRepository
import com.example.documentsend.utils.StreamUtils
import com.example.documentsend.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
    @Volatile
    private var isRunning = false
    private var mainScope: CoroutineScope? = null
    private var serverScope: CoroutineScope? = null

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

        mainScope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())
        serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        serverScope!!.launch {
            try {
                serverSocket = ServerSocket(port)
                isRunning = true
                Logger.logInfo("Network", "ServerStart", "服务端启动，监听端口: $port")

                while (isRunning) {
                    val socket = serverSocket!!.accept()
                    Logger.logInfo("Network", "ClientConnect", "客户端连接: ${socket.inetAddress.hostAddress}")
                    launch {
                        handleConnection(socket, listener)
                    }
                }
            } catch (e: IOException) {
                if (isRunning) {
                    Logger.logError("Network", "ServerException", e)
                    listener.onError("服务端异常: ${e.message}")
                }
            }
        }
    }

    private suspend fun handleConnection(socket: Socket, listener: INetworkListener) {
        val clientIp = socket.inetAddress.hostAddress ?: "未知"
        mainScope?.launch { listener.onConnected(clientIp) }

        try {
            val dis = DataInputStream(socket.getInputStream())
            while (isRunning) {
                val header = PacketHeader.readFrom(dis)
                val type = PacketType.fromValue(header.type)

                if (type == null) {
                    StreamUtils.skipBytesFully(dis, header.nameLength.toLong() + header.bodyLength)
                    continue
                }

                val receiver = receivers[type]
                if (receiver != null) {
                    receiver.receive(header, dis, listener, clientIp)
                } else {
                    StreamUtils.skipBytesFully(dis, header.nameLength.toLong() + header.bodyLength)
                }
            }
        } catch (e: Exception) {
            Logger.logWarn("Network", "ClientDisconnect", "客户端断开: $clientIp, ${e.message}")
        } finally {
            mainScope?.launch { listener.onDisconnected() }
            socket.close()
        }
    }

    fun stop() {
        Logger.logInfo("Network", "ServerStop", "服务端停止")
        isRunning = false
        serverScope?.cancel()
        mainScope?.cancel()
        mainScope = null
        try {
            serverSocket?.close()
        } catch (_: Exception) {
        }
    }
}

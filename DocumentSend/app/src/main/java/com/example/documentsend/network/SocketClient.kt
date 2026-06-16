package com.example.documentsend.network

import android.content.Context
import com.example.documentsend.manager.TransferManager
import com.example.documentsend.network.handlers.send.IPacketSender
import com.example.documentsend.network.handlers.send.SendContent
import com.example.documentsend.network.handlers.send.TextPacketSender
import com.example.documentsend.network.handlers.send.FilePacketSender
import com.example.documentsend.repository.HistoryRepository
import com.example.documentsend.log.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket

class SocketClient(
    private val context: Context,
    private val transferManager: TransferManager,
    private val historyRepository: HistoryRepository
) {

    private val senders = mapOf<PacketType, IPacketSender>(
        PacketType.TEXT to TextPacketSender(),
        PacketType.IMAGE to FilePacketSender(transferManager, historyRepository),
        PacketType.VIDEO to FilePacketSender(transferManager, historyRepository),
        PacketType.FILE to FilePacketSender(transferManager, historyRepository),
        PacketType.ARCHIVE to FilePacketSender(transferManager, historyRepository)
    )

    suspend fun send(
        ip: String,
        port: Int,
        packetType: PacketType,
        content: SendContent
    ): Result<String> = withContext(Dispatchers.IO) {
        val socket = Socket()
        try {
            Logger.logInfo("Network", "Connect", "连接目标: $ip:$port")
            socket.connect(InetSocketAddress(ip, port), 5000)
            val dos = DataOutputStream(socket.getOutputStream())

            val sender = senders[packetType]
                ?: return@withContext Result.failure(IllegalArgumentException("No sender for type: $packetType"))

            sender.send(dos, content, packetType)
            Logger.logInfo("Network", "SendSuccess", "发送成功: $ip:$port")
            Result.success("发送成功")
        } catch (e: Exception) {
            Logger.logError("Network", "SendFailed", e)
            Result.failure(e)
        } finally {
            try {
                socket.close()
            } catch (_: Exception) {
            }
        }
    }
}

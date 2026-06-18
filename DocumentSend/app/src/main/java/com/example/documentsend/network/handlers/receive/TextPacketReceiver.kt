package com.example.documentsend.network.handlers.receive

import com.example.documentsend.data.History
import com.example.documentsend.data.HistoryType
import com.example.documentsend.log.Logger
import com.example.documentsend.network.PacketHeader
import com.example.documentsend.network.PacketType
import com.example.documentsend.utils.XorCipher
import com.example.documentsend.network.handlers.INetworkListener
import com.example.documentsend.repository.HistoryRepository
import com.example.documentsend.utils.StreamUtils
import java.io.DataInputStream

class TextPacketReceiver(
    private val historyRepository: HistoryRepository,
    var saveToHistory: Boolean = true
) : IPacketReceiver {

    override suspend fun receive(header: PacketHeader, dis: DataInputStream, listener: INetworkListener, senderIp: String): Result<Unit> {
        return try {
            if (header.nameLength > 0) {
                val nameBytes = ByteArray(header.nameLength.toInt())
                dis.readFully(nameBytes)
            }

            val bodyLength = header.bodyLength
            if (bodyLength > Int.MAX_VALUE) {
                StreamUtils.skipBytesFully(dis, bodyLength)
                return Result.failure(IllegalArgumentException("文本消息过长"))
            }

            val bodyBytes = ByteArray(bodyLength.toInt())
            dis.readFully(bodyBytes)
            XorCipher.xor(bodyBytes, 0, bodyBytes.size)
            val text = String(bodyBytes, Charsets.UTF_8)
            Logger.logInfo("Network", "TextReceive", "收到文本消息, 长度: ${text.length}")

            val record = History(
                name = "",
                filetypeString = PacketType.TEXT.name,
                typeString = HistoryType.RECEIVE,
                targetIp = senderIp,
                totalLength = text.toByteArray().size.toLong(),
                offset = text.toByteArray().size.toLong(),
                text = text
            )
            if (saveToHistory) {
                historyRepository.insertHistory(record)
            }
            listener.onReceiveRecord(record)

            listener.onTextMessage(text)
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.logError("Network", "TextReceiveFailed", e)
            Result.failure(e)
        }
    }
}

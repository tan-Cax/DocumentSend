package com.example.documentsend.network.handlers.receive

import com.example.documentsend.network.PacketHeader
import com.example.documentsend.network.handlers.INetworkListener
import java.io.DataInputStream

class TextPacketReceiver : IPacketReceiver {

    override suspend fun receive(header: PacketHeader, dis: DataInputStream, listener: INetworkListener): Result<Unit> {
        return try {
            if (header.nameLength > 0) {
                val nameBytes = ByteArray(header.nameLength.toInt())
                dis.readFully(nameBytes)
            }

            val bodyLength = header.bodyLength
            if (bodyLength > Int.MAX_VALUE) {
                skipBytesFully(dis, bodyLength)
                return Result.failure(IllegalArgumentException("文本消息过长"))
            }

            val bodyBytes = ByteArray(bodyLength.toInt())
            dis.readFully(bodyBytes)
            val text = String(bodyBytes, Charsets.UTF_8)

            listener.onTextMessage(text)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
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
}

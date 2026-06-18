package com.example.documentsend.network.handlers.send

import com.example.documentsend.log.Logger
import com.example.documentsend.network.PacketHeader
import com.example.documentsend.network.PacketType
import com.example.documentsend.utils.XorCipher
import java.io.DataOutputStream

class TextPacketSender : IPacketSender {

    override suspend fun send(dos: DataOutputStream, content: SendContent, packetType: PacketType): Result<Unit> {
        if (content !is SendContent.Text) {
            return Result.failure(IllegalArgumentException("TextPacketSender only accepts SendContent.Text"))
        }

        return try {

            val messageBytes = content.message.toByteArray(Charsets.UTF_8)
            Logger.logInfo("Network", "TextSend", "发送文本, 长度: ${messageBytes.size}")

            val header = PacketHeader(
                type = packetType.value,
                nameLength = 0,
                bodyLength = messageBytes.size.toLong(),
                offset = 0,
                totalLength = messageBytes.size.toLong()
            )
            header.writeTo(dos)

            XorCipher.xor(messageBytes, 0, messageBytes.size)
            dos.write(messageBytes)
            dos.flush()

            Result.success(Unit)
        } catch (e: Exception) {
            Logger.logError("Network", "TextSendFailed", e)
            Result.failure(e)
        }
    }
}

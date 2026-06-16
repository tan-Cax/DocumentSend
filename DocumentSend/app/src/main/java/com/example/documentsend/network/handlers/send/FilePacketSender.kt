package com.example.documentsend.network.handlers.send

import com.example.documentsend.log.Logger
import com.example.documentsend.manager.TransferManager
import com.example.documentsend.network.PacketHeader
import com.example.documentsend.network.PacketType
import com.example.documentsend.repository.HistoryRepository
import java.io.DataOutputStream

class FilePacketSender(
    private val transferManager: TransferManager,
    private val historyRepository: HistoryRepository
) : IPacketSender {

    override suspend fun send(dos: DataOutputStream, content: SendContent, packetType: PacketType): Result<Unit> {
        if (content !is SendContent.File) {
            return Result.failure(IllegalArgumentException("FilePacketSender only accepts SendContent.File"))
        }

        return try {
            val fileNameBytes = content.fileName.toByteArray(Charsets.UTF_8)
            val fileLength = content.fileLength
            val offset = content.offset

            Logger.logInfo("Network", "FileSendStart", "开始发送文件: ${content.fileName}, 大小: $fileLength, offset: $offset")

            // skip offset 字节
            var skipRemaining = offset
            while (skipRemaining > 0) {
                val skipped = content.inputStream.skip(skipRemaining)
                if (skipped <= 0) break
                skipRemaining -= skipped
            }

            // 写入 PacketHeader
            val header = PacketHeader(
                type = packetType.value,
                nameLength = fileNameBytes.size.toShort(),
                bodyLength = fileLength,
                offset = offset,
                totalLength = fileLength
            )
            header.writeTo(dos)

            // 写入文件名
            dos.write(fileNameBytes)

            // 循环发送文件内容
            val buffer = ByteArray(8192)
            var totalSent = offset
            var lastOffsetSave = offset

            try {
                var bytesRead: Int
                while (content.inputStream.read(buffer).also { bytesRead = it } != -1) {
                    dos.write(buffer, 0, bytesRead)
                    totalSent += bytesRead

                    // 更新传输进度
                    transferManager.updateSendingProgress(content.fileName, totalSent, fileLength)

                    // 每 500KB 保存 offset
                    if (totalSent - lastOffsetSave >= 500 * 1024) {
                        content.historyId?.let { id ->
                            historyRepository.updateOffset(id, totalSent)
                        }
                        lastOffsetSave = totalSent
                    }
                }
                dos.flush()
            } finally {
                content.inputStream.close()
            }

            // 完成后更新 offset = totalLength
            content.historyId?.let { id ->
                historyRepository.updateOffset(id, fileLength)
            }

            transferManager.setIdle()
            Logger.logInfo("Network", "FileSendComplete", "文件发送完成: ${content.fileName}")
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.logError("Network", "FileSendFailed", e)
            transferManager.setIdle()
            Result.failure(e)
        }
    }
}

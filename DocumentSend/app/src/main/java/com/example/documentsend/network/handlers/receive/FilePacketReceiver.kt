package com.example.documentsend.network.handlers.receive

import android.content.Context
import com.example.documentsend.manager.TransferManager
import com.example.documentsend.network.PacketHeader
import com.example.documentsend.network.handlers.INetworkListener
import com.example.documentsend.repository.HistoryRepository
import com.example.documentsend.utils.StorageUtils
import java.io.DataInputStream
import java.io.FileOutputStream

class FilePacketReceiver(
    private val context: Context,
    private val transferManager: TransferManager,
    private val historyRepository: HistoryRepository
) : IPacketReceiver {

    override suspend fun receive(
        header: PacketHeader,
        dis: DataInputStream,
        listener: INetworkListener
    ): Result<Unit> {
        return try {
            // 1. 读取文件名
            val nameBytes = ByteArray(header.nameLength.toInt())
            dis.readFully(nameBytes)
            val fileName = String(nameBytes, Charsets.UTF_8)

            // 2. 确定存储路径
            val dir = StorageUtils.getDownloadDir(context)

            // 3. 根据 offset 决定文件
            val file = if (header.offset > 0) {
                // 续传：使用 .partial 文件
                StorageUtils.getPartialFile(dir, fileName)
            } else {
                // 直接发送：删除旧 .partial，创建新文件
                StorageUtils.deletePartial(dir, fileName)
                StorageUtils.getPartialFile(dir, fileName)
            }

            // 4. 打开输出流（追加模式）
            val fos = FileOutputStream(file, header.offset > 0)
            val buffer = ByteArray(8192)
            var totalRead = header.offset
            var remainingBytes = header.bodyLength - header.offset
            var lastOffsetSave = header.offset

            // 5. 通知开始接收
            listener.onFileStarted(fileName, header.totalLength)

            // 6. 循环接收
            try {
                while (remainingBytes > 0) {
                    val toRead = minOf(buffer.size.toLong(), remainingBytes).toInt()
                    val read = dis.read(buffer, 0, toRead)
                    if (read == -1) break

                    fos.write(buffer, 0, read)
                    totalRead += read
                    remainingBytes -= read

                    // 更新传输进度
                    transferManager.updateReceivingProgress(fileName, totalRead, header.totalLength)

                    // 每 500KB 更新进度回调
                    if (totalRead - lastOffsetSave >= 100 * 1024) {
                        listener.onFileProgress(fileName, totalRead, header.totalLength)
                        lastOffsetSave = totalRead
                    }
                }
            } finally {
                fos.close()
            }

            // 7. .partial → 正式文件名
            StorageUtils.renameToFinal(dir, fileName)

            // 8. 通知完成
            listener.onFileFinished(fileName)
            transferManager.setIdle()

            Result.success(Unit)
        } catch (e: Exception) {
            transferManager.setIdle()
            Result.failure(e)
        }
    }
}

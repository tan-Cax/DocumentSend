package com.example.documentsend.network.handlers.receive

import android.content.Context
import com.example.documentsend.data.History
import com.example.documentsend.data.HistoryType
import com.example.documentsend.manager.TransferManager
import com.example.documentsend.network.PacketHeader
import com.example.documentsend.network.handlers.INetworkListener
import com.example.documentsend.repository.HistoryRepository
import com.example.documentsend.utils.NetworkConfigUtils
import com.example.documentsend.utils.StorageUtils
import kotlinx.coroutines.delay
import java.io.DataInputStream
import java.io.File
import java.io.FileOutputStream

class FilePacketReceiver(
    private val context: Context,
    private val transferManager: TransferManager,
    private val historyRepository: HistoryRepository,
    private val autoSave: Boolean = true
) : IPacketReceiver {

    override suspend fun receive(
        header: PacketHeader,
        dis: DataInputStream,
        listener: INetworkListener,
        senderIp: String
    ): Result<Unit> {
        return try {
            // 读取文件名
            val nameBytes = ByteArray(header.nameLength.toInt())
            dis.readFully(nameBytes)
            val fileName = String(nameBytes, Charsets.UTF_8)

            // 确定存储路径
            val dir = if (autoSave) {
                val targetDir = NetworkConfigUtils.getCurrentSavePath(context)
                if (!targetDir.exists() && !targetDir.mkdirs()) {
                    // 外部存储目录创建失败，回退到内部缓存
                    File(context.cacheDir, "pending_files").also { it.mkdirs() }
                } else {
                    targetDir
                }
            } else {
                File(context.cacheDir, "pending_files").also { it.mkdirs() }
            }

            // 根据 offset 决定文件
            val file = if (header.offset > 0) {
                StorageUtils.getPartialFile(dir, fileName)
            } else {
                StorageUtils.deletePartial(dir, fileName)
                StorageUtils.getPartialFile(dir, fileName)
            }

            // 打开输出流（追加模式）
            val fos = FileOutputStream(file, header.offset > 0)
            val buffer = ByteArray(8192)
            var totalRead = header.offset
            var remainingBytes = header.bodyLength - header.offset
            var lastOffsetSave = header.offset

            // 通知开始接收
            listener.onFileStarted(fileName, header.totalLength)

            // 循环接收
            try {
                while (remainingBytes > 0) {
                    val toRead = minOf(buffer.size.toLong(), remainingBytes).toInt()
                    val read = dis.read(buffer, 0, toRead)
                    if (read == -1) break

                    fos.write(buffer, 0, read)
                    totalRead += read
                    remainingBytes -= read

                    transferManager.updateReceivingProgress(fileName, totalRead, header.totalLength)

                    if (totalRead - lastOffsetSave >= 100 * 1024) {
                        listener.onFileProgress(fileName, totalRead, header.totalLength)
                        lastOffsetSave = totalRead
                    }
                }
            } finally {
                fos.close()
            }

            // 重命名为最终文件（自动处理重名）
            val finalFile = StorageUtils.renameToFinal(dir, fileName)

            // 入库并通知记录
            val record = History(
                name = finalFile.name,
                uriString = android.net.Uri.fromFile(finalFile).toString(),
                filetypeString = com.example.documentsend.network.PacketType.fromValue(header.type)?.name ?: "FILE",
                typeString = HistoryType.RECEIVE,
                targetIp = senderIp,
                totalLength = header.totalLength,
                offset = totalRead
            )
            val historyId = historyRepository.insertHistory(record)
            listener.onReceiveRecord(record)

            // 通知完成 - 统一走 onFileReadyToSave，让 ViewModel 处理
            listener.onFileReadyToSave(historyId.toInt(), finalFile.name, header.totalLength, finalFile.absolutePath)

            // 稍微延迟重置状态，让用户看到进度条到 100%
            delay(1000)
            transferManager.setIdle()

            Result.success(Unit)
        } catch (e: Exception) {
            listener.onError("接收文件失败: ${e.message}")
            transferManager.setIdle()
            Result.failure(e)
        }
    }
}

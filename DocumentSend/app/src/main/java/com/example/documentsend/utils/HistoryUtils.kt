package com.example.documentsend.utils

import android.net.Uri
import com.example.documentsend.data.History
import com.example.documentsend.data.HistoryType
import com.example.documentsend.network.PacketType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object HistoryUtils {

    fun createSendRecord(
        fileName: String,
        uri: Uri,
        fileType: PacketType,
        targetIp: String,
        totalLength: Long,
        offset: Long = 0
    ): History {
        return History(
            name = fileName,
            uriString = uri.toString(),
            filetypeString = fileType.name,
            typeString = HistoryType.SEND,
            targetIp = targetIp,
            offset = offset,
            totalLength = totalLength
        )
    }

    fun createTextSendRecoder(
        content: String,
        targetIp: String
    ): History {
        return History(
            name = "",
            uriString = "",
            filetypeString = PacketType.TEXT.name,
            typeString = HistoryType.SEND,
            targetIp = targetIp,
            offset = 0,
            totalLength = content.toByteArray().size.toLong(),
            text = content
        )

    }


    fun createReceiveRecord(
        fileName: String,
        savePath: String,
        senderIp: String,
        totalLength: Long,
        offset: Long = 0
    ): History {
        return History(
            name = fileName,
            uriString = savePath,
            filetypeString = PacketType.FILE.name,
            typeString = HistoryType.RECEIVE,
            targetIp = senderIp,
            offset = offset,
            totalLength = totalLength
        )
    }

    fun getUri(history: History): Uri? {
        return if (history.uriString.isNotEmpty()) Uri.parse(history.uriString) else null
    }

    fun getPacketType(history: History): PacketType? {
        return try {
            PacketType.valueOf(history.filetypeString)
        } catch (e: Exception) {
            null
        }
    }

    fun isSend(history: History): Boolean {
        return history.typeString == HistoryType.SEND
    }

    fun isReceive(history: History): Boolean {
        return history.typeString == HistoryType.RECEIVE
    }

    fun isCompleted(history: History): Boolean {
        return history.offset >= history.totalLength
    }

    fun getProgress(history: History): Float {
        return if (history.totalLength > 0) {
            history.offset.toFloat() / history.totalLength.toFloat()
        } else 0f
    }

    fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}

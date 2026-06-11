package com.example.documentsend.network.handlers.send

import android.content.Context
import android.net.Uri
import com.example.documentsend.utils.FileUtils

sealed class SendContent {
    data class Text(val message: String) : SendContent()
    
    data class File(
        val fileName: String,
        val fileLength: Long,
        val inputStream: java.io.InputStream,
        val offset: Long = 0,
        val historyId: Int? = null
    ) : SendContent()

    companion object {
        fun fromText(message: String): SendContent = Text(message)

        fun fromUri(context: Context, uri: Uri, offset: Long = 0, historyId: Int? = null): SendContent? {
            val fileName = FileUtils.getFileName(context, uri)
            val fileSize = FileUtils.getFileSize(context, uri)
            val inputStream = FileUtils.getInputStream(context, uri) ?: return null
            return File(
                fileName = fileName,
                fileLength = fileSize,
                inputStream = inputStream,
                offset = offset,
                historyId = historyId
            )
        }
    }
}
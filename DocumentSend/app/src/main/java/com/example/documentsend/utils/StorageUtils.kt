package com.example.documentsend.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object StorageUtils {

    /** 检查是否有存储权限（Android 11+ 需要 MANAGE_EXTERNAL_STORAGE） */
    fun hasStoragePermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun getDownloadDir(context: Context): File {
        val dir = NetworkConfigUtils.getCurrentSavePath(context)
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun getUniqueFile(dir: File, fileName: String): File {
        val file = File(dir, fileName)
        if (!file.exists()) return file

        val nameWithoutExt = fileName.substringBeforeLast(".")
        val ext = fileName.substringAfterLast(".", "")

        var counter = 1
        var newFile: File
        do {
            val newName = if (ext.isNotEmpty()) "${nameWithoutExt}(${counter}).${ext}" else "${nameWithoutExt}(${counter})"
            newFile = File(dir, newName)
            counter++
        } while (newFile.exists())

        return newFile
    }

    fun formatFileSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
        }
    }

    fun getPartialFile(dir: File, fileName: String): File {
        return File(dir, "${fileName}.partial")
    }

    fun deletePartial(dir: File, fileName: String) {
        getPartialFile(dir, fileName).delete()
    }

    fun renameToFinal(dir: File, fileName: String): File {
        val partial = getPartialFile(dir, fileName)
        val finalFile = getUniqueFile(dir, fileName)
        partial.renameTo(finalFile)
        return finalFile
    }

    fun hasPartialFile(dir: File, fileName: String): Boolean {
        return getPartialFile(dir, fileName).exists()
    }
}

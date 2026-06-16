package com.example.documentsend.utils

import java.io.DataInputStream

object StreamUtils {
    fun skipBytesFully(dis: DataInputStream, bytesToSkip: Long) {
        var remaining = bytesToSkip
        while (remaining > 0) {
            val skipped = dis.skip(remaining)
            if (skipped <= 0) break
            remaining -= skipped
        }
    }
}

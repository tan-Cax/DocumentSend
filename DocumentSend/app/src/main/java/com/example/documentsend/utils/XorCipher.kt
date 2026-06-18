package com.example.documentsend.utils

object XorCipher {
    private val key = "gfh98H6dEG6H47d".toByteArray(Charsets.UTF_8)

    fun xor(data: ByteArray, offset: Int, length: Int) {
        for (i in offset until offset + length) {
            data[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
        }
    }
}

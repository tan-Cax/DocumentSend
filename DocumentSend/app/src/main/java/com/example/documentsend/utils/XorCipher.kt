package com.example.documentsend.utils

object XorCipher {
    private val key = "gfh98H6dEG6H47d".toByteArray(Charsets.UTF_8)

    // fun xor(data: ByteArray, offset: Int, length: Int) {
    //     for (i in offset until offset + length) {
    //         data[i] = (data[i].toInt() xor key[i % key.size].toInt()).toByte()
    //     }
    // }

    /**
     * @param streamOffset 当前 chunk 第一个字节在完整数据流中的绝对位置
     */
    fun xor(data: ByteArray, offset: Int, length: Int, streamOffset: Long = 0) {
        for (i in offset until offset + length) {
            val keyIndex = ((streamOffset + (i - offset)) % key.size).toInt()
            data[i] = (data[i].toInt() xor key[keyIndex].toInt()).toByte()
        }
    }
}

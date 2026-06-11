package com.example.documentsend.network

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException

data class PacketHeader(
    var type: Byte = 0,
    var nameLength: Short = 0,
    var bodyLength: Long = 0,
    var offset: Long = 0,
    var totalLength: Long = 0
) {
    companion object {
        const val MAGIC_NUMBER: Short = 0xCAFE.toShort()
        const val HEADER_LENGTH: Int = 29

        /**
         * 从输入流中读取 29 字节并解析为报头对象
         */
        @Throws(IOException::class)
        fun readFrom(dis: DataInputStream): PacketHeader {
            val magic = dis.readShort()
            if (magic != MAGIC_NUMBER) {
                throw IOException("Magic Number mismatch: expected 0xCAFE, got 0x" + Integer.toHexString(magic.toInt() and 0xFFFF))
            }

            return PacketHeader(
                type = dis.readByte(),
                nameLength = dis.readShort(),
                bodyLength = dis.readLong(),
                offset = dis.readLong(),
                totalLength = dis.readLong()
            )
        }
    }

    /**
     * 将当前报头数据写入输出流 (以供发送端使用)
     */
    @Throws(IOException::class)
    fun writeTo(dos: DataOutputStream) {
        dos.writeShort(MAGIC_NUMBER.toInt())
        dos.writeByte(type.toInt())
        dos.writeShort(nameLength.toInt())
        dos.writeLong(bodyLength)
        dos.writeLong(offset)
        dos.writeLong(totalLength)
    }
}


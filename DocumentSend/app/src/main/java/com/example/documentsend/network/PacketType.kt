package com.example.documentsend.network

enum class PacketType(val value: Byte) {
    TEXT(1),
    IMAGE(2),
    VIDEO(3),
    FILE(4),
    ARCHIVE(5);

    companion object {
        fun fromValue(value: Byte): PacketType? {
            return values().find { it.value == value }
        }
    }
}


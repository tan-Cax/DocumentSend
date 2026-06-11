package com.example.documentsend.data

import androidx.room.Entity
import androidx.room.PrimaryKey

class HistoryType {
    companion object {
        const val SEND = "send"
        const val RECEIVE = "receive"
    }
}

@Entity
data class History(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
    val uriString: String = "",
    val filetypeString: String = "",
    val typeString: String = HistoryType.SEND,
    val targetIp: String = "",
    val offset: Long = 0,
    val totalLength: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val text: String = ""
)

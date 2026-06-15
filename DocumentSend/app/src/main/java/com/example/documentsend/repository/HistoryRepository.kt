package com.example.documentsend.repository

import com.example.documentsend.data.History
import com.example.documentsend.data.HistoryDao

class HistoryRepository(private val dao: HistoryDao) {

    fun getAllHistory() = dao.getAllHistory()

    suspend fun insertHistory(history: History): Long {
        return dao.insertHistory(history)
    }

    suspend fun updateUri(historyId: Int, uriString: String) {
        dao.updateHistoryUri(historyId, uriString)
    }

    suspend fun deleteHistory(historyId: Int) {
        dao.deleteHistory(historyId)
    }

    suspend fun getUnfinishedTransfer(fileName: String): History? {
        return dao.getUnfinishedTransfer(fileName)
    }

    suspend fun getUnfinishedTransfer(fileName: String, targetIp: String): History? {
        return dao.getUnfinishedTransfer(fileName, targetIp)
    }

    suspend fun updateOffset(historyId: Int, offset: Long) {
        dao.updateHistoryOffset(historyId, offset)
    }

    suspend fun getHistoryById(id: Int): History? {
        return dao.getHistoryById(id)
    }

    fun getSendHistory() = dao.getSendHistory()

    fun getReceiveHistory() = dao.getReceiveHistory()
}

package com.example.documentsend.repository

import com.example.documentsend.data.History
import com.example.documentsend.data.HistoryDao
import com.example.documentsend.log.Logger

class HistoryRepository(private val dao: HistoryDao) {

    fun getAllHistory() = dao.getAllHistory()

    suspend fun insertHistory(history: History): Long {
        val id = dao.insertHistory(history)
        Logger.logInfo("Database", "InsertHistory", "插入记录: name=${history.name}, type=${history.typeString}, id=$id")
        return id
    }

    suspend fun updateUri(historyId: Int, uriString: String) {
        dao.updateHistoryUri(historyId, uriString)
        Logger.logInfo("Database", "UpdateUri", "更新URI: id=$historyId")
    }

    suspend fun deleteHistory(historyId: Int) {
        dao.deleteHistory(historyId)
        Logger.logInfo("Database", "DeleteHistory", "删除记录: id=$historyId")
    }

    suspend fun deleteAllHistory() {
        dao.deleteAllHistory()
        Logger.logInfo("Database", "DeleteAllHistory", "清空所有历史记录")
    }

    suspend fun getUnfinishedTransfer(fileName: String): History? {
        val result = dao.getUnfinishedTransfer(fileName)
        Logger.logInfo("Database", "QueryUnfinished", "查询未完成记录: file=$fileName, found=${result != null}")
        return result
    }

    suspend fun getUnfinishedTransfer(fileName: String, targetIp: String): History? {
        val result = dao.getUnfinishedTransfer(fileName, targetIp)
        Logger.logInfo("Database", "QueryUnfinished", "查询未完成记录: file=$fileName, ip=$targetIp, found=${result != null}")
        return result
    }

    suspend fun updateOffset(historyId: Int, offset: Long) {
        dao.updateHistoryOffset(historyId, offset)
        Logger.logInfo("Database", "UpdateOffset", "更新offset: id=$historyId, offset=$offset")
    }

    suspend fun getHistoryById(id: Int): History? {
        return dao.getHistoryById(id)
    }

    fun getSendHistory() = dao.getSendHistory()

    fun getReceiveHistory() = dao.getReceiveHistory()
}

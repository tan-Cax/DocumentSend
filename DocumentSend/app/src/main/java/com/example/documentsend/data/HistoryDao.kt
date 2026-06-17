package com.example.documentsend.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM History ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<History>>

    @Insert
    suspend fun insertHistory(history: History): Long

    @Query("UPDATE History SET uriString = :uriString WHERE id = :historyId")
    suspend fun updateHistoryUri(historyId: Int, uriString: String)

    @Query("DELETE FROM History WHERE id = :historyId")
    suspend fun deleteHistory(historyId: Int)

    @Query("DELETE FROM History")
    suspend fun deleteAllHistory()

    @Query("SELECT * FROM History WHERE name = :fileName AND `offset` < totalLength LIMIT 1")
    suspend fun getUnfinishedTransfer(fileName: String): History?

    @Query("SELECT * FROM History WHERE name = :fileName AND targetIp = :targetIp AND `offset` < totalLength LIMIT 1")
    suspend fun getUnfinishedTransfer(fileName: String, targetIp: String): History?

    @Query("SELECT * FROM History WHERE id = :id LIMIT 1")
    suspend fun getHistoryById(id: Int): History?

    @Query("UPDATE History SET `offset` = :offset WHERE id = :historyId")
    suspend fun updateHistoryOffset(historyId: Int, offset: Long)

    @Query("SELECT * FROM History WHERE typeString = 'send' ORDER BY timestamp DESC")
    fun getSendHistory(): Flow<List<History>>

    @Query("SELECT * FROM History WHERE typeString = 'receive' ORDER BY timestamp DESC")
    fun getReceiveHistory(): Flow<List<History>>
}

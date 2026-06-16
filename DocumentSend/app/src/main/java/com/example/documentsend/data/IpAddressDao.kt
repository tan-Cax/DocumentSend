package com.example.documentsend.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IpAddressDao {

    @Query("SELECT * FROM IpAddress")
    fun getAllIpAddresses(): Flow<List<IpAddress>>

    @Query("SELECT COUNT(*) FROM IpAddress WHERE ip = :ip")
    suspend fun countByIp(ip: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIPAddress(ipAddress: IpAddress)

    @Update
    suspend fun updateIPAddress(ipAddress: IpAddress)
}
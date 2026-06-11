package com.example.documentsend.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface IpAddressDao {

    @Query("SELECT * FROM IpAddress")
    fun getAllIpAddresses(): Flow<List<IpAddress>>

    @Insert
    suspend fun insertIPAddress(ipAddress: IpAddress)

    @Update
    suspend fun updateIPAddress(ipAddress: IpAddress)
}
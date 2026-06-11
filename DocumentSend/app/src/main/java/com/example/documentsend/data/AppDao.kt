package com.example.documentsend.data

import androidx.room.Dao

@Dao
interface AppDao : IpAddressDao, HistoryDao

package com.example.documentsend.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
data class IpAddress (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ip: String
)

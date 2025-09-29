package com.example.telegramWallet.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "states")
data class StatesEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ColumnInfo(name = "channel") val channel: Long,
    @ColumnInfo(name = "data") val data: String,
)

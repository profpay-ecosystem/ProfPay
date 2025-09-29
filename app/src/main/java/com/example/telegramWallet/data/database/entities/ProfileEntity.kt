package com.example.telegramWallet.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "profile",
    indices = [
        Index(
            value = ["telegram_id"],
            unique = true,
        ),
        Index(
            value = ["device_token"],
            unique = true,
        ),
    ],
)
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
    @ColumnInfo(name = "user_id") val userId: Long? = null,
    @ColumnInfo(name = "app_id") val appId: String? = null,
    @ColumnInfo(name = "device_token") val deviceToken: String? = null,
    @ColumnInfo(name = "telegram_id") val telegramId: Long? = null,
    @ColumnInfo(name = "username") val username: String? = null,
    @ColumnInfo(name = "active_tg_id") val activeTgId: Boolean = false,
    @ColumnInfo(name = "access_token") var accessToken: String? = null,
    @ColumnInfo(name = "expires_at") var expiresAt: Long? = null,
)

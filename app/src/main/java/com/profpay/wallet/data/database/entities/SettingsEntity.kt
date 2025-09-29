package com.profpay.wallet.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey(autoGenerate = true) var id: Long? = null,
    @ColumnInfo(name = "token_bot") var tokenBot: String,
    @ColumnInfo(name = "language_code", defaultValue = "ru") var languageCode: String = "ru",
    @ColumnInfo(name = "active_bot") var activeBot: Boolean = false,
    @ColumnInfo(name = "auto_aml") var autoAml: Boolean = false,
)

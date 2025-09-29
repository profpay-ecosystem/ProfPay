package com.profpay.wallet.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.profpay.wallet.data.database.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(entity = SettingsEntity::class)
    fun insertNewSettings(settings: SettingsEntity)

    @Query("UPDATE settings SET token_bot = :botToken")
    fun updateBotToken(botToken: String)

    @Query("UPDATE settings SET active_bot = :active")
    fun updateActiveBot(active: Boolean)

    @Query("SELECT COUNT(*) FROM settings")
    fun getCountRecordSettings(): Int

    @Query("SELECT language_code FROM settings LIMIT 1")
    fun getLanguageCode(): String

    @Query("SELECT * FROM settings")
    fun getSettings(): SettingsEntity?

    @Query("SELECT * FROM settings")
    fun getSettingsForVM(): Flow<SettingsEntity>

    @Query("UPDATE settings SET auto_aml = :autoAML")
    fun updateAutoAML(autoAML: Boolean)
}

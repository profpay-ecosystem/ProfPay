package com.profpay.wallet.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.profpay.wallet.data.database.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Insert(entity = SettingsEntity::class)
    suspend fun insertNewSettings(settings: SettingsEntity)

    @Query("SELECT EXISTS(SELECT 1 FROM settings)")
    suspend fun isSettingsExists(): Boolean

    @Query("UPDATE settings SET token_bot = :botToken")
    suspend fun updateBotToken(botToken: String)

    @Query("UPDATE settings SET active_bot = :active")
    suspend fun updateActiveBot(active: Boolean)

    @Query("SELECT COUNT(*) FROM settings")
    suspend fun getCountRecordSettings(): Int

    @Query("SELECT language_code FROM settings LIMIT 1")
    suspend fun getLanguageCode(): String

    @Query("SELECT * FROM settings")
    suspend fun getSettings(): SettingsEntity?

    @Query("SELECT * FROM settings")
    fun getSettingsFlow(): Flow<SettingsEntity>

    @Query("UPDATE settings SET auto_aml = :autoAML")
    suspend fun updateAutoAML(autoAML: Boolean)
}

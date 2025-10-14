package com.profpay.wallet.data.database.repositories

import com.profpay.wallet.data.database.dao.SettingsDao
import com.profpay.wallet.data.database.entities.SettingsEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface SettingsRepo {
    suspend fun insertNewSettings(settingsEntity: SettingsEntity)

    suspend fun getLanguageCode(): String

    suspend fun getSettings(): SettingsEntity?

    suspend fun getCountRecordSettings(): Int

    fun getSettingsFlow(): Flow<SettingsEntity>

    suspend fun updateActiveBot(active: Boolean)

    suspend fun updateBotToken(botToken: String)

    suspend fun updateAutoAML(autoAML: Boolean)

    suspend fun isSettingsExists(): Boolean
}

@Singleton
class SettingsRepoImpl @Inject constructor(
    private val settingsDao: SettingsDao,
) : SettingsRepo {
    /**
     * Для изменения(добавления) любых данных в таблицу необходимо использовать эту функцию
     */
    override suspend fun insertNewSettings(settingsEntity: SettingsEntity) {
        if (!isSettingsExists()) {
            settingsDao.insertNewSettings(settingsEntity)
        }
    }

    override suspend fun getCountRecordSettings(): Int =
        settingsDao.getCountRecordSettings()

    override suspend fun getSettings(): SettingsEntity? =
        settingsDao.getSettings()

    override suspend fun getLanguageCode(): String =
        settingsDao.getLanguageCode()

    override fun getSettingsFlow(): Flow<SettingsEntity> =
        settingsDao.getSettingsFlow()

    override suspend fun updateActiveBot(active: Boolean) =
        settingsDao.updateActiveBot(active)

    override suspend fun updateBotToken(botToken: String) =
        settingsDao.updateBotToken(botToken)

    override suspend fun updateAutoAML(autoAML: Boolean) =
        settingsDao.updateAutoAML(autoAML)

    override suspend fun isSettingsExists(): Boolean =
        settingsDao.isSettingsExists()
}

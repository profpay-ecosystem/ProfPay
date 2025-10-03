package com.profpay.wallet.data.database.repositories

import com.profpay.wallet.data.database.dao.SettingsDao
import com.profpay.wallet.data.database.entities.SettingsEntity
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface SettingsRepo {
    suspend fun insertNewSettings(settingsEntity: SettingsEntity)

    suspend fun getLanguageCode(): String

    suspend fun getSettings(): SettingsEntity?

    suspend fun getCountRecordSettings(): Int

    suspend fun getSettingsForVM(): Flow<SettingsEntity>

    suspend fun updateActiveBot(active: Boolean)

    suspend fun updateBotToken(botToken: String)

    suspend fun updateAutoAML(autoAML: Boolean)
}

@Singleton
class SettingsRepoImpl
    @Inject
    constructor(
        private val settingsDao: SettingsDao,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : SettingsRepo {
        /**
         * Для изменения(добавления) любых данных в таблицу необходимо использовать эту функцию
         */
        override suspend fun insertNewSettings(settingsEntity: SettingsEntity) {
            withContext(ioDispatcher) {
                when (settingsDao.getCountRecordSettings()) {
                    0 -> {
                        settingsDao.insertNewSettings(settingsEntity)
                    }
                }
            }
        }

        override suspend fun getCountRecordSettings(): Int {
            return withContext(ioDispatcher) {
                return@withContext settingsDao.getCountRecordSettings()
            }
        }

        override suspend fun getSettings(): SettingsEntity? {
            return withContext(ioDispatcher) {
                return@withContext settingsDao.getSettings()
            }
        }

        override suspend fun getLanguageCode(): String {
            return withContext(ioDispatcher) {
                return@withContext settingsDao.getLanguageCode()
            }
        }

        override suspend fun getSettingsForVM(): Flow<SettingsEntity> {
            return withContext(ioDispatcher) {
                return@withContext settingsDao.getSettingsForVM()
            }
        }

        override suspend fun updateActiveBot(active: Boolean) {
            withContext(ioDispatcher) {
                settingsDao.updateActiveBot(active)
            }
        }

        override suspend fun updateBotToken(botToken: String) {
            withContext(ioDispatcher) {
                settingsDao.updateBotToken(botToken)
            }
        }

        override suspend fun updateAutoAML(autoAML: Boolean) {
            withContext(ioDispatcher) {
                settingsDao.updateAutoAML(autoAML)
            }
        }
    }

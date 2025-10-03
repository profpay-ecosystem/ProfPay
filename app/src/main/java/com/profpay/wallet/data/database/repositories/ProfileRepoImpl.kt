package com.profpay.wallet.data.database.repositories

import androidx.lifecycle.LiveData
import com.profpay.wallet.bridge.view_model.dto.ProfileDto
import com.profpay.wallet.data.database.dao.ProfileDao
import com.profpay.wallet.data.database.entities.ProfileEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
interface ProfileRepo {
    suspend fun getProfileUserId(): Long

    suspend fun getProfileAppId(): String

    // Создание профиля аккаунта
    suspend fun insertNewProfile(profileEntity: ProfileEntity)

    // Получение TgId подтверждённого профиля
    suspend fun getProfileTelegramIdLiveData(): LiveData<Long>

    suspend fun getProfileTelegramId(): Long?

    suspend fun getProfileTgUsername(): LiveData<String>

    // Получение кол-ва профилей в бд
    suspend fun isProfileExists(): Boolean

    // Получение данных неподтверждённого профиля для UI
    suspend fun getInactiveProfileFromVM(): Flow<ProfileDto>

    // Получение данных подтверждённого профиля для UI
    suspend fun getActiveProfileFromVM(): Flow<ProfileDto>

    // Удаление профиля по Tg-Id
    suspend fun deleteProfileByTgId(tgId: Long)

    // Получение данных подтверждённого профиля
    suspend fun getActiveProfile(): ProfileDto

    // Обновление активности профиля и access-token с временем его жизни
    suspend fun updateActiveTgId(
        valActive: Boolean,
        tgId: Long,
        accessToken: String,
        expiresAt: Long,
    )

    suspend fun updateProfileTelegramIdAndUsername(
        telegramId: Long,
        username: String,
    )

    suspend fun getDeviceToken(): String?

    suspend fun updateDeviceToken(deviceToken: String)

    suspend fun updateUserId(userId: Long)
}

@Singleton
class ProfileRepoImpl
    @Inject
    constructor(
        private val profileDao: ProfileDao,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : ProfileRepo {
        override suspend fun getProfileAppId(): String {
            return withContext(dispatcher) {
                return@withContext profileDao.getProfileAppId()
            }
        }

        override suspend fun getProfileUserId(): Long {
            return withContext(dispatcher) {
                return@withContext profileDao.getProfileUserId()
            }
        }

        override suspend fun insertNewProfile(profileEntity: ProfileEntity) {
            withContext(dispatcher) {
                profileDao.insertNewProfile(profileEntity)
            }
        }

        override suspend fun getProfileTelegramIdLiveData(): LiveData<Long> {
            return withContext(dispatcher) {
                return@withContext profileDao.getProfileTelegramIdLiveData()
            }
        }

        override suspend fun getProfileTelegramId(): Long? {
            return withContext(dispatcher) {
                return@withContext profileDao.getProfileTelegramId()
            }
        }

        override suspend fun getProfileTgUsername(): LiveData<String> {
            return withContext(dispatcher) {
                return@withContext profileDao.getProfileTgUsername()
            }
        }

        override suspend fun isProfileExists(): Boolean {
            return withContext(dispatcher) {
                return@withContext profileDao.isProfileExists()
            }
        }

        override suspend fun updateActiveTgId(
            valActive: Boolean,
            tgId: Long,
            accessToken: String,
            expiresAt: Long,
        ) {
            withContext(dispatcher) {
                profileDao.updateActiveTgId(valActive, tgId, accessToken, expiresAt)
            }
        }

        override suspend fun updateProfileTelegramIdAndUsername(
            telegramId: Long,
            username: String,
        ) {
            return withContext(dispatcher) {
                return@withContext profileDao.updateProfileTelegramIdAndUsername(telegramId, username)
            }
        }

        override suspend fun getDeviceToken(): String? {
            return withContext(dispatcher) {
                return@withContext profileDao.getDeviceToken()
            }
        }

        override suspend fun updateDeviceToken(deviceToken: String) {
            return withContext(dispatcher) {
                return@withContext profileDao.updateDeviceToken(deviceToken)
            }
        }

        override suspend fun updateUserId(userId: Long) {
            return withContext(dispatcher) {
                return@withContext profileDao.updateUserId(userId)
            }
        }

        override suspend fun getInactiveProfileFromVM(): Flow<ProfileDto> {
            return withContext(dispatcher) {
                return@withContext profileDao.getInactiveProfileFromVM()
            }
        }

        override suspend fun getActiveProfileFromVM(): Flow<ProfileDto> {
            return withContext(dispatcher) {
                return@withContext profileDao.getActiveProfileFromVM()
            }
        }

        override suspend fun deleteProfileByTgId(tgId: Long) {
            withContext(dispatcher) {
                profileDao.deleteProfileByTgId(tgId)
            }
        }

        override suspend fun getActiveProfile(): ProfileDto {
            return withContext(dispatcher) {
                return@withContext profileDao.getActiveProfile()
            }
        }
    }

package com.profpay.wallet.data.database.repositories

import com.profpay.wallet.data.database.dao.ProfileDao
import com.profpay.wallet.data.database.entities.ProfileEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

interface ProfileRepo {
    suspend fun getProfileUserId(): Long

    suspend fun getProfileAppId(): String

    // Создание профиля аккаунта
    suspend fun insertNewProfile(profileEntity: ProfileEntity)

    // Получение TgId подтверждённого профиля
    fun getProfileTelegramIdFlow(): Flow<Long?>

    suspend fun getProfileTelegramId(): Long?

    fun getProfileTgUsernameFlow(): Flow<String?>

    // Получение кол-ва профилей в бд
    suspend fun isProfileExists(): Boolean

    // Удаление профиля по Tg-Id
    suspend fun deleteProfileByTgId(tgId: Long)

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
class ProfileRepoImpl @Inject constructor(
    private val profileDao: ProfileDao,
) : ProfileRepo {
    override suspend fun getProfileAppId(): String =
        profileDao.getProfileAppId()

    override suspend fun getProfileUserId(): Long =
        profileDao.getProfileUserId()

    override suspend fun insertNewProfile(profileEntity: ProfileEntity) =
        profileDao.insertNewProfile(profileEntity)

    override fun getProfileTelegramIdFlow(): Flow<Long?> =
        profileDao.getProfileTelegramIdFlow()

    override suspend fun getProfileTelegramId(): Long? =
        profileDao.getProfileTelegramId()

    override fun getProfileTgUsernameFlow(): Flow<String?> =
        profileDao.getProfileTgUsernameFlow()

    override suspend fun isProfileExists(): Boolean =
        profileDao.isProfileExists()

    override suspend fun updateActiveTgId(
        valActive: Boolean,
        tgId: Long,
        accessToken: String,
        expiresAt: Long,
    ) = profileDao.updateActiveTgId(valActive, tgId, accessToken, expiresAt)

    override suspend fun updateProfileTelegramIdAndUsername(
        telegramId: Long,
        username: String,
    ) = profileDao.updateProfileTelegramIdAndUsername(telegramId, username)

    override suspend fun getDeviceToken(): String? =
        profileDao.getDeviceToken()

    override suspend fun updateDeviceToken(deviceToken: String) =
        profileDao.updateDeviceToken(deviceToken)

    override suspend fun updateUserId(userId: Long) =
        profileDao.updateUserId(userId)

    override suspend fun deleteProfileByTgId(tgId: Long) =
        profileDao.deleteProfileByTgId(tgId)
}

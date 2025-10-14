package com.profpay.wallet.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.profpay.wallet.data.database.entities.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Insert(entity = ProfileEntity::class)
    suspend fun insertNewProfile(profile: ProfileEntity)

    @Query("SELECT user_id FROM profile")
    suspend fun getProfileUserId(): Long

    @Query("SELECT app_id FROM profile")
    suspend fun getProfileAppId(): String

    @Query("SELECT telegram_id FROM profile")
    fun getProfileTelegramIdFlow(): Flow<Long?>

    @Query("SELECT telegram_id FROM profile")
    suspend fun getProfileTelegramId(): Long?

    @Query("SELECT username FROM profile")
    fun getProfileTgUsernameFlow(): Flow<String?>

    @Query("SELECT EXISTS(SELECT 1 FROM profile)")
    suspend fun isProfileExists(): Boolean

    @Query("DELETE FROM profile WHERE telegram_id = :tgId")
    suspend fun deleteProfileByTgId(tgId: Long)

    @Query("UPDATE profile SET active_tg_id = :valActive, access_token = :accessToken, expires_at = :expiresAt WHERE telegram_id = :tgId")
    suspend fun updateActiveTgId(
        valActive: Boolean,
        tgId: Long,
        accessToken: String,
        expiresAt: Long,
    )

    @Query("SELECT active_tg_id FROM profile")
    fun isActiveTgId(): Flow<Boolean>

    @Query("UPDATE profile SET telegram_id = :telegramId, username = :username")
    suspend fun updateProfileTelegramIdAndUsername(
        telegramId: Long,
        username: String,
    )

    @Query("SELECT device_token FROM profile")
    suspend fun getDeviceToken(): String?

    @Query("UPDATE profile SET device_token = :deviceToken")
    suspend fun updateDeviceToken(deviceToken: String)

    @Query("UPDATE profile SET user_id = :userId")
    suspend fun updateUserId(userId: Long)
}

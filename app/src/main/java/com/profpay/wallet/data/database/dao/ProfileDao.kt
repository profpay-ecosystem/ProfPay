package com.profpay.wallet.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.profpay.wallet.bridge.view_model.dto.ProfileDto
import com.profpay.wallet.data.database.entities.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Insert(entity = ProfileEntity::class)
    fun insertNewProfile(profile: ProfileEntity)

    @Query("SELECT user_id FROM profile")
    fun getProfileUserId(): Long

    @Query("SELECT app_id FROM profile")
    fun getProfileAppId(): String

    @Query("SELECT telegram_id FROM profile")
    fun getProfileTelegramIdLiveData(): LiveData<Long>

    @Query("SELECT telegram_id FROM profile")
    fun getProfileTelegramId(): Long?

    @Query("SELECT username FROM profile")
    fun getProfileTgUsername(): LiveData<String>

    @Query("SELECT EXISTS(SELECT 1 FROM profile)")
    fun isProfileExists(): Boolean

    @Query("SELECT telegram_id, active_tg_id, username FROM profile WHERE active_tg_id = 0")
    fun getInactiveProfileFromVM(): Flow<ProfileDto>

    @Query("SELECT telegram_id, active_tg_id, username, access_token, expires_at FROM profile WHERE active_tg_id = 1")
    fun getActiveProfileFromVM(): Flow<ProfileDto>

    @Query("DELETE FROM profile WHERE telegram_id = :tgId")
    fun deleteProfileByTgId(tgId: Long)

    @Query("UPDATE profile SET active_tg_id = :valActive, access_token = :accessToken, expires_at = :expiresAt WHERE telegram_id = :tgId")
    fun updateActiveTgId(
        valActive: Boolean,
        tgId: Long,
        accessToken: String,
        expiresAt: Long,
    )

    @Query("SELECT active_tg_id FROM profile")
    fun isActiveTgId(): Flow<Boolean>

    @Query("SELECT telegram_id, active_tg_id, username, access_token, expires_at FROM profile WHERE active_tg_id = 1")
    fun getActiveProfile(): ProfileDto

    @Query("UPDATE profile SET telegram_id = :telegramId, username = :username")
    fun updateProfileTelegramIdAndUsername(
        telegramId: Long,
        username: String,
    )

    @Query("SELECT device_token FROM profile")
    fun getDeviceToken(): String?

    @Query("UPDATE profile SET device_token = :deviceToken")
    fun updateDeviceToken(deviceToken: String)

    @Query("UPDATE profile SET user_id = :userId")
    fun updateUserId(userId: Long)
}

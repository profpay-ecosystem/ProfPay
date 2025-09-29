package com.profpay.wallet.bridge.view_model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.bridge.view_model.dto.ProfileDto
import com.profpay.wallet.data.database.repositories.ProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RequestNewTelegramAcViewModel
    @Inject
    constructor(
        private val profileRepo: ProfileRepo,
    ) : ViewModel() {
        // Получаем Flow с данными неподтверждённого Telegram-профиля
        suspend fun inactiveProfile(): Flow<ProfileDto> = profileRepo.getInactiveProfileFromVM()

        // Получаем Flow с данными подтверждённого Telegram-профиль
        suspend fun activeProfile(): Flow<ProfileDto> = profileRepo.getActiveProfileFromVM()

        // Удаляем Telegram-профиль по Telegram-ID
        fun deleteProfileById(tgId: Long) {
            viewModelScope.launch {
                profileRepo.deleteProfileByTgId(tgId)
            }
        }

        // Обновляем активность Tg-профиля, access-токен и время жизни токена по Telegram-ID
        fun updateActiveBot(
            isActive: Boolean,
            tgId: Long,
            accessToken: String,
            expiresAt: Long,
        ) {
            viewModelScope.launch {
                profileRepo.updateActiveTgId(isActive, tgId, accessToken, expiresAt)
            }
        }
    }

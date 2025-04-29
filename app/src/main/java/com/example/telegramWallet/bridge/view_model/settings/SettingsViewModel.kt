package com.example.telegramWallet.bridge.view_model.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.data.database.entities.SettingsEntity
import com.example.telegramWallet.data.database.repositories.SettingsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepo,
    @ApplicationContext val applicationContext: Context,
) : ViewModel() {
    /**
     * Внесение новых данных настроек, обновление существующих
     */
    fun insertNewSettings(settingsEntity: SettingsEntity) {
        viewModelScope.launch {
            settingsRepo.insertNewSettings(settingsEntity)
        }
    }

    // Обновление токена-бота
    fun updateTokenBot(tokenBot: String) {
        viewModelScope.launch {
            settingsRepo.updateBotToken(tokenBot)
        }
    }

    // Обновление авто-AML true\false
    fun updateAutoAML(autoAML: Boolean) {
        viewModelScope.launch {
            settingsRepo.updateAutoAML(autoAML)
        }
    }

    // Получение данных настроек в формате Flow
    suspend fun getSettingsForVM(): Flow<SettingsEntity> {
        return settingsRepo.getSettingsForVM()
    }

    // Получение токена Tg-бота, если его пока нет возвращается пустая строка
    suspend fun getBotToken(): String {
        return if (settingsRepo.getSettings() != null) {
            settingsRepo.getSettings()!!.tokenBot
        } else ""
    }
}
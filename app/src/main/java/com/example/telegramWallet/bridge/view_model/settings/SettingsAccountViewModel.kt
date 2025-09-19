package com.example.telegramWallet.bridge.view_model.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.flow_db.repo.SettingsAccountRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsAccountViewModel @Inject constructor(
    private val profileRepo: ProfileRepo,
    private val settingsAccountRepo: SettingsAccountRepo
) : ViewModel() {
    val profileTelegramId: LiveData<Long> = liveData(Dispatchers.IO) {
        emitSource(profileRepo.getProfileTelegramIdLiveData())
    }

    val profileTelegramUsername: LiveData<String> = liveData(Dispatchers.IO) {
        emitSource(profileRepo.getProfileTgUsername())
    }

    suspend fun getProfileUserId(): Long {
        return withContext(Dispatchers.IO) {
            profileRepo.getProfileUserId()
        }
    }

    suspend fun getProfileAppId(): String {
        return withContext(Dispatchers.IO) {
            return@withContext profileRepo.getProfileAppId()
        }
    }

    suspend fun getUserTelegramData() {
        settingsAccountRepo.getUserTelegramData()
    }
}
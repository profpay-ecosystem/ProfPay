package com.profpay.wallet.bridge.view_model.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import com.profpay.wallet.data.di.module.MainDispatcher
import com.profpay.wallet.data.repository.SettingsAccountRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsAccountViewModel @Inject constructor(
    private val profileRepo: ProfileRepo,
    private val settingsAccountRepo: SettingsAccountRepo,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    @param:MainDispatcher private val mainDispatcher: CoroutineDispatcher,
) : ViewModel() {
    val profileTelegramId: LiveData<Long?> =
        liveData(ioDispatcher) {
            emitSource(profileRepo.getProfileTelegramIdFlow().asLiveData())
        }

    val profileTelegramUsername: LiveData<String?> =
        liveData(ioDispatcher) {
            emitSource(profileRepo.getProfileTgUsernameFlow().asLiveData())
        }

    fun loadUserAndAppIds(
        onLoaded: (userId: Long, appId: String) -> Unit,
        onError: (Throwable) -> Unit = {}
    ) = viewModelScope.launch(ioDispatcher) {
        try {
            val userId = profileRepo.getProfileUserId()
            val appId = profileRepo.getProfileAppId()
            withContext(mainDispatcher) {
                onLoaded(userId, appId)
            }
        } catch (e: Exception) {
            onError(e)
        }
    }

    fun getUserTelegramData() = viewModelScope.launch {
        settingsAccountRepo.getUserTelegramData()
    }
}

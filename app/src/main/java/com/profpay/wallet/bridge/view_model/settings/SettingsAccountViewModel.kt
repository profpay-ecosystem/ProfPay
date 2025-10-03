package com.profpay.wallet.bridge.view_model.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.flow_db.repo.SettingsAccountRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SettingsAccountViewModel
    @Inject
    constructor(
        private val profileRepo: ProfileRepo,
        private val settingsAccountRepo: SettingsAccountRepo,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : ViewModel() {
        val profileTelegramId: LiveData<Long> =
            liveData(dispatcher) {
                emitSource(profileRepo.getProfileTelegramIdLiveData())
            }

        val profileTelegramUsername: LiveData<String> =
            liveData(dispatcher) {
                emitSource(profileRepo.getProfileTgUsername())
            }

        suspend fun getProfileUserId(): Long =
            withContext(dispatcher) {
                profileRepo.getProfileUserId()
            }

        suspend fun getProfileAppId(): String {
            return withContext(dispatcher) {
                return@withContext profileRepo.getProfileAppId()
            }
        }

        suspend fun getUserTelegramData() {
            settingsAccountRepo.getUserTelegramData()
        }
    }

package com.profpay.wallet.data.repository.flow

import android.content.Context
import androidx.core.content.ContextCompat
import com.profpay.wallet.R
import com.profpay.wallet.data.di.module.IoDispatcher
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface BlockingAppRepo {
    val isBlockedApp: Flow<BlockingAppRepoState>

    suspend fun getBlockedAppState()
}

class BlockingAppRepoImpl
    @Inject
    constructor(
        @param:ApplicationContext val appContext: Context,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : BlockingAppRepo {
        private val _isBlockedApp = MutableSharedFlow<BlockingAppRepoState>(replay = 1)

        // Получение текущего состояния статуса блокировки приложения
        override val isBlockedApp: Flow<BlockingAppRepoState> =
            _isBlockedApp.asSharedFlow()

        // Триггер обновления статуса
        override suspend fun getBlockedAppState() {
            withContext(ioDispatcher) {
                val sharedPref =
                    appContext.getSharedPreferences(
                        ContextCompat.getString(appContext, R.string.preference_file_key),
                        Context.MODE_PRIVATE,
                    )
                val isBlockedApp = sharedPref.getBoolean("is_blocked_app", false)
                _isBlockedApp.emit(BlockingAppRepoState(isBlockedApp))
            }
        }
    }

data class BlockingAppRepoState(
    val isBlockedApp: Boolean,
)

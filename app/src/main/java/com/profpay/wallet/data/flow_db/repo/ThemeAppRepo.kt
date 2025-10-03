package com.profpay.wallet.data.flow_db.repo

import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ThemeAppRepo {
    val isDarkTheme: Flow<Int>

    suspend fun isDarkTheme(shared: SharedPreferences)
}

class ThemeAppRepoImpl
    @Inject
    constructor(private val dispatcher: CoroutineDispatcher) : ThemeAppRepo {
        private val _isDarkTheme = MutableSharedFlow<Int>(replay = 1)

        // Получение числового значения текущей темы приложения
        override val isDarkTheme: Flow<Int> =
            _isDarkTheme.asSharedFlow()

        // Триггер на обновление числового значения темы приложения
        override suspend fun isDarkTheme(shared: SharedPreferences) {
            withContext(dispatcher) {
                val isDarkTheme = themeShared(shared)
                _isDarkTheme.emit(isDarkTheme)
            }
        }

        private fun themeShared(shared: SharedPreferences): Int = shared.getInt("valueTheme", 2)
    }

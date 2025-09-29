package com.profpay.wallet.bridge.view_model.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.flow_db.repo.ThemeAppRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel
    @Inject
    constructor(
        private val themeAppRepo: ThemeAppRepo,
    ) : ViewModel() {
        // Получаем true\false в зависимости тёмная\светлая тема
        fun isDarkTheme(
            sharedThemeInt: Int,
            systemDarkTheme: Boolean,
        ): Boolean = sharedThemeInt == 1 || (sharedThemeInt == 2 && systemDarkTheme)

        // Триггер на обновление состояния выбранной темы
        fun getThemeApp(shared: SharedPreferences) {
            viewModelScope.launch {
                themeAppRepo.isDarkTheme(shared)
            }
        }

        private val _state: MutableStateFlow<ThemeState> =
            MutableStateFlow(ThemeState.Loading)

        // Значение темы в числовом формате
        val state: StateFlow<ThemeState> =
            _state.asStateFlow()

        init {
            viewModelScope.launch {
                themeAppRepo.isDarkTheme.collect {
                    _state.value = ThemeState.Success(it)
                }
            }
        }
    }

sealed interface ThemeState {
    data object Loading : ThemeState

    data class Success(
        val themeStateResult: Int,
    ) : ThemeState
}

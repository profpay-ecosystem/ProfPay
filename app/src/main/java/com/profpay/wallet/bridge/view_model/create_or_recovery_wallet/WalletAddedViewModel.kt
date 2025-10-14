package com.profpay.wallet.bridge.view_model.create_or_recovery_wallet

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.data.flow_db.repo.WalletAddedRepo
import com.profpay.wallet.tron.AddressesWithKeysForM
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletAddedViewModel @Inject constructor(
    private val walletAddedRepo: WalletAddedRepo,
) : ViewModel() {
    private val _uiEvent = MutableSharedFlow<WalletUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    sealed class WalletUiEvent {
        object NavigateToHome : WalletUiEvent()
        data class ShowError(val message: String) : WalletUiEvent()
    }

    fun onWalletCreatedClicked(
        addressesWithKeysForM: AddressesWithKeysForM,
        sharedPref: SharedPreferences
    ) = viewModelScope.launch {
        try {
            // Получаем deviceToken — уникальный идентификатор устройства.
            val deviceToken = sharedPref.getString(PrefKeys.DEVICE_TOKEN, null)
                ?: throw Exception("Device Token not found.")

            // Проверяем, первый ли это запуск приложения (нужно для регистрации нового пользователя)
            val isFirstStarted = sharedPref.getBoolean(PrefKeys.FIRST_STARTED, true)

            if (isFirstStarted) {
                // Если первый запуск — регистрируем новый аккаунт пользователя
                walletAddedRepo.registerUserAccount(deviceToken, sharedPref)

                // Помечаем, что первый запуск завершён
                sharedPref.edit(commit = true) {
                    putBoolean(PrefKeys.FIRST_STARTED, false)
                }
            }

            // Создаём криптоадреса для пользователя
            walletAddedRepo.createCryptoAddresses(addressesWithKeysForM)

            // Сохраняем эти адреса в локальную базу данных
            walletAddedRepo.insertNewCryptoAddresses(addressesWithKeysForM)

            // Навигация на главный экран после успешного завершения процесса
            _uiEvent.emit(WalletUiEvent.NavigateToHome)

        } catch (e: Exception) {
            // В случае ошибки сбрасываем флаг, чтобы можно было повторить регистрацию
            sharedPref.edit(commit = true) {
                putBoolean(PrefKeys.FIRST_STARTED, true)
            }

            // Отправляем ошибку в Sentry для анализа
            Sentry.captureException(e)

            // Отображаем сообщение об ошибке пользователю
            _uiEvent.emit(WalletUiEvent.ShowError(e.message ?: "Неизвестная ошибка"))
        }
    }

    fun onWalletRecoveryClicked(
        sharedPref: SharedPreferences,
        addressesWithKeysForM: AddressesWithKeysForM,
        accountWasFound: Boolean,
        userId: Long?
    ) = viewModelScope.launch {
        // Получаем сохранённый deviceToken, без него нельзя продолжать
        val deviceToken = sharedPref.getString(PrefKeys.DEVICE_TOKEN, null)
            ?: throw Exception("Device Token not found.")

        // Проверяем, первый ли запуск приложения
        val isFirstStarted = sharedPref.getBoolean(PrefKeys.FIRST_STARTED, true)

        try {
            if (isFirstStarted) {
                // Первый запуск
                if (accountWasFound && userId != null) {
                    // Аккаунт найден, просто регистрируем устройство
                    walletAddedRepo.registerUserDevice(userId, deviceToken, sharedPref)
                    walletAddedRepo.insertNewCryptoAddresses(addressesWithKeysForM)
                } else {
                    // Аккаунт не найден — создаём новый, затем адреса
                    walletAddedRepo.registerUserAccount(deviceToken, sharedPref)
                    walletAddedRepo.createCryptoAddresses(addressesWithKeysForM)
                    walletAddedRepo.insertNewCryptoAddresses(addressesWithKeysForM)
                }

                // Помечаем, что первый запуск завершён
                sharedPref.edit { putBoolean(PrefKeys.FIRST_STARTED, false) }
            } else {
                // Повторный запуск — просто пересоздаём и вставляем адреса
                walletAddedRepo.createCryptoAddresses(addressesWithKeysForM)
                walletAddedRepo.insertNewCryptoAddresses(addressesWithKeysForM)
            }

            // Навигация на главный экран после успешной операции
            _uiEvent.emit(WalletUiEvent.NavigateToHome)
        } catch (e: Exception) {
            // В случае ошибки сбрасываем флаг, чтобы можно было повторить первый запуск
            if (!sharedPref.getBoolean(PrefKeys.FIRST_STARTED, true)) {
                sharedPref.edit { putBoolean(PrefKeys.FIRST_STARTED, true) }
            }

            // Отображаем ошибку пользователю
            _uiEvent.emit(WalletUiEvent.ShowError(e.message ?: "Неизвестная ошибка"))
        }
    }

}

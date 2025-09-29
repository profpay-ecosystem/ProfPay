package com.profpay.wallet.bridge.view_model.pin_lock

import android.content.Context
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.services.AppLockManager
import com.profpay.wallet.security.KeystoreEncryptionUtils
import com.profpay.wallet.security.SecureDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class PinLockViewModel
    @Inject
    constructor(
        @param:ApplicationContext val context: Context,
        private val dataStore: DataStore<Preferences>,
    ) : ViewModel() {
        private val _navigationEvents = MutableSharedFlow<LockState>(replay = 1)
        val navigationEvents = _navigationEvents.asSharedFlow()
        private val keystore = KeystoreEncryptionUtils()

        init {
            AppLockManager.lock()
        }

        private fun launchIO(block: suspend () -> Unit) =
            viewModelScope.launch(Dispatchers.IO) {
                block()
            }

        fun checkPinState() =
            launchIO {
                val savedPin = dataStore.data.map { it[SecureDataStore.PIN_CODE_KEY] }.firstOrNull()

                val newState =
                    if (AppLockManager.isAppLocked()) {
                        if (savedPin == null) LockState.RequireCreation else LockState.RequireUnlock
                    } else {
                        LockState.None
                    }

                _navigationEvents.emit(newState)
            }

        fun unlockSession() {
            AppLockManager.unlock()
            viewModelScope.launch { _navigationEvents.emit(LockState.None) }
        }

        fun saveNewPin(pin: String) =
            launchIO {
                val encrypted = keystore.encrypt(pin.toByteArray())
                val encoded = Base64.encodeToString(encrypted, Base64.DEFAULT)

                dataStore.edit { prefs ->
                    prefs[SecureDataStore.PIN_CODE_KEY] = encoded
                }

                unlockSession()
            }

        fun validatePin(
            entered: String,
            callback: (Boolean) -> Unit,
        ) = launchIO {
            val saved = dataStore.data.map { it[SecureDataStore.PIN_CODE_KEY] }.firstOrNull()
            val enteredBytes = entered.toByteArray()
            val isCorrect =
                saved?.let {
                    try {
                        val decryptedBytes = keystore.decrypt(Base64.decode(it, Base64.DEFAULT))
                        val match = MessageDigest.isEqual(decryptedBytes, enteredBytes)
                        decryptedBytes.fill(0)
                        match
                    } catch (_: Exception) {
                        false
                    }
                } ?: false

            enteredBytes.fill(0)

            withContext(Dispatchers.Main) {
                callback(isCorrect)
            }
        }
    }

enum class LockState {
    RequireUnlock,
    RequireCreation,
    None,
}

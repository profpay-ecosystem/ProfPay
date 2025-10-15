package com.profpay.wallet.bridge.viewmodel.createorrecovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.repository.flow.AddressAndMnemonicRepo
import com.profpay.wallet.data.repository.flow.RecoveryResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoverWalletViewModel @Inject constructor(
    private val addressAndMnemonicRepo: AddressAndMnemonicRepo,
) : ViewModel() {
    // Восстановление кошелька по мнемонике(сид-фразе)
    // Данные восстановленного кошелька

    private val _mutableState = MutableStateFlow<RecoverWalletState>(RecoverWalletState.Loading)
    val mutableState: StateFlow<RecoverWalletState> = _mutableState.asStateFlow()

    private val _uiEvent = MutableStateFlow<RecoverUiEvent?>(null)
    val uiEvent = _uiEvent.asStateFlow()

    init {
        viewModelScope.launch {
            addressAndMnemonicRepo.addressFromMnemonic.collect {
                _mutableState.value = RecoverWalletState.Success(it)
            }
        }
    }

    fun recoverWallet(mnemonic: String) =
        viewModelScope.launch {
            try {
                addressAndMnemonicRepo.generateAddressFromMnemonic(mnemonic)
                _uiEvent.emit(RecoverUiEvent.Success)
            } catch (e: Exception) {
                _uiEvent.emit(RecoverUiEvent.Error(e.message ?: "Ошибка восстановления"))
            }
        }

    fun clearAddressFromMnemonic() =
        viewModelScope.launch {
            addressAndMnemonicRepo.clearAddressFromMnemonic()
        }
}

sealed interface RecoverWalletState {
    data object Loading : RecoverWalletState

    data class Success(
        val addressRecoverResult: RecoveryResult,
    ) : RecoverWalletState
}

sealed class RecoverUiEvent {
    data object Success : RecoverUiEvent()

    data class Error(
        val message: String,
    ) : RecoverUiEvent()
}

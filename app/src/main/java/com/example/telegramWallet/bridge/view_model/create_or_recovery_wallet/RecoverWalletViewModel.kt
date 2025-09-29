package com.example.telegramWallet.bridge.view_model.create_or_recovery_wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.data.flow_db.repo.AddressAndMnemonicRepo
import com.example.telegramWallet.data.flow_db.repo.RecoveryResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecoverWalletViewModel
    @Inject
    constructor(
        private val addressAndMnemonicRepo: AddressAndMnemonicRepo,
    ) : ViewModel() {
        // Восстановление кошелька по мнемонике(сид-фразе)
        // Данные восстановленного кошелька

        private val _mutableState = MutableStateFlow<RecoverWalletState>(RecoverWalletState.Loading)
        val state: StateFlow<RecoverWalletState> = _mutableState.asStateFlow()

        init {
            viewModelScope.launch {
                addressAndMnemonicRepo.addressFromMnemonic.collect {
                    _mutableState.value = RecoverWalletState.Success(it)
                }
            }
        }

        fun recoverWallet(mnemonic: String) {
            viewModelScope.launch {
                addressAndMnemonicRepo.generateAddressFromMnemonic(mnemonic)
            }
        }

        fun clearAddressFromMnemonic() {
            viewModelScope.launch {
                addressAndMnemonicRepo.clearAddressFromMnemonic()
            }
        }
    }

sealed interface RecoverWalletState {
    data object Loading : RecoverWalletState

    data class Success(
        val addressRecoverResult: RecoveryResult,
    ) : RecoverWalletState
}

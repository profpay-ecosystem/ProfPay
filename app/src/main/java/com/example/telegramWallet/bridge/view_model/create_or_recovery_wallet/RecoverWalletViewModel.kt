package com.example.telegramWallet.bridge.view_model.create_or_recovery_wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.data.flow_db.repo.AddressAndMnemonicRepo
import com.example.telegramWallet.data.flow_db.repo.RecoveryResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class RecoverWalletViewModel @Inject constructor(
    private val addressAndMnemonicRepo: AddressAndMnemonicRepo
) : ViewModel() {
    // Восстановление кошелька по мнемонике(сид-фразе)
    fun recoverWallet(mnemonic: String) {
        viewModelScope.launch {
            addressAndMnemonicRepo.generateAddressFromMnemonic(mnemonic)
        }
    }
    // Данные восстановленного кошелька
    val state: StateFlow<RecoverWalletState> =
        addressAndMnemonicRepo.addressFromMnemonic.map {
            RecoverWalletState.Success(it)
        }.stateIn(
            scope = viewModelScope, initialValue = RecoverWalletState.Loading,
            started = SharingStarted.WhileSubscribed(5_000)
        )
}
sealed interface RecoverWalletState {
    data object Loading : RecoverWalletState
    data class Success(
        val addressRecoverResult: RecoveryResult
    ) : RecoverWalletState
}
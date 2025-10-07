package com.profpay.wallet.bridge.view_model.create_or_recovery_wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.flow_db.repo.AddressAndMnemonicRepo
import com.profpay.wallet.tron.AddressGenerateResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateNewWalletViewModel
    @Inject
    constructor(
        private val addressAndMnemonicRepo: AddressAndMnemonicRepo,
    ) : ViewModel() {
        // Создание нового кошелька
        private fun createNewWallet() {
            viewModelScope.launch {
                addressAndMnemonicRepo.generateNewAddressAndMnemonic()
            }
        }

        private val _state: MutableStateFlow<CreateNewWalletState> =
            MutableStateFlow(CreateNewWalletState.Loading)

        // Получаем данные нового кошелька
        val state: StateFlow<CreateNewWalletState> =
            _state.asStateFlow()

        init {
            createNewWallet()
            viewModelScope.launch {
                addressAndMnemonicRepo.addressAndMnemonic.collect {
                    _state.value = CreateNewWalletState.Success(it)
                }
            }
        }
    }

sealed interface CreateNewWalletState {
    data object Loading : CreateNewWalletState

    data class Success(
        val addressGenerateResult: AddressGenerateResult,
    ) : CreateNewWalletState
}


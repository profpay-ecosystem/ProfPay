package com.example.telegramWallet.bridge.view_model.create_or_recovery_wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.data.flow_db.repo.AddressAndMnemonicRepo
import com.example.telegramWallet.tron.AddressGenerateResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class SeedPhraseConfirmationViewModel
    @Inject
    constructor(
        addressAndMnemonicRepo: AddressAndMnemonicRepo,
    ) : ViewModel() {
        // Данные нового кошелька
        val state: StateFlow<SeedPhraseConfirmationState> =
            addressAndMnemonicRepo.addressAndMnemonic
                .map {
                    SeedPhraseConfirmationState.Success(it)
                }.stateIn(
                    scope = viewModelScope,
                    initialValue = SeedPhraseConfirmationState.Loading,
                    started = SharingStarted.WhileSubscribed(5_000),
                )
    }

sealed interface SeedPhraseConfirmationState {
    data object Loading : SeedPhraseConfirmationState

    data class Success(
        val addressGenerateResult: AddressGenerateResult,
    ) : SeedPhraseConfirmationState
}

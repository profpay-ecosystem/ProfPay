package com.profpay.wallet.bridge.viewmodel.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.repository.ReissueCentralAddressRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReissueCentralAddressViewModel
    @Inject
    constructor(
        private val repository: ReissueCentralAddressRepo,
    ) : ViewModel() {
        fun reissueCentralAddress() =
            viewModelScope.launch {
                repository.changeCentralAddress()
            }
    }

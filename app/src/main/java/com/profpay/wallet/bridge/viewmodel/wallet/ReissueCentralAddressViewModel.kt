package com.profpay.wallet.bridge.viewmodel.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import com.profpay.wallet.tron.Tron
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReissueCentralAddressViewModel
    @Inject
    constructor(
        private var centralAddressRepo: CentralAddressRepo,
        private val tron: Tron,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        fun reissueCentralAddress() =
            viewModelScope.launch(ioDispatcher) {
                val address = tron.addressUtilities.generateSingleAddress()
                centralAddressRepo.changeCentralAddress(
                    address = address.address,
                    publicKey = address.publicKey,
                    privateKey = address.privateKey,
                )
            }
    }

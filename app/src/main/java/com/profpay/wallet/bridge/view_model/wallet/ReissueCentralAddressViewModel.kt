package com.profpay.wallet.bridge.view_model.wallet

import androidx.lifecycle.ViewModel
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.tron.Tron
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ReissueCentralAddressViewModel
    @Inject
    constructor(
        private var centralAddressRepo: CentralAddressRepo,
        private val tron: Tron,
    ) : ViewModel() {
        suspend fun reissueCentralAddress() {
            val address = tron.addressUtilities.generateAddressAndMnemonic()
            centralAddressRepo.changeCentralAddress(
                address =
                    address.addressesWithKeysForM.addresses
                        .get(0)
                        .address,
                publicKey =
                    address.addressesWithKeysForM.addresses
                        .get(0)
                        .publicKey,
                privateKey =
                    address.addressesWithKeysForM.addresses
                        .get(0)
                        .privateKey,
            )
        }
    }

package com.profpay.wallet.bridge.view_model.wallet.walletSot

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class WalletArchivalSotViewModel
    @Inject
    constructor(
        private val addressRepo: AddressRepo,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        fun getAddressWithTokensArchivalByBlockchain(
            walletId: Long,
            blockchainName: String,
        ): LiveData<List<AddressWithTokens>> =
            liveData(ioDispatcher) {
                emitSource(addressRepo.getAddressesWithTokensArchivalByBlockchainFlow(walletId, blockchainName).asLiveData())
            }

        fun getAddressesWTAWithFunds(
            listAddressWithTokens: List<AddressWithTokens>,
            tokenName: String,
        ): List<AddressWithTokens> =
            listAddressWithTokens.filter { addressWT ->
                addressWT.tokens.any { token ->
                    token.token.tokenName == tokenName && token.balanceWithoutFrozen > BigInteger.ZERO
                }
            }
    }

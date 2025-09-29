package com.profpay.wallet.bridge.view_model.wallet.walletSot

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.TokenRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class WalletArchivalSotViewModel
    @Inject
    constructor(
        private val addressRepo: AddressRepo,
        private val walletProfileRepo: WalletProfileRepo,
        private val tokenRepo: TokenRepo,
    ) : ViewModel() {
        fun getAddressWithTokensArchivalByBlockchainLD(
            walletId: Long,
            blockchainName: String,
        ): LiveData<List<AddressWithTokens>> =
            liveData(Dispatchers.IO) {
                emitSource(addressRepo.getAddressesWithTokensArchivalByBlockchainLD(walletId, blockchainName))
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

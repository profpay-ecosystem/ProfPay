package com.profpay.wallet.tron

import android.content.Context
import com.profpay.wallet.tron.smartcontract.SmartContract
import javax.inject.Inject

class Tron
    @Inject
    constructor(
        private val context: Context,
    ) {
        val transactions: Transactions = Transactions()
        val addressUtilities: AddressUtilities = AddressUtilities()
        val staking: Staking = Staking()
        val accounts: Accounts = Accounts()
        val smartContracts: SmartContract = SmartContract(context = context)
    }

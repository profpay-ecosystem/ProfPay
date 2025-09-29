package com.profpay.wallet.bridge.view_model.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.profpay.wallet.data.database.entities.wallet.CentralAddressEntity
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class WalletSystemTRXScreenViewModel
    @Inject
    constructor(
        val centralAddressRepo: CentralAddressRepo,
        val transactionsRepo: TransactionsRepo,
    ) : ViewModel() {
        fun getCentralAddressLiveData(): LiveData<CentralAddressEntity?> =
            liveData(Dispatchers.IO) {
                emitSource(centralAddressRepo.getCentralAddressLiveData())
            }
    }

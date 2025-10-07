package com.profpay.wallet.bridge.view_model.wallet

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.profpay.wallet.data.database.entities.wallet.CentralAddressEntity
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Inject

@HiltViewModel
class WalletSystemTRXScreenViewModel
    @Inject
    constructor(
        val centralAddressRepo: CentralAddressRepo,
        val transactionsRepo: TransactionsRepo,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        fun getCentralAddressLiveData(): LiveData<CentralAddressEntity?> =
            liveData(ioDispatcher) {
                emitSource(centralAddressRepo.getCentralAddressLiveData())
            }
    }

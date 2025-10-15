package com.profpay.wallet.bridge.viewmodel.wallet.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.database.entities.wallet.CentralAddressEntity
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CentralAddressTxHistoryViewModel
    @Inject
    constructor(
        private val transactionsRepo: TransactionsRepo,
        private val centralAddressRepo: CentralAddressRepo,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _groupedAllTransaction = MutableStateFlow<List<List<TransactionModel>>>(emptyList())
        val groupedAllTransaction: StateFlow<List<List<TransactionModel>>> = _groupedAllTransaction

        fun getTransactionsByAddressAndTokenLD(
            walletId: Long,
            address: String,
            tokenName: String,
            isSender: Boolean,
            isCentralAddress: Boolean,
        ): LiveData<List<TransactionModel>> =
            liveData(ioDispatcher) {
                emitSource(
                    transactionsRepo
                        .getTransactionsByAddressAndTokenFlow(
                            walletId = walletId,
                            address = address,
                            tokenName = tokenName,
                            isSender = isSender,
                            isCentralAddress = isCentralAddress,
                        ).asLiveData(),
                )
            }

        fun groupTransactions(list: List<TransactionModel>) =
            viewModelScope.launch(ioDispatcher) {
                val groupedList =
                    if (list.isEmpty()) {
                        emptyList()
                    } else {
                        list
                            .sortedByDescending { it.timestamp }
                            .groupBy { it.transactionDate }
                            .values
                            .toList()
                    }

                _groupedAllTransaction.value = groupedList
            }

        fun getCentralAddressLiveData(): LiveData<CentralAddressEntity?> =
            liveData(ioDispatcher) {
                emitSource(centralAddressRepo.getCentralAddressFlow().asLiveData())
            }
    }

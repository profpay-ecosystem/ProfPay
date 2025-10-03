package com.profpay.wallet.bridge.view_model.wallet.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.data.database.entities.wallet.CentralAddressEntity
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CentralAddressTxHistoryViewModel
    @Inject
    constructor(
        private val transactionsRepo: TransactionsRepo,
        private val centralAddressRepo: CentralAddressRepo,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        fun getTransactionsByAddressAndTokenLD(
            walletId: Long,
            address: String,
            tokenName: String,
            isSender: Boolean,
            isCentralAddress: Boolean,
        ): LiveData<List<TransactionModel>> =
            liveData(ioDispatcher) {
                emitSource(
                    transactionsRepo.getTransactionsByAddressAndTokenLD(
                        walletId = walletId,
                        address = address,
                        tokenName = tokenName,
                        isSender = isSender,
                        isCentralAddress = isCentralAddress,
                    ),
                )
            }

        fun getListTransactionToTimestamp(listTransactions: List<TransactionModel>): List<List<TransactionModel?>> {
            var listListTransactions: List<List<TransactionModel>> = listOf(emptyList())

            viewModelScope.launch(ioDispatcher) {
                if (listTransactions.isEmpty()) return@launch
                listListTransactions =
                    listTransactions
                        .sortedByDescending { it.timestamp }
                        .groupBy { it.transactionDate }
                        .values
                        .toList()
            }
            return listListTransactions
        }

        fun getCentralAddressLiveData(): LiveData<CentralAddressEntity?> =
            liveData(ioDispatcher) {
                emitSource(centralAddressRepo.getCentralAddressLiveData())
            }
    }

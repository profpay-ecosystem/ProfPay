package com.profpay.wallet.bridge.view_model.wallet.transaction

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.profpay.wallet.data.database.entities.wallet.CentralAddressEntity
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CentralAddressTxHistoryViewModel
    @Inject
    constructor(
        private val transactionsRepo: TransactionsRepo,
        private val centralAddressRepo: CentralAddressRepo,
    ) : ViewModel() {
        fun getTransactionsByAddressAndTokenLD(
            walletId: Long,
            address: String,
            tokenName: String,
            isSender: Boolean,
            isCentralAddress: Boolean,
        ): LiveData<List<TransactionModel>> =
            liveData(Dispatchers.IO) {
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

        suspend fun getListTransactionToTimestamp(listTransactions: List<TransactionModel>): List<List<TransactionModel?>> {
            var listListTransactions: List<List<TransactionModel>> = listOf(emptyList())

            withContext(Dispatchers.IO) {
                if (listTransactions.isEmpty()) return@withContext
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
            liveData(Dispatchers.IO) {
                emitSource(centralAddressRepo.getCentralAddressLiveData())
            }
    }

package com.profpay.wallet.bridge.view_model.wallet.walletSot

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.bridge.view_model.dto.transfer.TransferResult
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.models.TokenWithPendingTransactions
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.flow_db.repo.EstimateCommissionResult
import com.profpay.wallet.data.flow_db.repo.WalletAddressRepo
import com.profpay.wallet.data.services.TransactionProcessorService
import com.profpay.wallet.tron.Tron
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.protobuf.transfer.TransferProto
import java.math.BigInteger
import javax.inject.Inject

@HiltViewModel
class WalletAddressViewModel
    @Inject
    constructor(
        private val walletAddressRepo: WalletAddressRepo,
        val addressRepo: AddressRepo,
        val transactionsRepo: TransactionsRepo,
        val tron: Tron,
        private val transactionProcessorService: TransactionProcessorService,
    ) : ViewModel() {
        private val _isActivated = MutableStateFlow(false)
        val isActivated: StateFlow<Boolean> = _isActivated

        private val _stateCommission =
            MutableStateFlow<EstimateCommissionResult>(EstimateCommissionResult.Empty)
        val stateCommission: StateFlow<EstimateCommissionResult> = _stateCommission.asStateFlow()

        fun checkActivation(address: String) {
            viewModelScope.launch {
                _isActivated.value =
                    withContext(Dispatchers.IO) {
                        tron.addressUtilities.isAddressActivated(address)
                    }
            }
        }

        suspend fun estimateCommission(
            address: String,
            bandwidth: Long,
            energy: Long,
        ) {
            walletAddressRepo.estimateCommission(address, bandwidth = bandwidth, energy = energy)
            walletAddressRepo.estimateCommission.collect { commission ->
                _stateCommission.value = commission
            }
        }

        fun getAddressWithTokensByAddressLD(address: String): LiveData<AddressWithTokens> =
            liveData(Dispatchers.IO) {
                emitSource(addressRepo.getAddressWithTokensByAddressLD(address))
            }

        suspend fun isGeneralAddress(address: String): Boolean = addressRepo.isGeneralAddress(address)

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

        suspend fun rejectTransaction(
            toAddress: String,
            addressWithTokens: AddressWithTokens,
            amount: BigInteger,
            commission: BigInteger,
            tokenEntity: TokenWithPendingTransactions?,
            commissionResult: TransferProto.EstimateCommissionResponse,
        ): TransferResult =
            transactionProcessorService.sendTransaction(
                sender = addressWithTokens.addressEntity.address,
                receiver = toAddress,
                amount = amount,
                commission = commission,
                tokenEntity = tokenEntity,
                commissionResult = commissionResult,
            )

        suspend fun acceptTransaction(
            addressWithTokens: AddressWithTokens,
            commission: BigInteger,
            walletId: Long,
            tokenEntity: TokenWithPendingTransactions?,
            amount: BigInteger,
            commissionResult: TransferProto.EstimateCommissionResponse,
        ): TransferResult {
            val generalAddress = addressRepo.getGeneralAddressByWalletId(walletId)
            return transactionProcessorService.sendTransaction(
                sender = addressWithTokens.addressEntity.address,
                receiver = generalAddress,
                amount = amount,
                commission = commission,
                tokenEntity = tokenEntity,
                commissionResult = commissionResult,
            )
        }
    }

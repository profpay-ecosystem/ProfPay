package com.profpay.wallet.bridge.viewmodel.wallet.walletSot

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.bridge.viewmodel.dto.transfer.TransferResult
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.models.TokenWithPendingTransactions
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import com.profpay.wallet.data.repository.flow.EstimateCommissionResult
import com.profpay.wallet.data.repository.flow.WalletAddressRepo
import com.profpay.wallet.data.services.TransactionProcessorService
import com.profpay.wallet.data.utils.toSunAmount
import com.profpay.wallet.security.KeystoreCryptoManager
import com.profpay.wallet.tron.Tron
import com.profpay.wallet.utils.ResolvePrivateKeyDeps
import com.profpay.wallet.utils.resolvePrivateKey
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.protobuf.transfer.TransferProto
import java.math.BigInteger
import javax.inject.Inject

sealed class TransferRejectUiEvent {
    data object Idle : TransferRejectUiEvent()

    data object Success : TransferRejectUiEvent()

    data class Error(
        val title: String,
        val message: String,
    ) : TransferRejectUiEvent()
}

@HiltViewModel
class WalletAddressViewModel
    @Inject
    constructor(
        private val walletAddressRepo: WalletAddressRepo,
        val addressRepo: AddressRepo,
        val transactionsRepo: TransactionsRepo,
        val tron: Tron,
        private val transactionProcessorService: TransactionProcessorService,
        private val keystoreCryptoManager: KeystoreCryptoManager,
        private val walletProfileRepo: WalletProfileRepo,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _isActivated = MutableStateFlow(false)
        val isActivated: StateFlow<Boolean> = _isActivated

        private val _stateCommission =
            MutableStateFlow<EstimateCommissionResult>(EstimateCommissionResult.Empty)
        val stateCommission: StateFlow<EstimateCommissionResult> = _stateCommission.asStateFlow()

        private val _isGeneralAddress = MutableStateFlow(false)
        val isGeneralAddress: StateFlow<Boolean> = _isGeneralAddress.asStateFlow()

        private val _uiEventTransfer = MutableStateFlow<TransferRejectUiEvent>(TransferRejectUiEvent.Idle)
        val uiEventTransfer: StateFlow<TransferRejectUiEvent> = _uiEventTransfer.asStateFlow()

        fun checkActivation(address: String) =
            viewModelScope.launch {
                _isActivated.value =
                    withContext(ioDispatcher) {
                        tron.addressUtilities.isAddressActivated(address)
                    }
            }

        private suspend fun estimateCommission(
            address: String,
            bandwidth: Long,
            energy: Long,
        ) {
            walletAddressRepo.estimateCommission(address, bandwidth = bandwidth, energy = energy)
            walletAddressRepo.estimateCommission.collect { commission ->
                _stateCommission.value = commission
            }
        }

        fun requestCommission(
            addressWithTokens: AddressWithTokens,
            tokenName: String,
            valueAmount: String,
            addressSending: String,
        ) = viewModelScope.launch {
            if (valueAmount.isEmpty() ||
                !tron.addressUtilities.isValidTronAddress(addressSending)
            ) {
                return@launch
            }

            val privKeyBytes =
                resolvePrivateKey(
                    walletId = addressWithTokens.addressEntity.walletId,
                    addressEntity = addressWithTokens.addressEntity,
                    resolvePrivateKeyDeps =
                        ResolvePrivateKeyDeps(
                            addressRepo = addressRepo,
                            walletProfileRepo = walletProfileRepo,
                            keystoreCryptoManager = keystoreCryptoManager,
                            tron = tron,
                        ),
                )

            try {
                val amount = valueAmount.toBigDecimal().toSunAmount()

                val requiredEnergy =
                    withContext(ioDispatcher) {
                        tron.transactions.estimateEnergy(
                            fromAddress = addressWithTokens.addressEntity.address,
                            toAddress = addressSending,
                            privateKey = privKeyBytes,
                            amount = amount,
                        )
                    }

                val requiredBandwidth =
                    withContext(ioDispatcher) {
                        tron.transactions.estimateBandwidth(
                            fromAddress = addressWithTokens.addressEntity.address,
                            toAddress = addressSending,
                            privateKey = privKeyBytes,
                            amount = amount,
                        )
                    }

                val hasEnoughBandwidth =
                    withContext(ioDispatcher) {
                        tron.accounts.hasEnoughBandwidth(
                            addressWithTokens.addressEntity.address,
                            requiredBandwidth.bandwidth,
                        )
                    }

                estimateCommission(
                    address = addressWithTokens.addressEntity.address,
                    bandwidth = if (hasEnoughBandwidth) 0 else requiredBandwidth.bandwidth,
                    energy = if (tokenName == "TRX") 0 else requiredEnergy.energy,
                )
            } catch (e: NumberFormatException) {
                Sentry.captureException(e)
            }
        }

        fun getAddressWithTokensByAddress(address: String): LiveData<AddressWithTokens> =
            liveData(ioDispatcher) {
                emitSource(addressRepo.getAddressWithTokensByAddressFlow(address).asLiveData())
            }

        fun isGeneralAddress(address: String) =
            viewModelScope.launch(ioDispatcher) {
                val value = addressRepo.isGeneralAddress(address)
                _isGeneralAddress.emit(value)
            }

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

        fun getListTransactionToTimestamp(listTransactions: List<TransactionModel>): List<List<TransactionModel>> {
            if (listTransactions.isEmpty()) return emptyList()
            val listListTransactions: List<List<TransactionModel>> =
                listTransactions
                    .sortedByDescending { it.timestamp }
                    .groupBy { it.transactionDate }
                    .values
                    .toList()

            return listListTransactions
        }

        fun onClickedReject(
            toAddress: String,
            addressWithTokens: AddressWithTokens,
            amount: BigInteger,
            commission: BigInteger,
            tokenEntity: TokenWithPendingTransactions?,
            commissionResult: TransferProto.EstimateCommissionResponse,
        ) = viewModelScope.launch(ioDispatcher) {
            val result =
                transactionProcessorService.sendTransaction(
                    sender = addressWithTokens.addressEntity.address,
                    receiver = toAddress,
                    amount = amount,
                    commission = commission,
                    tokenEntity = tokenEntity,
                    commissionResult = commissionResult,
                )

            when (result) {
                is TransferResult.Success -> _uiEventTransfer.emit(TransferRejectUiEvent.Success)
                is TransferResult.Failure ->
                    _uiEventTransfer.emit(
                        TransferRejectUiEvent.Error(
                            title = "Ошибка перевода",
                            message = result.error.message ?: "Неизвестная ошибка",
                        ),
                    )
            }
        }

        fun isValidTronAddress(address: String) = tron.addressUtilities.isValidTronAddress(address)
    }

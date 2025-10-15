package com.profpay.wallet.bridge.viewmodel.wallet.walletSot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.bridge.viewmodel.dto.transfer.TransferResult
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.models.TokenWithPendingTransactions
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import com.profpay.wallet.data.repository.flow.EstimateCommissionResult
import com.profpay.wallet.data.repository.flow.WalletAddressRepo
import com.profpay.wallet.data.services.TransactionProcessorService
import com.profpay.wallet.security.KeystoreCryptoManager
import com.profpay.wallet.tron.Tron
import com.profpay.wallet.utils.ResolvePrivateKeyDeps
import com.profpay.wallet.utils.resolvePrivateKey
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.protobuf.transfer.TransferProto
import java.math.BigInteger
import javax.inject.Inject

sealed class TransferUiEvent {
    data object Idle : TransferUiEvent()

    data object Success : TransferUiEvent()

    data class Error(
        val title: String,
        val message: String,
    ) : TransferUiEvent()
}

@HiltViewModel
class GeneralTransactionViewModel
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
        private val _stateCommission =
            MutableStateFlow<EstimateCommissionResult>(EstimateCommissionResult.Empty)
        val stateCommission: StateFlow<EstimateCommissionResult> = _stateCommission.asStateFlow()

        private val _isGeneralAddressNotActivatedVisible = MutableStateFlow(false)
        val isGeneralAddressNotActivatedVisible: StateFlow<Boolean> = _isGeneralAddressNotActivatedVisible

        private val _generalAddressActivatedCommission = MutableStateFlow<BigInteger?>(null)
        val generalAddressActivatedCommission: StateFlow<BigInteger?> = _generalAddressActivatedCommission

        private val _uiEventTransfer =
            MutableStateFlow<TransferUiEvent>(
                TransferUiEvent.Idle,
            )
        val uiEventTransfer: StateFlow<TransferUiEvent> = _uiEventTransfer.asStateFlow()

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

        fun prepareTransaction(
            walletId: Long,
            addressWithTokens: AddressWithTokens,
            tokenEntity: TokenWithPendingTransactions?,
            balance: BigInteger?,
        ) = viewModelScope.launch(ioDispatcher) {
            val generalAddress = addressRepo.getGeneralAddressByWalletId(walletId)

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

            val requiredEnergy =
                tron.transactions.estimateEnergy(
                    fromAddress = addressWithTokens.addressEntity.address,
                    toAddress = generalAddress,
                    privateKey = privKeyBytes,
                    amount = balance ?: tokenEntity?.balanceWithoutFrozen!!,
                )

            val requiredBandwidth =
                tron.transactions.estimateBandwidth(
                    fromAddress = addressWithTokens.addressEntity.address,
                    toAddress = generalAddress,
                    privateKey = privKeyBytes,
                    amount = balance ?: tokenEntity?.balanceWithoutFrozen!!,
                )

            if (!tron.addressUtilities.isAddressActivated(generalAddress)) {
                val commission = tron.addressUtilities.getCreateNewAccountFeeInSystemContract()
                _isGeneralAddressNotActivatedVisible.value = true
                _generalAddressActivatedCommission.value = commission
            }

            estimateCommission(
                address = addressWithTokens.addressEntity.address,
                bandwidth = requiredBandwidth.bandwidth,
                energy = requiredEnergy.energy,
            )
        }

        fun onConfirmTransaction(
            addressWithTokens: AddressWithTokens,
            commission: BigInteger,
            walletId: Long,
            tokenEntity: TokenWithPendingTransactions?,
            amount: BigInteger,
            commissionResult: TransferProto.EstimateCommissionResponse,
        ) = viewModelScope.launch {
            val generalAddress = addressRepo.getGeneralAddressByWalletId(walletId)
            val result =
                transactionProcessorService.sendTransaction(
                    sender = addressWithTokens.addressEntity.address,
                    receiver = generalAddress,
                    amount = amount,
                    commission = commission,
                    tokenEntity = tokenEntity,
                    commissionResult = commissionResult,
                )

            when (result) {
                is TransferResult.Success -> _uiEventTransfer.emit(TransferUiEvent.Success)
                is TransferResult.Failure ->
                    _uiEventTransfer.emit(
                        TransferUiEvent.Error(
                            title = "Ошибка перевода",
                            message = result.error.message ?: "Неизвестная ошибка",
                        ),
                    )
            }
        }
    }

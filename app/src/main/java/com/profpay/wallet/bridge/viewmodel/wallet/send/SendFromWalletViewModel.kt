package com.profpay.wallet.bridge.viewmodel.wallet.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.backend.http.models.binance.BinanceSymbolEnum
import com.profpay.wallet.bridge.viewmodel.dto.TokenName
import com.profpay.wallet.bridge.viewmodel.dto.transfer.TransferResult
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.profpay.wallet.data.database.repositories.wallet.TokenRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import com.profpay.wallet.data.repository.flow.EstimateCommissionResult
import com.profpay.wallet.data.repository.flow.SendFromWalletRepo
import com.profpay.wallet.data.services.TransactionProcessorService
import com.profpay.wallet.data.utils.toBigInteger
import com.profpay.wallet.data.utils.toSunAmount
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.security.KeystoreCryptoManager
import com.profpay.wallet.tron.Tron
import com.profpay.wallet.ui.feature.wallet.send.bottomsheet.ModelTransferFromBS
import com.profpay.wallet.utils.ResolvePrivateKeyDeps
import com.profpay.wallet.utils.resolvePrivateKey
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.protobuf.transfer.TransferProto
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

data class TransferUiState(
    val addressWithTokens: AddressWithTokens? = null,
    val isAddressActivated: Boolean = true,
    val isValidRecipientAddress: Boolean = true,
    val isEnoughBalance: Boolean = true,
    val warning: String? = null,
    val commission: BigDecimal = BigDecimal.ZERO,
    val commissionResult: TransferProto.EstimateCommissionResponse = TransferProto.EstimateCommissionResponse.getDefaultInstance(),
    val tokenBalance: BigInteger = BigInteger.ZERO,
    val isButtonEnabled: Boolean = false,
)

sealed class TransferUiEvent {
    data object Idle : TransferUiEvent()

    data object Success : TransferUiEvent()

    data class Error(
        val title: String,
        val message: String,
    ) : TransferUiEvent()
}

@HiltViewModel
class SendFromWalletViewModel
    @Inject
    constructor(
        val addressRepo: AddressRepo,
        val profileRepo: ProfileRepo,
        val tokenRepo: TokenRepo,
        private val sendFromWalletRepo: SendFromWalletRepo,
        val tron: Tron,
        val exchangeRatesRepo: ExchangeRatesRepo,
        private val transactionProcessorService: TransactionProcessorService,
        private val walletProfileRepo: WalletProfileRepo,
        private val keystoreCryptoManager: KeystoreCryptoManager,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) : ViewModel() {
        private val _stateCommission =
            MutableStateFlow<EstimateCommissionResult>(EstimateCommissionResult.Empty)
        val stateCommission: StateFlow<EstimateCommissionResult> = _stateCommission.asStateFlow()

        private val _uiEventTransfer = MutableStateFlow<TransferUiEvent>(TransferUiEvent.Idle)
        val uiEventTransfer: StateFlow<TransferUiEvent> = _uiEventTransfer.asStateFlow()

        private val _uiState = MutableStateFlow(TransferUiState())
        val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

        private val _trxToUsdtRate = MutableStateFlow<BigDecimal>(BigDecimal.ZERO)
        val trxToUsdtRate: StateFlow<BigDecimal> = _trxToUsdtRate

        private val _isActivated = MutableStateFlow(false)
        val isActivated: StateFlow<Boolean> = _isActivated

        fun checkActivation(address: String) =
            viewModelScope.launch {
                _isActivated.value =
                    withContext(ioDispatcher) {
                        tron.addressUtilities.isAddressActivated(address)
                    }
            }

        fun loadAddressWithTokens(
            addressId: Long,
            blockchain: String,
            tokenName: String,
        ) = viewModelScope.launch(ioDispatcher) {
            val addressWithTokens = addressRepo.getGeneralAddressWithTokens(addressId, blockchain)
            val isActivated = tron.addressUtilities.isAddressActivated(addressWithTokens.addressEntity.address)
            val token = addressWithTokens.tokens.find { it.token.tokenName == tokenName }
            val balance = token?.balanceWithoutFrozen?.toTokenAmount() ?: BigDecimal.ZERO

            _uiState.update {
                it.copy(
                    addressWithTokens = addressWithTokens,
                    isAddressActivated = isActivated,
                    tokenBalance = balance.toSunAmount(),
                    warning = it.warning,
                )
            }
        }

        fun updateInputs(
            addressTo: String,
            sum: String,
            tokenName: TokenName,
        ) {
            viewModelScope.launch(ioDispatcher) {
                val isValidAddress = tron.addressUtilities.isValidTronAddress(addressTo)
                val addressEntity = _uiState.value.addressWithTokens ?: return@launch
                val token = addressEntity.tokens.find { it.token.tokenName == tokenName.tokenName }
                val balance = token?.balanceWithoutFrozen?.toTokenAmount() ?: BigDecimal.ZERO
                val amount = sum.toBigDecimalOrNull() ?: BigDecimal.ZERO

                if (isValidAddress) {
                    estimateCommissions(addressEntity, sum, addressTo, tokenName)
                }

                val isEnough = amount <= balance

                _uiState.update {
                    it.copy(
                        isValidRecipientAddress = isValidAddress,
                        isEnoughBalance = isEnough,
                        isButtonEnabled = isValidAddress && isEnough && amount > BigDecimal.ZERO,
                        warning = if (!isEnough) "Недостаточно средств" else null,
                    )
                }
            }
        }

        fun onCommissionResult(result: EstimateCommissionResult) {
            when (result) {
                is EstimateCommissionResult.Success -> {
                    _uiState.update {
                        it.copy(
                            commission =
                                result.response.commission
                                    .toBigInteger()
                                    .toTokenAmount(),
                            commissionResult = result.response,
                        )
                    }
                }

                is EstimateCommissionResult.Error -> {
                    _uiState.update { it.copy(warning = "Ошибка при расчёте комиссии") }
                    Sentry.captureException(result.throwable)
                }

                else -> {
                    // Empty
                }
            }
        }

        fun loadTrxToUsdtRate() =
            viewModelScope.launch {
                val rate = exchangeRatesRepo.getExchangeRateValue(BinanceSymbolEnum.TRX_USDT.symbol)
                _trxToUsdtRate.value = rate.toBigDecimal()
            }

        private suspend fun estimateCommission(
            address: String,
            bandwidth: Long,
            energy: Long,
        ) {
            sendFromWalletRepo.estimateCommission(address, bandwidth, energy)
            sendFromWalletRepo.estimateCommission.collect { commission ->
                _stateCommission.value = commission
            }
        }

        fun onConfirmTransaction(modelTransferFromBS: ModelTransferFromBS) =
            viewModelScope.launch {
                val tokenName = modelTransferFromBS.tokenName.tokenName
                val tokenEntity =
                    modelTransferFromBS.addressWithTokens
                        ?.tokens
                        ?.firstOrNull { it.token.tokenName == tokenName }

                if (tokenEntity == null) {
                    _uiEventTransfer.emit(
                        TransferUiEvent.Error(
                            title = "Ошибка перевода",
                            message = "Не удалось найти токен",
                        ),
                    )
                    return@launch
                }

                val result =
                    transactionProcessorService.sendTransaction(
                        sender = modelTransferFromBS.addressSender,
                        receiver = modelTransferFromBS.addressReceiver,
                        amount = modelTransferFromBS.amount.toSunAmount(),
                        commission = modelTransferFromBS.commission.toSunAmount(),
                        tokenEntity = tokenEntity,
                        commissionResult = modelTransferFromBS.commissionResult,
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

        fun estimateCommissions(
            addressWithTokens: AddressWithTokens?,
            sumSending: String,
            addressSending: String,
            tokenNameModel: TokenName,
        ) {
            viewModelScope.launch(ioDispatcher) {
                if (addressWithTokens == null ||
                    sumSending.isEmpty() ||
                    !tron.addressUtilities.isValidTronAddress(
                        addressSending,
                    )
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

                val requiredBandwidth =
                    tron.transactions.estimateBandwidth(
                        fromAddress = addressWithTokens.addressEntity.address,
                        toAddress = addressSending,
                        privateKey = privKeyBytes,
                        amount = sumSending.toBigDecimal().toSunAmount(),
                    )

                val requiredEnergy =
                    if (tokenNameModel.tokenName == "USDT") {
                        tron.transactions
                            .estimateEnergy(
                                fromAddress = addressWithTokens.addressEntity.address,
                                toAddress = addressSending,
                                privateKey = privKeyBytes,
                                amount = sumSending.toBigDecimal().toSunAmount(),
                            ).energy
                    } else {
                        0
                    }

                val hasEnoughBandwidth =
                    tron.accounts.hasEnoughBandwidth(
                        addressWithTokens.addressEntity.address,
                        requiredBandwidth.bandwidth,
                    )

                estimateCommission(
                    address = addressWithTokens.addressEntity.address,
                    bandwidth = if (hasEnoughBandwidth) 0 else requiredBandwidth.bandwidth,
                    energy = requiredEnergy,
                )
            }
        }
    }

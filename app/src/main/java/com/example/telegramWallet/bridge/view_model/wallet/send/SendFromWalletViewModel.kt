package com.example.telegramWallet.bridge.view_model.wallet.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.AppConstants
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.ProfPayServerGrpcClient
import com.example.telegramWallet.backend.http.models.binance.BinanceSymbolEnum
import com.example.telegramWallet.bridge.view_model.dto.TokenName
import com.example.telegramWallet.bridge.view_model.dto.transfer.TransferResult
import com.example.telegramWallet.data.database.entities.wallet.PendingTransactionEntity
import com.example.telegramWallet.data.database.models.AddressWithTokens
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.ExchangeRatesRepo
import com.example.telegramWallet.data.database.repositories.wallet.PendingTransactionRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.flow_db.repo.EstimateCommissionResult
import com.example.telegramWallet.data.flow_db.repo.SendFromWalletRepo
import com.example.telegramWallet.data.utils.toByteString
import com.example.telegramWallet.data.utils.toSunAmount
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.exceptions.payments.GrpcClientErrorSendTransactionExcpetion
import com.example.telegramWallet.exceptions.payments.GrpcServerErrorSendTransactionExcpetion
import com.example.telegramWallet.tron.EstimateBandwidthData
import com.example.telegramWallet.tron.EstimateEnergyData
import com.example.telegramWallet.tron.SignedTransactionData
import com.example.telegramWallet.tron.Tron
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.protobuf.transfer.TransferProto
import org.example.protobuf.transfer.TransferProto.TransactionData
import org.example.protobuf.transfer.TransferProto.TransferNetwork
import org.example.protobuf.transfer.TransferProto.TransferToken
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
    val tokenBalance: BigInteger = BigInteger.ZERO,
    val isButtonEnabled: Boolean = false
)

@HiltViewModel
class SendFromWalletViewModel @Inject constructor(
    val addressRepo: AddressRepo,
    val profileRepo: ProfileRepo,
    val tokenRepo: TokenRepo,
    private val sendFromWalletRepo: SendFromWalletRepo,
    val tron: Tron,
    val exchangeRatesRepo: ExchangeRatesRepo,
    private val pendingTransactionRepo: PendingTransactionRepo,
    grpcClientFactory: GrpcClientFactory
) : ViewModel() {
    private val _stateCommission =
        MutableStateFlow<EstimateCommissionResult>(EstimateCommissionResult.Empty)
    val stateCommission: StateFlow<EstimateCommissionResult> = _stateCommission.asStateFlow()

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState: StateFlow<TransferUiState> = _uiState.asStateFlow()

    private val profPayServerGrpcClient: ProfPayServerGrpcClient = grpcClientFactory.getGrpcClient(
        ProfPayServerGrpcClient::class.java,
        AppConstants.Network.GRPC_ENDPOINT,
        AppConstants.Network.GRPC_PORT
    )

    fun loadAddressWithTokens(addressId: Long, blockchain: String, tokenName: String) {
        viewModelScope.launch {
            val addressWithTokens = getGeneralAddressWithTokens(addressId, blockchain)
            val isActivated = tron.addressUtilities.isAddressActivated(addressWithTokens.addressEntity.address)
            val token = addressWithTokens.tokens.find { it.token.tokenName == tokenName }
            val balance = token?.balanceWithoutFrozen?.toTokenAmount() ?: BigDecimal.ZERO

            _uiState.update {
                it.copy(
                    addressWithTokens = addressWithTokens,
                    isAddressActivated = isActivated,
                    tokenBalance = balance.toSunAmount(),
                    warning = if (!isActivated) "Перевод невозможен. Нужно активировать адрес, отправив 20 TRX." else null
                )
            }
        }
    }

    fun updateInputs(addressTo: String, sum: String, tokenName: TokenName) {
        viewModelScope.launch {
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
                    warning = if (!isValidAddress && addressTo != "") "Невалидный адрес" else if (!isEnough) "Недостаточно средств" else null
                )
            }
        }
    }

    fun onCommissionResult(result: EstimateCommissionResult) {
        when (result) {
            is EstimateCommissionResult.Success -> {
                _uiState.update { it.copy(commission = result.response.commission.toBigDecimal()) }
            }

            is EstimateCommissionResult.Error -> {
                _uiState.update { it.copy(warning = "Ошибка при расчёте комиссии") }
                Sentry.captureException(result.throwable)
            }

            else -> {}
        }
    }

    suspend fun trxToUsdtRate(): BigDecimal {
        val trxToUsdtRate =
            exchangeRatesRepo.getExchangeRateValue(BinanceSymbolEnum.TRX_USDT.symbol)
        return trxToUsdtRate.toBigDecimal()
    }

    private suspend fun estimateCommission(address: String, bandwidth: Long, energy: Long) {
        sendFromWalletRepo.estimateCommission(address, bandwidth, energy)
        sendFromWalletRepo.estimateCommission.collect { commission ->
            _stateCommission.value = commission
        }
    }

    suspend fun getGeneralAddressWithTokens(
        addressId: Long,
        blockchainName: String
    ): AddressWithTokens {
        return addressRepo.getGeneralAddressWithTokens(addressId, blockchainName)
    }

    suspend fun transferProcess(
        addressSenderId: Long, toAddress: String, amount: BigInteger, token: TransferToken,
        commission: BigInteger
    ): TransferResult {
        val userId = profileRepo.getProfileUserId()

        val addressSender = addressRepo.getAddressEntityById(addressSenderId)
            ?: return TransferResult.Failure(IllegalStateException("Адрес отправителя не найден"))

        val balance = tron.addressUtilities.getTrxBalance(addressSender.address).toTokenAmount()
        val amountToken = amount.toTokenAmount()
        val commissionToken = commission.toTokenAmount()
        val createAccountFee = tron.addressUtilities
            .getCreateNewAccountFeeInSystemContract()
            .toTokenAmount()

        if (token == TransferToken.TRX) {
            val netAmount: BigDecimal = if (tron.addressUtilities.isAddressActivated(toAddress)) {
                balance.subtract(amountToken).subtract(commissionToken)
            } else {
                balance.subtract(amountToken).subtract(commissionToken).subtract(createAccountFee)
            }

            if (netAmount < BigDecimal.ZERO) {
                return TransferResult.Failure(IllegalStateException("Перевод невозможен, так как суммы недостаточно с учетом комиссии"))
            }
        } else {
            if (balance < commission.toTokenAmount()) {
                return TransferResult.Failure(IllegalStateException("Недостаточно TRX для комиссии"))
            }
        }

        if (commission.toTokenAmount() <= BigDecimal.ZERO) {
            return TransferResult.Failure(IllegalArgumentException("Комиссия должна быть больше 0"))
        }

        val trxFeeAddress = profPayServerGrpcClient.getServerParameters().fold(
            onSuccess = {
                it.trxFeeAddress
            },
            onFailure = {
                Sentry.captureException(it)
                throw RuntimeException(it)
            }
        )

        val signedTxnBytesCommission = withContext(Dispatchers.IO) {
            tron.transactions.getSignedTrxTransaction(
                fromAddress = addressSender.address,
                toAddress = trxFeeAddress,
                privateKey = addressSender.privateKey,
                amount = commission
            )
        }

        val estimateCommissionBandwidth = withContext(Dispatchers.IO) {
            tron.transactions.estimateBandwidthTrxTransaction(
                fromAddress = addressSender.address,
                toAddress = trxFeeAddress,
                privateKey = addressSender.privateKey,
                amount = commission
            )
        }

        var estimateEnergy = EstimateEnergyData(0, BigInteger.ZERO)
        var estimateBandwidth = EstimateBandwidthData(300, 0.0)
        if (token == TransferToken.USDT_TRC20) {
            estimateEnergy = withContext(Dispatchers.IO) {
                tron.transactions.estimateEnergy(
                    fromAddress = addressSender.address,
                    toAddress = toAddress,
                    privateKey = addressSender.privateKey,
                    amount = amount
                )
            }

            estimateBandwidth = withContext(Dispatchers.IO) {
                tron.transactions.estimateBandwidth(
                    fromAddress = addressSender.address,
                    toAddress = toAddress,
                    privateKey = addressSender.privateKey,
                    amount = amount
                )
            }
        } else if (token == TransferToken.TRX) {
            estimateBandwidth = withContext(Dispatchers.IO) {
                tron.transactions.estimateBandwidthTrxTransaction(
                    fromAddress = addressSender.address,
                    toAddress = toAddress,
                    privateKey = addressSender.privateKey,
                    amount = amount
                )
            }
        }

        val signedTxnBytes: SignedTransactionData = when (token) {
            TransferToken.USDT_TRC20 -> withContext(Dispatchers.IO) {
                tron.transactions.getSignedUsdtTransaction(
                    fromAddress = addressSender.address,
                    toAddress = toAddress,
                    privateKey = addressSender.privateKey,
                    amount = amount
                )
            }

            TransferToken.TRX -> withContext(Dispatchers.IO) {
                tron.transactions.getSignedTrxTransaction(
                    fromAddress = addressSender.address,
                    toAddress = toAddress,
                    privateKey = addressSender.privateKey,
                    amount = amount
                )
            }

            TransferToken.UNRECOGNIZED -> return TransferResult.Failure(IllegalArgumentException("Токен не был определен системой."))
        }

        if (signedTxnBytes.signedTxn == null) {
            return TransferResult.Failure(IllegalStateException("Не удалось подписать транзакцию"))
        }

        val hasEnoughTransactionBandwidth = tron.accounts.hasEnoughBandwidth(
            addressSender.address,
            estimateBandwidth.bandwidth
        )
        val hasEnoughCommissionBandwidth = tron.accounts.hasEnoughBandwidth(
            addressSender.address,
            estimateCommissionBandwidth.bandwidth
        )

        return withContext(Dispatchers.IO) {
            try {
                sendFromWalletRepo.sendTronTransactionRequestGrpc(
                    userId = userId,
                    transaction = TransactionData.newBuilder()
                        .setAddress(addressSender.address)
                        .setReceiverAddress(toAddress)
                        .setAmount(amount.toByteString())
                        .setEstimateEnergy(estimateEnergy.energy)
                        .setBandwidthRequired(if (hasEnoughTransactionBandwidth) 0 else estimateBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytes.signedTxn)
                        .build(),
                    commission = TransferProto.TransactionCommissionData.newBuilder()
                        .setAddress(addressSender.address)
                        .setBandwidthRequired(if (hasEnoughCommissionBandwidth) 0 else estimateCommissionBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytesCommission.signedTxn)
                        .setAmount(commission.toByteString())
                        .build(),
                    network = TransferNetwork.MAIN_NET,
                    token = token,
                    txId = "null"
                )

                val tokenType = if (token == TransferToken.USDT_TRC20) "USDT" else "TRX"
                val tokenId = tokenRepo.getTokenIdByAddressIdAndTokenName(addressSender.addressId!!, tokenType)

                pendingTransactionRepo.insert(
                    PendingTransactionEntity(
                        tokenId = tokenId,
                        txid = signedTxnBytes.txid,
                        amount = amount
                    )
                )
                return@withContext TransferResult.Success
            } catch (e: GrpcServerErrorSendTransactionExcpetion) {
                return@withContext TransferResult.Failure(e)
            } catch (e: GrpcClientErrorSendTransactionExcpetion) {
                return@withContext TransferResult.Failure(e)
            } catch (e: Exception) {
                val message = e.message ?: "Unknown client error"
                val cause = e.cause ?: Throwable("No cause provided")
                val exception = GrpcClientErrorSendTransactionExcpetion(message, cause)

                Sentry.captureException(exception)
                return@withContext TransferResult.Failure(e)
            }
        }
    }

    fun estimateCommissions(
        addressWithTokens: AddressWithTokens?,
        sumSending: String,
        addressSending: String,
        tokenNameModel: TokenName
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (addressWithTokens == null || sumSending.isEmpty() || !tron.addressUtilities.isValidTronAddress(
                    addressSending
                )
            ) return@launch

            val requiredBandwidth = tron.transactions.estimateBandwidth(
                fromAddress = addressWithTokens.addressEntity.address,
                toAddress = addressSending,
                privateKey = addressWithTokens.addressEntity.privateKey,
                amount = sumSending.toBigDecimal().toSunAmount()
            )

            val requiredEnergy = if (tokenNameModel.tokenName == "USDT") {
                tron.transactions.estimateEnergy(
                    fromAddress = addressWithTokens.addressEntity.address,
                    toAddress = addressSending,
                    privateKey = addressWithTokens.addressEntity.privateKey,
                    amount = sumSending.toBigDecimal().toSunAmount()
                ).energy
            } else 0

            val hasEnoughBandwidth = tron.accounts.hasEnoughBandwidth(
                addressWithTokens.addressEntity.address,
                requiredBandwidth.bandwidth
            )

            withContext(Dispatchers.Main) {
                estimateCommission(
                    address = addressWithTokens.addressEntity.address,
                    bandwidth = if (hasEnoughBandwidth) 0 else requiredBandwidth.bandwidth,
                    energy = requiredEnergy
                )
            }
        }
    }
}

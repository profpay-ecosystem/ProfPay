package com.profpay.wallet.data.services

import android.database.sqlite.SQLiteConstraintException
import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.backend.grpc.ProfPayServerGrpcClient
import com.profpay.wallet.bridge.viewmodel.dto.TokenName
import com.profpay.wallet.bridge.viewmodel.dto.transfer.TransferResult
import com.profpay.wallet.data.database.entities.wallet.AddressEntity
import com.profpay.wallet.data.database.entities.wallet.PendingTransactionEntity
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.database.entities.wallet.TransactionStatusCode
import com.profpay.wallet.data.database.entities.wallet.assignTransactionType
import com.profpay.wallet.data.database.models.TokenWithPendingTransactions
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.TransactionsRepo
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.database.repositories.wallet.PendingTransactionRepo
import com.profpay.wallet.data.database.repositories.wallet.TokenRepo
import com.profpay.wallet.data.database.repositories.wallet.WalletProfileRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import com.profpay.wallet.data.repository.flow.WalletAddressRepo
import com.profpay.wallet.data.utils.toByteString
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.security.KeystoreCryptoManager
import com.profpay.wallet.tron.EstimateBandwidthData
import com.profpay.wallet.tron.EstimateEnergyData
import com.profpay.wallet.tron.SignedTransactionData
import com.profpay.wallet.tron.Tron
import com.profpay.wallet.utils.ResolvePrivateKeyDeps
import com.profpay.wallet.utils.resolvePrivateKey
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.bitcoinj.base.internal.ByteUtils
import org.example.protobuf.transfer.TransferProto
import org.example.protobuf.transfer.TransferProto.TransactionData
import org.example.protobuf.transfer.TransferProto.TransferNetwork
import org.example.protobuf.transfer.TransferProto.TransferToken
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionProcessorService
    @Inject
    constructor(
        private val walletAddressRepo: WalletAddressRepo,
        private val addressRepo: AddressRepo,
        private val centralAddressRepo: CentralAddressRepo,
        private val profileRepo: ProfileRepo,
        private val tokenRepo: TokenRepo,
        private val pendingTransactionRepo: PendingTransactionRepo,
        private val transactionsRepo: TransactionsRepo,
        private val keystoreCryptoManager: KeystoreCryptoManager,
        private val walletProfileRepo: WalletProfileRepo,
        val tron: Tron,
        @IoDispatcher private val dispatcher: CoroutineDispatcher,
        grpcClientFactory: GrpcClientFactory,
    ) {
        private val profPayServerGrpcClient: ProfPayServerGrpcClient =
            grpcClientFactory.getGrpcClient(
                ProfPayServerGrpcClient::class.java,
                AppConstants.Network.GRPC_ENDPOINT,
                AppConstants.Network.GRPC_PORT,
            )

        suspend fun sendTransaction(
            sender: String,
            receiver: String,
            amount: BigInteger,
            commission: BigInteger,
            tokenEntity: TokenWithPendingTransactions?,
            commissionResult: TransferProto.EstimateCommissionResponse,
        ): TransferResult {
            val tokenName = tokenEntity?.token?.tokenName ?: return fail("Токен не найден")
            val addressEntity = addressRepo.getAddressEntityByAddress(sender) ?: return fail("Адрес отправителя не найден")

            // Достаём приватный ключ
            val privKeyBytes =
                resolvePrivateKey(
                    walletId = addressEntity.walletId,
                    addressEntity = addressEntity,
                    resolvePrivateKeyDeps =
                        ResolvePrivateKeyDeps(
                            addressRepo = addressRepo,
                            walletProfileRepo = walletProfileRepo,
                            keystoreCryptoManager = keystoreCryptoManager,
                            tron = tron,
                        ),
                )
            // Получаем адрес для комиссии
            val commissionAddressData =
                getCommissionAddressData(
                    tokenName = tokenName,
                    privKeyBytes = privKeyBytes,
                    addressEntity = addressEntity,
                )
            // Валидируем
            try {
                validateBalances(sender, commissionAddressData, tokenName, commission, tokenEntity, amount)
            } catch (e: IllegalStateException) {
                return fail(e.message ?: "Ошибка валидации балансов")
            }
            val amountSending = calculateAmountSending(receiver, tokenName, amount, commission)
            val trxFeeAddress = getTrxFeeAddress() ?: return fail("Не удалось получить TRX fee адрес")
            val tokenType = if (tokenName == TokenName.TRX.tokenName) TransferToken.TRX else TransferToken.USDT_TRC20

            val (signedTxnBytes, estimateEnergy, estimateBandwidth) =
                signMainTransaction(tokenType, sender, receiver, privKeyBytes, amountSending)

            val signedTxnBytesCommission =
                tron.transactions.getSignedTrxTransaction(
                    fromAddress = commissionAddressData.address,
                    toAddress = trxFeeAddress,
                    privateKey = commissionAddressData.privateKey,
                    amount = commission,
                )

            val estimateCommissionBandwidth =
                tron.transactions.estimateBandwidthTrxTransaction(
                    fromAddress = commissionAddressData.address,
                    toAddress = trxFeeAddress,
                    privateKey = commissionAddressData.privateKey,
                    amount = commission,
                )

            return sendGrpcRequest(
                sender = sender,
                receiver = receiver,
                amountSending = amountSending,
                estimateEnergy = estimateEnergy,
                estimateBandwidth = estimateBandwidth,
                signedTxnBytes = signedTxnBytes,
                commissionAddressData = commissionAddressData,
                signedTxnBytesCommission = signedTxnBytesCommission,
                commission = commission,
                estimateCommissionBandwidth = estimateCommissionBandwidth,
                token = tokenType,
                addressEntity = addressEntity,
                commissionResult = commissionResult,
            )
        }

        suspend fun getCommissionAddressData(
            privKeyBytes: ByteArray,
            tokenName: String,
            addressEntity: AddressEntity,
        ): CommissionAddressData {
            val isGeneralAddress = addressRepo.isGeneralAddress(addressEntity.address)
            val centralAddr = centralAddressRepo.getCentralAddress() ?: throw IllegalStateException("Not found cental address")

            return if (tokenName == TokenName.TRX.tokenName || isGeneralAddress) {
                CommissionAddressData(
                    address = addressEntity.address,
                    privateKey = privKeyBytes,
                )
            } else {
                CommissionAddressData(
                    address = centralAddr.address,
                    privateKey = ByteUtils.parseHex(centralAddr.privateKey),
                )
            }
        }

        private fun fail(message: String) = TransferResult.Failure(IllegalStateException(message))

        private suspend fun validateBalances(
            sender: String,
            commissionAddress: CommissionAddressData,
            tokenName: String,
            commission: BigInteger,
            tokenEntity: TokenWithPendingTransactions,
            amount: BigInteger,
        ) {
            val feeBalance =
                if (tokenName == TokenName.TRX.tokenName) {
                    tron.addressUtilities.getTrxBalance(sender).toTokenAmount()
                } else {
                    tron.addressUtilities.getTrxBalance(commissionAddress.address).toTokenAmount()
                }

            if (!tron.addressUtilities.isAddressActivated(sender)) {
                throw IllegalStateException("Для активации необходимо нажать кнопку «Системный TRX»")
            }

            if (feeBalance < commission.toTokenAmount()) {
                val targetAddr =
                    if (tokenName == TokenName.TRX.tokenName) {
                        sender
                    } else {
                        commissionAddress.address
                    }
                throw IllegalStateException("Недостаточно средств для комиссии.\nАдрес: $targetAddr")
            }

            if (tokenEntity.balanceWithoutFrozen.toTokenAmount() < amount.toTokenAmount()) {
                throw IllegalStateException("Сумма транзакции превышает доступную")
            }

            if ((tokenEntity.balanceWithoutFrozen.toTokenAmount() - amount.toTokenAmount()) -
                commission.toTokenAmount() < BigDecimal.ZERO &&
                tokenName == TokenName.TRX.tokenName
            ) {
                throw IllegalStateException("Недостаточно средств с учётом комиссии")
            }

            if (commission.toTokenAmount() <= BigDecimal.ZERO) throw IllegalStateException("Комиссия должна быть больше 0")
        }

        private suspend fun calculateAmountSending(
            receiver: String,
            tokenName: String,
            amount: BigInteger,
            commission: BigInteger,
        ): BigInteger {
            val isReceiverActivated = tron.addressUtilities.isAddressActivated(receiver)

            return when {
                !isReceiverActivated && tokenName == TokenName.TRX.tokenName ->
                    amount - tron.addressUtilities.getCreateNewAccountFeeInSystemContract() - commission
                isReceiverActivated && tokenName == TokenName.TRX.tokenName ->
                    amount - commission
                else -> amount
            }
        }

        private suspend fun getTrxFeeAddress(): String? =
            profPayServerGrpcClient.getServerParameters().fold(
                onSuccess = { it.trxFeeAddress },
                onFailure = {
                    Sentry.captureException(it)
                    null
                },
            )

        private suspend fun signMainTransaction(
            token: TransferToken,
            sender: String,
            receiver: String,
            privateKey: ByteArray,
            amount: BigInteger,
        ): Triple<SignedTransactionData, EstimateEnergyData, EstimateBandwidthData> {
            var energy = EstimateEnergyData(0, BigInteger.ZERO)
            var bandwidth = EstimateBandwidthData(300, 0.0)
            val signedTxn =
                when (token) {
                    TransferToken.USDT_TRC20 ->
                        withContext(dispatcher) {
                            energy = tron.transactions.estimateEnergy(sender, receiver, privateKey, amount)
                            bandwidth = tron.transactions.estimateBandwidth(sender, receiver, privateKey, amount)
                            tron.transactions.getSignedUsdtTransaction(sender, receiver, privateKey, amount)
                        }
                    TransferToken.TRX ->
                        withContext(dispatcher) {
                            bandwidth = tron.transactions.estimateBandwidthTrxTransaction(sender, receiver, privateKey, amount)
                            tron.transactions.getSignedTrxTransaction(sender, receiver, privateKey, amount)
                        }
                    else -> throw IllegalArgumentException("Неподдерживаемый токен")
                }
            return Triple(signedTxn, energy, bandwidth)
        }

        private suspend fun sendGrpcRequest(
            sender: String,
            receiver: String,
            amountSending: BigInteger,
            estimateEnergy: EstimateEnergyData,
            estimateBandwidth: EstimateBandwidthData,
            signedTxnBytes: SignedTransactionData,
            commissionAddressData: CommissionAddressData,
            signedTxnBytesCommission: SignedTransactionData,
            commission: BigInteger,
            estimateCommissionBandwidth: EstimateBandwidthData,
            token: TransferToken,
            addressEntity: AddressEntity,
            commissionResult: TransferProto.EstimateCommissionResponse,
        ): TransferResult =
            withContext(dispatcher) {
                val userId = profileRepo.getProfileUserId()
                try {
                    walletAddressRepo.sendTronTransactionRequestGrpc(
                        userId = userId,
                        transaction =
                            TransactionData
                                .newBuilder()
                                .setAddress(sender)
                                .setReceiverAddress(receiver)
                                .setAmount(amountSending.toByteString())
                                .setEstimateEnergy(estimateEnergy.energy)
                                .setBandwidthRequired(
                                    if (tron.accounts.hasEnoughBandwidth(
                                            sender,
                                            estimateBandwidth.bandwidth,
                                        )
                                    ) {
                                        0
                                    } else {
                                        estimateBandwidth.bandwidth
                                    },
                                ).setTxnBytes(signedTxnBytes.signedTxn)
                                .build(),
                        commission =
                            TransferProto.TransactionCommissionData
                                .newBuilder()
                                .setAddress(commissionAddressData.address)
                                .setBandwidthRequired(
                                    if (tron.accounts.hasEnoughBandwidth(
                                            commissionAddressData.address,
                                            estimateCommissionBandwidth.bandwidth,
                                        )
                                    ) {
                                        0
                                    } else {
                                        estimateCommissionBandwidth.bandwidth
                                    },
                                ).setTxnBytes(signedTxnBytesCommission.signedTxn)
                                .setAmount(commission.toByteString())
                                .addAllCategories(commissionResult.categoriesList)
                                .build(),
                        network = TransferNetwork.MAIN_NET,
                        token = token,
                        txId = signedTxnBytes.txid,
                    )

                    val tokenType = if (token == TransferToken.USDT_TRC20) "USDT" else "TRX"
                    val tokenId =
                        tokenRepo.getTokenIdByAddressIdAndTokenName(
                            addressEntity.addressId!!,
                            tokenType,
                        )

                    pendingTransactionRepo.insert(
                        PendingTransactionEntity(
                            tokenId = tokenId,
                            txid = signedTxnBytes.txid,
                            amount = amountSending,
                        ),
                    )

                    try {
                        val senderAddressEntity = addressRepo.getAddressEntityByAddress(sender)
                        val receiverAddressEntity = addressRepo.getAddressEntityByAddress(receiver)

                        val transactionAddressEntity =
                            when (sender) {
                                senderAddressEntity?.address -> senderAddressEntity
                                receiverAddressEntity?.address -> receiverAddressEntity
                                else -> throw Exception("Не удалось найти адреса")
                            }

                        transactionsRepo.insertNewTransaction(
                            TransactionEntity(
                                txId = signedTxnBytes.txid,
                                senderAddressId = senderAddressEntity?.addressId,
                                receiverAddressId = receiverAddressEntity?.addressId,
                                senderAddress = sender,
                                receiverAddress = receiver,
                                walletId = transactionAddressEntity.walletId,
                                tokenName = tokenType,
                                amount = amountSending,
                                timestamp = System.currentTimeMillis(),
                                status = "Success",
                                type =
                                    assignTransactionType(
                                        idSend = senderAddressEntity?.addressId,
                                        idReceive = receiverAddressEntity?.addressId,
                                    ),
                                statusCode = TransactionStatusCode.PENDING.index,
                                commission = commission,
                            ),
                        )
                    } catch (_: SQLiteConstraintException) {
                    }

                    TransferResult.Success
                } catch (e: Exception) {
                    Sentry.captureException(e)
                    TransferResult.Failure(e)
                }
            }

        data class CommissionAddressData(
            val address: String,
            val privateKey: ByteArray,
        ) {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as CommissionAddressData

                if (address != other.address) return false
                if (!privateKey.contentEquals(other.privateKey)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = address.hashCode()
                result = 31 * result + privateKey.contentHashCode()
                return result
            }
        }
    }

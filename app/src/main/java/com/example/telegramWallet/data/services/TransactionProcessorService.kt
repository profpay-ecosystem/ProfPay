package com.example.telegramWallet.data.services

import com.example.telegramWallet.AppConstants
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.ProfPayServerGrpcClient
import com.example.telegramWallet.bridge.view_model.dto.TokenName
import com.example.telegramWallet.bridge.view_model.dto.transfer.TransferResult
import com.example.telegramWallet.data.database.entities.wallet.AddressEntity
import com.example.telegramWallet.data.database.entities.wallet.PendingTransactionEntity
import com.example.telegramWallet.data.database.models.HasTronCredentials
import com.example.telegramWallet.data.database.models.TokenWithPendingTransactions
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.wallet.AddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.CentralAddressRepo
import com.example.telegramWallet.data.database.repositories.wallet.PendingTransactionRepo
import com.example.telegramWallet.data.database.repositories.wallet.TokenRepo
import com.example.telegramWallet.data.flow_db.repo.WalletAddressRepo
import com.example.telegramWallet.data.utils.toByteString
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.tron.EstimateBandwidthData
import com.example.telegramWallet.tron.EstimateEnergyData
import com.example.telegramWallet.tron.SignedTransactionData
import com.example.telegramWallet.tron.Tron
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.transfer.TransferProto
import org.example.protobuf.transfer.TransferProto.TransactionData
import org.example.protobuf.transfer.TransferProto.TransferNetwork
import org.example.protobuf.transfer.TransferProto.TransferToken
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionProcessorService @Inject constructor(
    private val walletAddressRepo: WalletAddressRepo,
    private val addressRepo: AddressRepo,
    private val centralAddressRepo: CentralAddressRepo,
    private val profileRepo: ProfileRepo,
    private val tokenRepo: TokenRepo,
    private val pendingTransactionRepo: PendingTransactionRepo,
    val tron: Tron,
    grpcClientFactory: GrpcClientFactory
) {
    private val profPayServerGrpcClient: ProfPayServerGrpcClient = grpcClientFactory.getGrpcClient(
        ProfPayServerGrpcClient::class.java,
        AppConstants.Network.GRPC_ENDPOINT,
        AppConstants.Network.GRPC_PORT
    )

    suspend fun sendTransaction(
        sender: String,
        receiver: String,
        amount: BigInteger,
        commission: BigInteger,
        tokenEntity: TokenWithPendingTransactions?,
        commissionResult: TransferProto.EstimateCommissionResponse
    ): TransferResult {
        val tokenName = tokenEntity?.token?.tokenName ?: return fail("Токен не найден")
        val addressEntity = addressRepo.getAddressEntityByAddress(sender) ?: return fail("Адрес отправителя не найден")
        val centralAddr = centralAddressRepo.getCentralAddress() ?: return fail("Центральный адрес не найден.")
        val userId = profileRepo.getProfileUserId()
        val isGeneralAddress = addressRepo.isGeneralAddress(sender)

        if (commission.toTokenAmount() <= BigDecimal.ZERO) return fail("Комиссия должна быть больше 0")

        val commissionAddressEntity = if (tokenName == TokenName.TRX.tokenName || isGeneralAddress) addressEntity else centralAddr

        try {
            validateBalances(sender, commissionAddressEntity, tokenName, commission, tokenEntity, amount)
        } catch (e: IllegalStateException) {
            return fail(e.message ?: "Ошибка валидации балансов")
        }

        val amountSending = calculateAmountSending(receiver, tokenName, amount, commission)

        val trxFeeAddress = getTrxFeeAddress() ?: return fail("Не удалось получить TRX fee адрес")

        val tokenType = if (tokenName == TokenName.TRX.tokenName) TransferToken.TRX else TransferToken.USDT_TRC20

        val (signedTxnBytes, estimateEnergy, estimateBandwidth) =
            signMainTransaction(tokenType, sender, receiver, addressEntity.privateKey, amountSending)

        val signedTxnBytesCommission =
            tron.transactions.getSignedTrxTransaction(
                fromAddress = commissionAddressEntity.address,
                toAddress = trxFeeAddress,
                privateKey = commissionAddressEntity.privateKey,
                amount = commission
            )

        val estimateCommissionBandwidth =
            tron.transactions.estimateBandwidthTrxTransaction(
                fromAddress = commissionAddressEntity.address,
                toAddress = trxFeeAddress,
                privateKey = commissionAddressEntity.privateKey,
                amount = commission
            )

        return sendGrpcRequest(
            userId = userId,
            sender = sender,
            receiver = receiver,
            amountSending = amountSending,
            estimateEnergy = estimateEnergy,
            estimateBandwidth = estimateBandwidth,
            signedTxnBytes = signedTxnBytes,
            commissionAddressEntity = commissionAddressEntity,
            signedTxnBytesCommission = signedTxnBytesCommission,
            commission = commission,
            estimateCommissionBandwidth = estimateCommissionBandwidth,
            token = tokenType,
            addressEntity = addressEntity,
            commissionResult = commissionResult
        )
    }

    private fun fail(message: String) = TransferResult.Failure(IllegalStateException(message))

    private suspend fun validateBalances(
        sender: String,
        commissionAddress: HasTronCredentials,
        tokenName: String,
        commission: BigInteger,
        tokenEntity: TokenWithPendingTransactions,
        amount: BigInteger
    ) {
        val feeBalance = if (tokenName == TokenName.TRX.tokenName) {
            tron.addressUtilities.getTrxBalance(sender).toTokenAmount()
        } else {
            tron.addressUtilities.getTrxBalance(commissionAddress.address).toTokenAmount()
        }

        if (!tron.addressUtilities.isAddressActivated(sender))
            throw IllegalStateException("Для активации необходимо нажать кнопку «Системный TRX»")

        if (feeBalance < commission.toTokenAmount()) {
            val targetAddr = if (tokenName == TokenName.TRX.tokenName) {
                sender
            } else {
                commissionAddress.address
            }
            throw IllegalStateException("Недостаточно средств для комиссии.\nАдрес для пополнения:\n$targetAddr")
        }

        if (tokenEntity.balanceWithoutFrozen.toTokenAmount() < amount.toTokenAmount())
            throw IllegalStateException("Сумма транзакции превышает доступную")

        if ((tokenEntity.balanceWithoutFrozen.toTokenAmount() - amount.toTokenAmount())
            - commission.toTokenAmount() < BigDecimal.ZERO && tokenName == TokenName.TRX.tokenName)
            throw IllegalStateException("Недостаточно средств с учётом комиссии")
    }

    private suspend fun calculateAmountSending(
        receiver: String,
        tokenName: String,
        amount: BigInteger,
        commission: BigInteger
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

    private suspend fun getTrxFeeAddress(): String? {
        return profPayServerGrpcClient.getServerParameters().fold(
            onSuccess = { it.trxFeeAddress },
            onFailure = {
                Sentry.captureException(it)
                null
            }
        )
    }

    private suspend fun signMainTransaction(
        token: TransferToken,
        sender: String,
        receiver: String,
        privateKey: String,
        amount: BigInteger
    ): Triple<SignedTransactionData, EstimateEnergyData, EstimateBandwidthData> {
        var energy = EstimateEnergyData(0, BigInteger.ZERO)
        var bandwidth = EstimateBandwidthData(300, 0.0)
        val signedTxn = when (token) {
            TransferToken.USDT_TRC20 -> withContext(Dispatchers.IO) {
                energy = tron.transactions.estimateEnergy(sender, receiver, privateKey, amount)
                bandwidth = tron.transactions.estimateBandwidth(sender, receiver, privateKey, amount)
                tron.transactions.getSignedUsdtTransaction(sender, receiver, privateKey, amount)
            }
            TransferToken.TRX -> withContext(Dispatchers.IO) {
                bandwidth = tron.transactions.estimateBandwidthTrxTransaction(sender, receiver, privateKey, amount)
                tron.transactions.getSignedTrxTransaction(sender, receiver, privateKey, amount)
            }
            else -> throw IllegalArgumentException("Неподдерживаемый токен")
        }
        return Triple(signedTxn, energy, bandwidth)
    }

    private suspend fun sendGrpcRequest(
        userId: Long,
        sender: String,
        receiver: String,
        amountSending: BigInteger,
        estimateEnergy: EstimateEnergyData,
        estimateBandwidth: EstimateBandwidthData,
        signedTxnBytes: SignedTransactionData,
        commissionAddressEntity: HasTronCredentials,
        signedTxnBytesCommission: SignedTransactionData,
        commission: BigInteger,
        estimateCommissionBandwidth: EstimateBandwidthData,
        token: TransferToken,
        addressEntity: AddressEntity,
        commissionResult: TransferProto.EstimateCommissionResponse
    ): TransferResult {
        return withContext(Dispatchers.IO) {
            try {
                walletAddressRepo.sendTronTransactionRequestGrpc(
                    userId = userId,
                    transaction = TransactionData.newBuilder()
                        .setAddress(sender)
                        .setReceiverAddress(receiver)
                        .setAmount(amountSending.toByteString())
                        .setEstimateEnergy(estimateEnergy.energy)
                        .setBandwidthRequired(
                            if (tron.accounts.hasEnoughBandwidth(
                                    sender,
                                    estimateBandwidth.bandwidth
                                )
                            ) 0 else estimateBandwidth.bandwidth
                        )
                        .setTxnBytes(signedTxnBytes.signedTxn)
                        .build(),
                    commission = TransferProto.TransactionCommissionData.newBuilder()
                        .setAddress(commissionAddressEntity.address)
                        .setBandwidthRequired(
                            if (tron.accounts.hasEnoughBandwidth(
                                    commissionAddressEntity.address,
                                    estimateCommissionBandwidth.bandwidth
                                )
                            ) 0 else estimateCommissionBandwidth.bandwidth
                        )
                        .setTxnBytes(signedTxnBytesCommission.signedTxn)
                        .setAmount(commission.toByteString())
                        .addAllCategories(commissionResult.categoriesList)
                        .build(),
                    network = TransferNetwork.MAIN_NET,
                    token = token,
                    txId = signedTxnBytes.txid
                )

                val tokenType = if (token == TransferToken.USDT_TRC20) "USDT" else "TRX"
                val tokenId = tokenRepo.getTokenIdByAddressIdAndTokenName(
                    addressEntity.addressId!!,
                    tokenType
                )

                pendingTransactionRepo.insert(
                    PendingTransactionEntity(
                        tokenId = tokenId,
                        txid = signedTxnBytes.txid,
                        amount = amountSending
                    )
                )

                TransferResult.Success
            } catch (e: Exception) {
                Sentry.captureException(e)
                TransferResult.Failure(e)
            }
        }
    }
}
package com.profpay.wallet.data.services

import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.AmlGrpcClient
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.backend.grpc.ProfPayServerGrpcClient
import com.profpay.wallet.data.database.entities.wallet.PendingAmlTransactionEntity
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.data.database.repositories.wallet.PendingAmlTransactionRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import com.profpay.wallet.data.utils.toBigInteger
import com.profpay.wallet.data.utils.toSunAmount
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.tron.Tron
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.bitcoinj.base.internal.ByteUtils
import org.server.protobuf.aml.AmlProto.AmlPaymentRequest
import org.server.protobuf.aml.AmlProto.AmlTransactionDetails
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AmlProcessorService
@Inject
constructor(
    private val centralAddressRepo: CentralAddressRepo,
    private val pendingAmlTransactionRepo: PendingAmlTransactionRepo,
    private val tron: Tron,
    private val profileRepo: ProfileRepo,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    grpcClientFactory: GrpcClientFactory,
) {
    private val profPayServerGrpcClient: ProfPayServerGrpcClient =
        grpcClientFactory.getGrpcClient(
            ProfPayServerGrpcClient::class.java,
            AppConstants.Network.GRPC_ENDPOINT,
            AppConstants.Network.GRPC_PORT,
        )

    private val amlClient: AmlGrpcClient =
        grpcClientFactory.getGrpcClient(
            AmlGrpcClient::class.java,
            AppConstants.Network.GRPC_ENDPOINT,
            AppConstants.Network.GRPC_PORT,
        )

    suspend fun processAmlReport(
        address: String,
        txid: String,
    ): Pair<Boolean, String> {
        val centralAddress = centralAddressRepo.getCentralAddress()

        if (centralAddress == null) return Pair(false, "Не удалось получить central address")

        val balance = tron.addressUtilities.getTrxBalance(centralAddress.address)
        val userId = profileRepo.getProfileUserId()

        val serverParameters =
            profPayServerGrpcClient.getServerParameters().fold(
                onSuccess = { it },
                onFailure = {
                    Sentry.captureException(it)
                    return Pair(false, "Сервер недоступен")
                },
            )

        val amlFeeValue = serverParameters.amlFee
        val trxFeeAddress = serverParameters.trxFeeAddress

        if (balance.toTokenAmount() < amlFeeValue.toBigInteger().toTokenAmount()) {
            return Pair(false, "Недостаточно средств на балансе.\nНеобходимо: ${amlFeeValue.toBigInteger().toTokenAmount()} TRX")
        }

        val signedTxnBytes = withContext(ioDispatcher) {
            tron.transactions.getSignedTrxTransaction(
                fromAddress = centralAddress.address,
                toAddress = trxFeeAddress,
                privateKey = ByteUtils.parseHex(centralAddress.privateKey),
                amount = amlFeeValue.toBigInteger().toTokenAmount().toSunAmount(),
            )
        }
        val estimateBandwidth = withContext(ioDispatcher) {
            tron.transactions.estimateBandwidth(
                fromAddress = centralAddress.address,
                toAddress = trxFeeAddress,
                privateKey = ByteUtils.parseHex(centralAddress.privateKey),
                amount = amlFeeValue.toBigInteger().toTokenAmount().toSunAmount(),
            )
        }

        executeAmlPayment(
            AmlPaymentRequest
                .newBuilder()
                .setUserId(userId)
                .setTx(txid)
                .setAddress(address)
                .setTransaction(
                    AmlTransactionDetails
                        .newBuilder()
                        .setAddress(centralAddress.address)
                        .setBandwidthRequired(estimateBandwidth.bandwidth)
                        .setTxnBytes(signedTxnBytes.signedTxn)
                        .build(),
                ).build(),
        )

        return Pair(true, "Успешное действие, ожидайте уведомление.")
    }

    private suspend fun executeAmlPayment(request: AmlPaymentRequest) {
        try {
            val result = amlClient.processAmlPayment(request)
            result.fold(
                onSuccess = {
                    pendingAmlTransactionRepo.insert(
                        PendingAmlTransactionEntity(
                            txid = request.tx,
                        ),
                    )
                },
                onFailure = {
                    Sentry.captureException(it)
                },
            )
        } catch (e: Exception) {
            Sentry.captureException(e)
        }
    }
}

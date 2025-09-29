package com.example.telegramWallet.data.flow_db.repo

import android.util.Log
import com.example.telegramWallet.AppConstants
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.TransferGrpcClient
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.exceptions.payments.GrpcClientErrorSendTransactionExcpetion
import com.example.telegramWallet.exceptions.payments.GrpcServerErrorSendTransactionExcpetion
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.example.protobuf.transfer.TransferProto
import org.example.protobuf.transfer.TransferProto.TransactionCommissionData
import org.example.protobuf.transfer.TransferProto.TransactionData
import org.example.protobuf.transfer.TransferProto.TransferNetwork
import javax.inject.Inject

interface WalletAddressRepo {
    val estimateCommission: Flow<EstimateCommissionResult>
    val transactionStatus: Flow<TransactionStatusResult>

    suspend fun sendTronTransactionRequestGrpc(
        userId: Long,
        transaction: TransactionData,
        commission: TransactionCommissionData?,
        network: TransferNetwork,
        token: TransferProto.TransferToken,
        txId: String,
    )

    suspend fun estimateCommission(
        address: String,
        bandwidth: Long,
        energy: Long,
    )

    suspend fun getTransactionStatus(txId: String)
}

sealed class EstimateCommissionResult {
    data class Success(
        val response: TransferProto.EstimateCommissionResponse,
    ) : EstimateCommissionResult()

    data class Error(
        val throwable: Throwable,
    ) : EstimateCommissionResult()

    data object Loading : EstimateCommissionResult()

    data object Empty : EstimateCommissionResult()
}

sealed class TransactionStatusResult {
    data class Success(
        val response: TransferProto.TransactionStatusResponse,
    ) : TransactionStatusResult()

    data class Error(
        val throwable: Throwable,
    ) : TransactionStatusResult()

    data object Loading : TransactionStatusResult()

    data object Empty : TransactionStatusResult()
}

class WalletAddressRepoImpl
    @Inject
    constructor(
        private val profileRepo: ProfileRepo,
        grpcClientFactory: GrpcClientFactory,
    ) : WalletAddressRepo {
        private val _estimateCommission = MutableSharedFlow<EstimateCommissionResult>(replay = 1)
        override val estimateCommission: Flow<EstimateCommissionResult> = _estimateCommission.asSharedFlow()

        private val _transactionStatus = MutableSharedFlow<TransactionStatusResult>(replay = 1)
        override val transactionStatus: Flow<TransactionStatusResult> = _transactionStatus.asSharedFlow()

        private val transferClient: TransferGrpcClient =
            grpcClientFactory.getGrpcClient(
                TransferGrpcClient::class.java,
                AppConstants.Network.GRPC_ENDPOINT,
                AppConstants.Network.GRPC_PORT,
            )

        override suspend fun estimateCommission(
            address: String,
            bandwidth: Long,
            energy: Long,
        ) {
            val userId = profileRepo.getProfileUserId()

            try {
                val result = transferClient.estimateCommission(userId, address, bandwidth = bandwidth, energy = energy)
                result.fold(
                    onSuccess = {
                        println(it)
                        _estimateCommission.emit(EstimateCommissionResult.Success(it))
                    },
                    onFailure = {
                        println(it)
                        _estimateCommission.emit(EstimateCommissionResult.Error(it))
                    },
                )
            } catch (e: Exception) {
                Sentry.captureException(RuntimeException(e.message ?: "Пустое сообщение, комиссия.", e))
            }
        }

        override suspend fun getTransactionStatus(txId: String) {
            try {
                val result = transferClient.getTransactionStatus(txId)
                result.fold(
                    onSuccess = {
                        _transactionStatus.emit(TransactionStatusResult.Success(it))
                    },
                    onFailure = {
                        _transactionStatus.emit(TransactionStatusResult.Error(it))
                    },
                )
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        override suspend fun sendTronTransactionRequestGrpc(
            userId: Long,
            transaction: TransactionData,
            commission: TransactionCommissionData?,
            network: TransferNetwork,
            token: TransferProto.TransferToken,
            txId: String,
        ) {
            try {
                val result =
                    transferClient.sendTronTransactionRequest(
                        userId = userId,
                        transaction = transaction,
                        commission = commission,
                        network = network,
                        transferToken = token,
                        txId = txId,
                    )
                result.fold(
                    onSuccess = {
                        Log.d("sendTronTransactionRequestGrpc", it.timestamp.toString())
                    },
                    onFailure = {
                        val message = it.message ?: "Unknown gRPC error"
                        val cause = it.cause ?: Throwable("No cause provided")
                        val exception = GrpcServerErrorSendTransactionExcpetion(message, cause)

                        Sentry.captureException(exception)
                        throw exception
                    },
                )
            } catch (e: Exception) {
                val message = e.message ?: "Unknown client error"
                val cause = e.cause ?: Throwable("No cause provided")
                val exception = GrpcClientErrorSendTransactionExcpetion(message, cause)

                Sentry.captureException(exception)
                throw exception
            }
        }
    }

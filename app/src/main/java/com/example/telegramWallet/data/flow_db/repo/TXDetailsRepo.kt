package com.example.telegramWallet.data.flow_db.repo

import android.util.Log
import com.example.telegramWallet.AppConstants
import com.example.telegramWallet.backend.grpc.AmlGrpcClient
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.TransferGrpcClient
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.database.repositories.TransactionsRepo
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
import org.server.protobuf.aml.AmlProto
import javax.inject.Inject

interface TXDetailsRepo {
    val aml: Flow<AmlResult>
    suspend fun getAmlFromTransactionId(address: String, tx: String, tokenName: String)
    suspend fun renewAmlFromTransactionId(address: String, tx: String, tokenName: String)
    suspend fun processAmlPayment(request: AmlProto.AmlPaymentRequest)
}

sealed class AmlResult {
    data class Success(val response: AmlProto.GetAmlByTxIdResponse) : AmlResult()
    data class Error(val throwable: Throwable) : AmlResult()
    data object Loading : AmlResult()
    data object Empty : AmlResult()
}

class TXDetailsRepoImpl @Inject constructor(
    private val profileRepo: ProfileRepo,
    grpcClientFactory: GrpcClientFactory
): TXDetailsRepo {
    private val _aml = MutableSharedFlow<AmlResult>(replay = 1)
    override val aml: Flow<AmlResult> = _aml.asSharedFlow()

    private val amlClient: AmlGrpcClient = grpcClientFactory.getGrpcClient(
        AmlGrpcClient::class.java,
        AppConstants.Network.GRPC_ENDPOINT,
        AppConstants.Network.GRPC_PORT
    )

    override suspend fun getAmlFromTransactionId(address: String, tx: String, tokenName: String) {
        val userId = profileRepo.getProfileUserId()
        try {
            val result = amlClient.getAmlFromTransactionId(userId = userId, address = address, tx = tx, tokenName = tokenName)
            result.fold(
                onSuccess = {
                    _aml.emit(AmlResult.Success(it))
                },
                onFailure = {
                    _aml.emit(AmlResult.Error(it))
                }
            )
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    override suspend fun renewAmlFromTransactionId(address: String, tx: String, tokenName: String) {
        val userId = profileRepo.getProfileUserId()
        try {
            val result = amlClient.renewAmlFromTransactionId(userId = userId, address = address, tx = tx, tokenName = tokenName)
            result.fold(
                onSuccess = {
                    _aml.emit(AmlResult.Success(it))
                },
                onFailure = {
                    _aml.emit(AmlResult.Error(it))
                }
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch transfer service", e)
        }
    }

    override suspend fun processAmlPayment(request: AmlProto.AmlPaymentRequest) {
        try {
            val result = amlClient.processAmlPayment(request)
            result.fold(
                onSuccess = {
                    println(it.timestamp)
                },
                onFailure = {
                    // TODO: Создать кастом
                    throw RuntimeException(it)
                }
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch transfer service", e)
        }
    }
}
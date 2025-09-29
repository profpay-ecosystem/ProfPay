package com.example.telegramWallet.backend.grpc

import com.example.telegramWallet.data.flow_db.token.SharedPrefsTokenProvider
import com.example.telegramWallet.utils.safeGrpcCall
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.server.protobuf.aml.AmlProto
import org.server.protobuf.aml.AmlServiceGrpc

class AmlGrpcClient(
    private val channel: ManagedChannel,
    val token: SharedPrefsTokenProvider,
) {
    private val stub: AmlServiceGrpc.AmlServiceBlockingStub = AmlServiceGrpc.newBlockingStub(channel)

    suspend fun getAmlFromTransactionId(
        userId: Long,
        address: String,
        tx: String,
        tokenName: String,
    ): Result<AmlProto.GetAmlByTxIdResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val request =
                    AmlProto.GetAmlByTxIdRequest
                        .newBuilder()
                        .setUserId(userId)
                        .setAddress(address)
                        .setTx(tx)
                        .setTokenName(tokenName)
                        .build()

                val response = stub.getAmlFromTransactionId(request)
                Result.success(response)
            }
        }

    suspend fun renewAmlFromTransactionId(
        userId: Long,
        address: String,
        tx: String,
        tokenName: String,
    ): Result<AmlProto.GetAmlByTxIdResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val request =
                    AmlProto.GetAmlByTxIdRequest
                        .newBuilder()
                        .setUserId(userId)
                        .setAddress(address)
                        .setTx(tx)
                        .setTokenName(tokenName)
                        .build()

                val response = stub.renewAmlFromTransactionId(request)
                Result.success(response)
            }
        }

    suspend fun processAmlPayment(request: AmlProto.AmlPaymentRequest): Result<AmlProto.AmlPaymentResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val response = stub.processAmlPayment(request)
                Result.success(response)
            }
        }

    fun shutdown() {
        channel.shutdown()
    }
}

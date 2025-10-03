package com.profpay.wallet.backend.grpc

import com.profpay.wallet.data.flow_db.token.SharedPrefsTokenProvider
import com.profpay.wallet.utils.safeGrpcCall
import io.grpc.ManagedChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.server.protobuf.aml.AmlProto
import org.server.protobuf.aml.AmlServiceGrpc

class AmlGrpcClient(
    private val channel: ManagedChannel,
    val token: SharedPrefsTokenProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val stub: AmlServiceGrpc.AmlServiceBlockingStub = AmlServiceGrpc.newBlockingStub(channel)

    suspend fun getAmlFromTransactionId(
        userId: Long,
        address: String,
        tx: String,
        tokenName: String,
    ): Result<AmlProto.GetAmlByTxIdResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
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
            withContext(ioDispatcher) {
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
            withContext(ioDispatcher) {
                val response = stub.processAmlPayment(request)
                Result.success(response)
            }
        }

    fun shutdown() {
        channel.shutdown()
    }
}

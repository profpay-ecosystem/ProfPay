package com.profpay.wallet.backend.grpc

import com.google.protobuf.Empty
import com.profpay.wallet.data.di.token.SharedPrefsTokenProvider
import com.profpay.wallet.utils.safeGrpcCall
import io.grpc.ManagedChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.server.protobuf.prof_pay_server.ProfPayServerGrpc
import org.server.protobuf.prof_pay_server.ProfPayServerProto

class ProfPayServerGrpcClient(
    private val channel: ManagedChannel,
    val token: SharedPrefsTokenProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val stub: ProfPayServerGrpc.ProfPayServerBlockingStub = ProfPayServerGrpc.newBlockingStub(channel)

    suspend fun getServerStatus(): Result<ProfPayServerProto.GetServerStatusResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
                val response = stub.getServerStatus(Empty.newBuilder().build())
                Result.success(response)
            }
        }

    suspend fun getServerParameters(): Result<ProfPayServerProto.GetServerParametersResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
                val response = stub.getServerParameters(Empty.newBuilder().build())
                Result.success(response)
            }
        }

    fun shutdown() {
        channel.shutdown()
    }
}

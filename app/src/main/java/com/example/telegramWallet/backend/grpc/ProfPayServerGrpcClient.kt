package com.example.telegramWallet.backend.grpc

import com.example.telegramWallet.data.flow_db.token.SharedPrefsTokenProvider
import com.example.telegramWallet.utils.safeGrpcCall
import com.google.protobuf.Empty
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.server.protobuf.prof_pay_server.ProfPayServerGrpc
import org.server.protobuf.prof_pay_server.ProfPayServerProto

class ProfPayServerGrpcClient(
    private val channel: ManagedChannel,
    val token: SharedPrefsTokenProvider,
) {
    private val stub: ProfPayServerGrpc.ProfPayServerBlockingStub = ProfPayServerGrpc.newBlockingStub(channel)

    suspend fun getServerStatus(): Result<ProfPayServerProto.GetServerStatusResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val response = stub.getServerStatus(Empty.newBuilder().build())
                Result.success(response)
            }
        }

    suspend fun getServerParameters(): Result<ProfPayServerProto.GetServerParametersResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val response = stub.getServerParameters(Empty.newBuilder().build())
                Result.success(response)
            }
        }

    fun shutdown() {
        channel.shutdown()
    }
}

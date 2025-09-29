package com.profpay.wallet.backend.grpc

import com.profpay.wallet.data.flow_db.token.SharedPrefsTokenProvider
import com.profpay.wallet.utils.safeGrpcCall
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.server.protobuf.auth.AuthProto
import org.server.protobuf.auth.AuthServiceGrpc

class AuthGrpcClient(
    private val channel: ManagedChannel,
    val token: SharedPrefsTokenProvider,
) {
    private val stub: AuthServiceGrpc.AuthServiceBlockingStub = AuthServiceGrpc.newBlockingStub(channel)

    suspend fun issueTokens(
        appId: String,
        userId: Long,
        deviceToken: String,
    ): Result<AuthProto.IssueTokensResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val request =
                    AuthProto.IssueTokensRequest
                        .newBuilder()
                        .setAppId(appId)
                        .setUserId(userId)
                        .setDeviceToken(deviceToken)
                        .build()

                val response = stub.issueTokens(request)
                Result.success(response)
            }
        }

    suspend fun refreshTokenPair(
        refreshToken: String,
        userId: Long,
        deviceToken: String,
    ): Result<AuthProto.RefreshTokenPairResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val request =
                    AuthProto.RefreshTokenPairRequest
                        .newBuilder()
                        .setRefreshToken(refreshToken)
                        .setUserId(userId)
                        .setDeviceToken(deviceToken)
                        .build()

                val response = stub.refreshTokenPair(request)
                Result.success(response)
            }
        }

    fun shutdown() {
        channel.shutdown()
    }
}

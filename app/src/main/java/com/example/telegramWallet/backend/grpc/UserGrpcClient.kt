package com.example.telegramWallet.backend.grpc

import com.example.telegramWallet.data.flow_db.token.SharedPrefsTokenProvider
import com.example.telegramWallet.utils.safeGrpcCall
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.user.UserProto
import org.example.protobuf.user.UserServiceGrpc

class UserGrpcClient(
    private val channel: ManagedChannel,
    val token: SharedPrefsTokenProvider,
) {
    private val stub: UserServiceGrpc.UserServiceBlockingStub = UserServiceGrpc.newBlockingStub(channel)

    suspend fun setUserLegalConsentsTrue(appId: String): Result<UserProto.UserLegalConsentsResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val request =
                    UserProto.UserLegalConsentsRequest
                        .newBuilder()
                        .setAppId(appId)
                        .build()
                val response = stub.setUserLegalConsentsTrue(request)
                Result.success(response)
            }
        }

    suspend fun registerUser(
        appId: String,
        deviceToken: String,
    ): Result<UserProto.RegisterUserResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val request =
                    UserProto.RegisterUserRequest
                        .newBuilder()
                        .setAppId(appId)
                        .setDeviceToken(deviceToken)
                        .build()
                val response = stub.registerUser(request)
                Result.success(response)
            }
        }

    suspend fun updateUserDeviceToken(
        appId: String,
        deviceToken: String,
    ): Result<UserProto.UserTelegramDataResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val request =
                    UserProto.UpdateUserDeviceTokenRequest
                        .newBuilder()
                        .setAppId(appId)
                        .setDeviceToken(deviceToken)
                        .build()
                val response = stub.updateUserDeviceToken(request)
                Result.success(response)
            }
        }

    suspend fun getUserTelegramData(appId: String): Result<UserProto.UserTelegramDataResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val request =
                    UserProto.UserTelegramDataRequest
                        .newBuilder()
                        .setAppId(appId)
                        .build()
                val response = stub.getUserTelegramData(request)
                Result.success(response)
            }
        }

    suspend fun registerUserDevice(
        userId: Long,
        appId: String,
        deviceToken: String,
    ): Result<UserProto.RegisterUserDeviceResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val request =
                    UserProto.RegisterUserDeviceRequest
                        .newBuilder()
                        .setUserId(userId)
                        .setAppId(appId)
                        .setDeviceToken(deviceToken)
                        .build()
                val response = stub.registerUserDevice(request)
                Result.success(response)
            }
        }

    suspend fun isUserExists(userId: Long): Result<UserProto.IsUserExistsResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val request =
                    UserProto.IsUserExistsRequest
                        .newBuilder()
                        .setUserId(userId)
                        .build()
                val response = stub.isUserExists(request)
                Result.success(response)
            }
        }

    suspend fun getUserPermissions(
        appId: String,
        deviceToken: String,
    ): Result<UserProto.GetUserPermissionsResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val request =
                    UserProto.GetUserPermissionsRequest
                        .newBuilder()
                        .setAppId(appId)
                        .setDeviceToken(deviceToken)
                        .build()
                val response = stub.getUserPermissions(request)
                Result.success(response)
            }
        }

    fun shutdown() {
        channel.shutdown()
    }
}

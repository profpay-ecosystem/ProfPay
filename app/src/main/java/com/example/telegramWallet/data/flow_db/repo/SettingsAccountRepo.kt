package com.example.telegramWallet.data.flow_db.repo

import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.UserGrpcClient
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.example.protobuf.user.UserProto.UserTelegramDataResponse
import javax.inject.Inject

interface SettingsAccountRepo {
    val telegramAccount: Flow<UserTelegramDataResponse>
    suspend fun getUserTelegramData()
}

class SettingsAccountRepoImpl @Inject constructor(
    private val profileRepo: ProfileRepo,
    grpcClientFactory: GrpcClientFactory
) : SettingsAccountRepo {
    private val _telegramAccount = MutableSharedFlow<UserTelegramDataResponse>(replay = 1)
    override val telegramAccount: Flow<UserTelegramDataResponse> = _telegramAccount.asSharedFlow()

    private val userGrpcClient: UserGrpcClient = grpcClientFactory.getGrpcClient(
        UserGrpcClient::class.java,
        "grpc.wallet-services-srv.com",
        8443
    )

    override suspend fun getUserTelegramData() {
        val result = userGrpcClient.getUserTelegramData(profileRepo.getProfileAppId())
        result.fold(
            onSuccess = {
                profileRepo.updateProfileTelegramIdAndUsername(
                    telegramId = it.telegramId,
                    username = it.username
                )
                _telegramAccount.emit(it)
            },
            onFailure = {
                // TODO: Создать кастом
                Sentry.captureException(it)
                throw RuntimeException(it)
            }
        )
    }
}
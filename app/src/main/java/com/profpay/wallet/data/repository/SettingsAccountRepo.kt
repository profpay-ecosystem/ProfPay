package com.profpay.wallet.data.repository

import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.backend.grpc.UserGrpcClient
import com.profpay.wallet.data.database.repositories.ProfileRepo
import io.sentry.Sentry
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

interface SettingsAccountRepo {
    suspend fun getUserTelegramData()
}

class SettingsAccountRepoImpl @Inject constructor(
    private val profileRepo: ProfileRepo,
    grpcClientFactory: GrpcClientFactory,
) : SettingsAccountRepo {
    private val userGrpcClient: UserGrpcClient =
        grpcClientFactory.getGrpcClient(
            UserGrpcClient::class.java,
            AppConstants.Network.GRPC_ENDPOINT,
            AppConstants.Network.GRPC_PORT,
        )

    override suspend fun getUserTelegramData() {
        val result = userGrpcClient.getUserTelegramData(profileRepo.getProfileAppId())
        result.fold(
            onSuccess = {
                profileRepo.updateProfileTelegramIdAndUsername(
                    telegramId = it.telegramId,
                    username = it.username,
                )
            },
            onFailure = { throwable ->
                if (throwable is CancellationException) {
                    throw throwable
                } else {
                    Sentry.captureException(throwable)
                    throw RuntimeException(throwable)
                }
            },
        )
    }
}

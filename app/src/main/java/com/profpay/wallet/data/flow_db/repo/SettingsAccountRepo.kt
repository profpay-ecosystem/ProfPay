package com.profpay.wallet.data.flow_db.repo

import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.backend.grpc.UserGrpcClient
import com.profpay.wallet.data.database.repositories.ProfileRepo
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.example.protobuf.user.UserProto.UserTelegramDataResponse
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

interface SettingsAccountRepo {
    val telegramAccount: Flow<UserTelegramDataResponse>

    suspend fun getUserTelegramData()
}

class SettingsAccountRepoImpl
    @Inject
    constructor(
        private val profileRepo: ProfileRepo,
        grpcClientFactory: GrpcClientFactory,
    ) : SettingsAccountRepo {
        private val _telegramAccount = MutableSharedFlow<UserTelegramDataResponse>(replay = 1)
        override val telegramAccount: Flow<UserTelegramDataResponse> = _telegramAccount.asSharedFlow()

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
                    _telegramAccount.emit(it)
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

package com.profpay.wallet.data.repository.flow

import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.backend.grpc.UserGrpcClient
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.exceptions.payments.GrpcClientErrorSendTransactionExcpetion
import com.profpay.wallet.exceptions.payments.GrpcServerErrorSendTransactionExcpetion
import io.sentry.Sentry
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.io.IOException
import javax.inject.Inject

interface AppAccessRepo {
    val accessStateFlow: SharedFlow<AppAccessState>
    suspend fun isAppRestricted()
}

class AppAccessRepoImpl @Inject constructor(
    private val profileRepo: ProfileRepo,
    grpcClientFactory: GrpcClientFactory,
) : AppAccessRepo {
    private val userClient: UserGrpcClient =
        grpcClientFactory.getGrpcClient(
            UserGrpcClient::class.java,
            AppConstants.Network.GRPC_ENDPOINT,
            AppConstants.Network.GRPC_PORT,
        )

    // SharedFlow, на который подписывается UI
    private val _accessStateFlow = MutableSharedFlow<AppAccessState>(
        replay = 1,
        extraBufferCapacity = 1
    ).apply {
        tryEmit(AppAccessState.Allowed)
    }
    override val accessStateFlow: SharedFlow<AppAccessState> = _accessStateFlow.asSharedFlow()

    override suspend fun isAppRestricted() {
        val telegramID = profileRepo.getProfileTelegramId()
        if (telegramID == null || telegramID == 0L) return

        val appId = profileRepo.getProfileAppId()
        val deviceToken = profileRepo.getDeviceToken()
        try {
            val result =
                userClient.getUserPermissions(
                    appId = appId,
                    deviceToken = deviceToken!!,
                )

            result.fold(
                onSuccess = {
                    val newState = if (it.isAppAllowed) {
                        AppAccessState.Allowed
                    } else {
                        AppAccessState.Restricted
                    }
                    _accessStateFlow.emit(newState)
                },
                onFailure = {
                    val message = it.message ?: "Unknown gRPC error"
                    val cause = it.cause ?: Throwable("No cause provided")
                    val exception = GrpcServerErrorSendTransactionExcpetion(message, cause)

                    Sentry.captureException(exception)
                    throw exception
                },
            )
        } catch (e: Exception) {
            if (e is IOException) {
                _accessStateFlow.emit(AppAccessState.NoInternet)
            }

            val message = e.message ?: "Unknown client error"
            val cause = e.cause ?: Throwable("No cause provided")
            val exception = GrpcClientErrorSendTransactionExcpetion(message, cause)

            Sentry.captureException(exception)
        }
    }
}

enum class AppAccessState {
    Allowed,
    NoInternet,
    Blocked,
    Restricted
}

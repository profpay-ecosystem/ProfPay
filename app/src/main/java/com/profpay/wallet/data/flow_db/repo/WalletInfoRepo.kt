package com.profpay.wallet.data.flow_db.repo

import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.navigation.NavController
import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.backend.grpc.UserGrpcClient
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.exceptions.payments.GrpcClientErrorSendTransactionExcpetion
import com.profpay.wallet.exceptions.payments.GrpcServerErrorSendTransactionExcpetion
import com.profpay.wallet.ui.app.navigation.graphs.navGraph.WalletInfo
import io.sentry.Sentry
import javax.inject.Inject

interface WalletInfoRepo {
    suspend fun getUserPermissions(
        sharedPrefs: SharedPreferences,
        navController: NavController,
    )
}

class WalletInfoRepoImpl
    @Inject
    constructor(
        private val profileRepo: ProfileRepo,
        grpcClientFactory: GrpcClientFactory,
    ) : WalletInfoRepo {
        private val userClient: UserGrpcClient =
            grpcClientFactory.getGrpcClient(
                UserGrpcClient::class.java,
                AppConstants.Network.GRPC_ENDPOINT,
                AppConstants.Network.GRPC_PORT,
            )

        override suspend fun getUserPermissions(
            sharedPrefs: SharedPreferences,
            navController: NavController,
        ) {
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
                        sharedPrefs.edit(commit = true) {
                            putBoolean("is_blocked_app", !it.isAppAllowed)
                            if (!it.isAppAllowed) {
                                navController.navigate(WalletInfo.BlockedAppScreen.route) {
                                    popUpTo(0) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
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
                val message = e.message ?: "Unknown client error"
                val cause = e.cause ?: Throwable("No cause provided")
                val exception = GrpcClientErrorSendTransactionExcpetion(message, cause)

                Sentry.captureException(exception)
                throw exception
            }
        }
    }

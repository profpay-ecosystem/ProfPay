package com.profpay.wallet.bridge.view_model.welcoming_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.backend.grpc.UserGrpcClient
import com.profpay.wallet.data.database.repositories.ProfileRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class WelcomingViewModel
    @Inject
    constructor(
        private val profileRepo: ProfileRepo,
        grpcClientFactory: GrpcClientFactory,
        private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
    ) : ViewModel() {
        private val userGrpcClient: UserGrpcClient =
            grpcClientFactory.getGrpcClient(
                UserGrpcClient::class.java,
                AppConstants.Network.GRPC_ENDPOINT,
                AppConstants.Network.GRPC_PORT,
            )

        suspend fun setUserLegalConsentsTrue() {
            try {
                val appId = profileRepo.getProfileAppId()
                return withContext(dispatcher) {
                    try {
                        val result = userGrpcClient.setUserLegalConsentsTrue(appId)
                        result.fold(
                            onSuccess = { _ ->
                                true
                            },
                            onFailure = { exception ->
                                Sentry.captureException(exception)
                                Log.e("gRPC ERROR", "Error during gRPC call: ${exception.message}")
                                false
                            },
                        )
                    } catch (e: Exception) {
                        Sentry.captureException(e)
                        Log.e("gRPC ERROR", "Error during gRPC call: ${e.message}")
                        false
                    }
                }
            } catch (e: Exception) {
                return
            }
        }
    }

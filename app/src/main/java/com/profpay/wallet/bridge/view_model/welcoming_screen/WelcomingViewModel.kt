package com.profpay.wallet.bridge.view_model.welcoming_screen

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.backend.grpc.UserGrpcClient
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomingViewModel @Inject constructor(
    private val profileRepo: ProfileRepo,
    grpcClientFactory: GrpcClientFactory,
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val userGrpcClient: UserGrpcClient =
        grpcClientFactory.getGrpcClient(
            UserGrpcClient::class.java,
            AppConstants.Network.GRPC_ENDPOINT,
            AppConstants.Network.GRPC_PORT,
        )

    fun setUserLegalConsentsTrue() = viewModelScope.launch(ioDispatcher) {
        runCatching {
            val appId = profileRepo.getProfileAppId()
            userGrpcClient.setUserLegalConsentsTrue(appId)
        }.onSuccess { result ->
            result.fold(
                onSuccess = { /* ok */ },
                onFailure = { e ->
                    Sentry.captureException(e)
                    Log.e("setUserLegalConsentsTrue", "gRPC call failed: ${e.message}")
                }
            )
        }.onFailure { e ->
            Sentry.captureException(e)
            Log.e("setUserLegalConsentsTrue", "Error: ${e.message}")
        }
    }
}

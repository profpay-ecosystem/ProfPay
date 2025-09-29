package com.example.telegramWallet.data.flow_db.repo

import com.example.telegramWallet.AppConstants
import com.example.telegramWallet.backend.grpc.CryptoAddressGrpcClient
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import javax.inject.Inject

interface WalletSotRepo {
    suspend fun updateDerivedIndex(
        appId: String,
        oldIndex: Long,
        newIndex: Long,
        generalAddress: String,
    )
}

class WalletSotRepoImpl
    @Inject
    constructor(
        grpcClientFactory: GrpcClientFactory,
    ) : WalletSotRepo {
        private val cryptoAddressGrpcClient: CryptoAddressGrpcClient =
            grpcClientFactory.getGrpcClient(
                CryptoAddressGrpcClient::class.java,
                AppConstants.Network.GRPC_ENDPOINT,
                AppConstants.Network.GRPC_PORT,
            )

        override suspend fun updateDerivedIndex(
            appId: String,
            oldIndex: Long,
            newIndex: Long,
            generalAddress: String,
        ) {
            try {
                val result =
                    cryptoAddressGrpcClient.updateDerivedIndex(
                        appId = appId,
                        oldIndex = oldIndex,
                        newIndex = newIndex,
                        generalAddress = generalAddress,
                    )
                result.fold(
                    onSuccess = {
                        println(it)
                    },
                    onFailure = {
                        // TODO: Создать кастом
                        throw RuntimeException(it)
                    },
                )
            } catch (e: Exception) {
                throw RuntimeException("Failed to fetch smart contracts", e)
            }
        }
    }

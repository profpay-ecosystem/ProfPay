package com.profpay.wallet.data.repository

import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.CryptoAddressGrpcClient
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import org.example.protobuf.address.CryptoAddressProto
import javax.inject.Inject

interface WalletSotRepo {
    suspend fun updateDerivedIndex(request: CryptoAddressProto.UpdateDerivedIndexRequest)
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
            request: CryptoAddressProto.UpdateDerivedIndexRequest
        ) {
            try {
                val result =
                    cryptoAddressGrpcClient.updateDerivedIndex(
                        request = request
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

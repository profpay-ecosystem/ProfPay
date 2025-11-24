package com.profpay.wallet.data.repository

import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.CryptoAddressGrpcClient
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.database.repositories.wallet.CentralAddressRepo
import com.profpay.wallet.exceptions.payments.GrpcClientErrorSendTransactionExcpetion
import com.profpay.wallet.exceptions.payments.GrpcServerErrorSendTransactionExcpetion
import com.profpay.wallet.tron.Tron
import io.sentry.Sentry
import org.example.protobuf.address.CryptoAddressProto
import javax.inject.Inject

interface ReissueCentralAddressRepo {
    suspend fun changeCentralAddress()
}

class ReissueCentralAddressRepoImpl @Inject constructor(
    private val profileRepo: ProfileRepo,
    private var centralAddressRepo: CentralAddressRepo,
    grpcClientFactory: GrpcClientFactory,
    private val tron: Tron,
) : ReissueCentralAddressRepo {
    private val cryptoAddressGrpcClient: CryptoAddressGrpcClient =
        grpcClientFactory.getGrpcClient(
            CryptoAddressGrpcClient::class.java,
            AppConstants.Network.GRPC_ENDPOINT,
            AppConstants.Network.GRPC_PORT,
        )

    override suspend fun changeCentralAddress() {
        val appId = profileRepo.getProfileAppId()
        val address = tron.addressUtilities.generateSingleAddress()

        try {
            val result = cryptoAddressGrpcClient.addCentralAddress(
                CryptoAddressProto.AddCentralAddressRequest.newBuilder()
                    .setAppId(appId)
                    .setAddress(address.address)
                    .setPubKey(address.publicKey)
                    .build()
            )
            result.fold(
                onSuccess = {
                    centralAddressRepo.changeCentralAddress(
                        address = address.address,
                        publicKey = address.publicKey,
                        privateKey = address.privateKey,
                    )
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

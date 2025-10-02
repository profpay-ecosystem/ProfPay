package com.profpay.wallet.bridge.view_model.smart_contract.usecases

import com.profpay.wallet.AppConstants
import com.profpay.wallet.backend.grpc.GrpcClientFactory
import com.profpay.wallet.backend.grpc.ProfPayServerGrpcClient
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.tron.Tron
import com.google.protobuf.ByteString
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.smart.SmartContractProto
import java.math.BigInteger
import javax.inject.Inject

class CommissionFeeBuilder
    @Inject
    constructor(
        private val addressRepo: AddressRepo,
        private val tron: Tron,
        grpcClientFactory: GrpcClientFactory,
    ) {
        private val profPayServerGrpcClient: ProfPayServerGrpcClient =
            grpcClientFactory.getGrpcClient(
                ProfPayServerGrpcClient::class.java,
                AppConstants.Network.GRPC_ENDPOINT,
                AppConstants.Network.GRPC_PORT,
            )

        suspend fun build(
            commission: BigInteger,
            userId: Long,
            deal: SmartContractProto.ContractDealListResponse,
        ): CommissionFeeBuilderResult {
            val address =
                if (userId == deal.buyer.userId) {
                    deal.buyer.address
                } else {
                    deal.seller.address
                }

            val addressData =
                withContext(Dispatchers.IO) {
                    addressRepo.getAddressEntityByAddress(address)
                } ?: return CommissionFeeBuilderResult.Error("Address data is null")

            val trxFeeAddress =
                profPayServerGrpcClient.getServerParameters().fold(
                    onSuccess = {
                        it.trxFeeAddress
                    },
                    onFailure = {
                        Sentry.captureException(it)
                        throw RuntimeException(it)
                    },
                )

            val signedTxnBytesCommission =
                withContext(Dispatchers.IO) {
                    tron.transactions.getSignedTrxTransaction(
                        fromAddress = addressData.address,
                        toAddress = trxFeeAddress,
                        privateKey = "addressData.privateKey".toByteArray(),
                        amount = commission,
                    )
                }

            val estimateCommissionBandwidth =
                withContext(Dispatchers.IO) {
                    tron.transactions.estimateBandwidthTrxTransaction(
                        fromAddress = addressData.address,
                        toAddress = trxFeeAddress,
                        privateKey = "addressData.privateKey".toByteArray(),
                        amount = commission,
                    )
                }
            return CommissionFeeBuilderResult.Success(
                executorAddress = address,
                requiredBandwidth = estimateCommissionBandwidth.bandwidth,
                transaction = signedTxnBytesCommission.signedTxn!!,
            )
        }
    }

sealed class CommissionFeeBuilderResult(
    open val executorAddress: String? = null,
    open val requiredBandwidth: Long? = null,
    open val transaction: ByteString? = null,
    open val errorMessage: String? = null,
) {
    data class Success(
        override val executorAddress: String,
        override val requiredBandwidth: Long,
        override val transaction: ByteString,
    ) : CommissionFeeBuilderResult(executorAddress, requiredBandwidth, transaction)

    data class Error(
        override val errorMessage: String,
    ) : CommissionFeeBuilderResult(errorMessage = errorMessage)
}

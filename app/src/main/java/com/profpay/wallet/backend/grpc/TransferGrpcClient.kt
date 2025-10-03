package com.profpay.wallet.backend.grpc

import com.profpay.wallet.data.flow_db.token.SharedPrefsTokenProvider
import com.profpay.wallet.utils.safeGrpcCall
import io.grpc.ManagedChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.transfer.TransferProto
import org.example.protobuf.transfer.TransferProto.TransactionCommissionData
import org.example.protobuf.transfer.TransferProto.TransactionData
import org.example.protobuf.transfer.TransferProto.TransferNetwork
import org.example.protobuf.transfer.TransferServiceGrpc

class TransferGrpcClient(
    private val channel: ManagedChannel,
    val token: SharedPrefsTokenProvider,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val stub: TransferServiceGrpc.TransferServiceBlockingStub = TransferServiceGrpc.newBlockingStub(channel)

    suspend fun sendTronTransactionRequest(
        userId: Long,
        transaction: TransactionData,
        commission: TransactionCommissionData? = null,
        network: TransferNetwork,
        transferToken: TransferProto.TransferToken,
        txId: String? = null,
    ): Result<TransferProto.SendUsdtTransactionResponse> =
        token.safeGrpcCall {
            withContext(dispatcher) {
                val request =
                    TransferProto.SendUsdtTransactionRequest
                        .newBuilder()
                        .setUserId(userId)
                        .setTransactionData(transaction)
                        .setCommissionData(commission)
                        .setNetwork(network)
                        .setToken(transferToken)
                        .setTxId(txId)
                        .build()

                val response = stub.sendTronTransaction(request)
                Result.success(response)
            }
        }

    suspend fun estimateCommission(
        userId: Long,
        address: String,
        bandwidth: Long,
        energy: Long,
    ): Result<TransferProto.EstimateCommissionResponse> =
        token.safeGrpcCall {
            withContext(dispatcher) {
                val request =
                    TransferProto.EstimateCommissionRequest
                        .newBuilder()
                        .setUserId(userId)
                        .setAddress(address)
                        .setBandwidthRequired(bandwidth)
                        .setEnergyRequired(energy)
                        .build()

                val response = stub.estimateCommission(request)
                Result.success(response)
            }
        }

    suspend fun getTransactionStatus(txId: String): Result<TransferProto.TransactionStatusResponse> =
        token.safeGrpcCall {
            withContext(dispatcher) {
                val request =
                    TransferProto.TransactionStatusRequest
                        .newBuilder()
                        .setTxId(txId)
                        .build()

                val response = stub.getTransactionStatus(request)
                Result.success(response)
            }
        }

    fun shutdown() {
        channel.shutdown()
    }
}

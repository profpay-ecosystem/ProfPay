package com.profpay.wallet.backend.grpc

import com.profpay.wallet.data.flow_db.token.SharedPrefsTokenProvider
import com.profpay.wallet.utils.safeGrpcCall
import io.grpc.ManagedChannel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.smart.SmartContractProto
import org.example.protobuf.smart.SmartContractServerGrpc

class SmartContractGrpcClient(
    private val channel: ManagedChannel,
    val token: SharedPrefsTokenProvider,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {
    private val stub: SmartContractServerGrpc.SmartContractServerBlockingStub = SmartContractServerGrpc.newBlockingStub(channel)

    suspend fun getMyContractDeals(userId: Long): Result<SmartContractProto.GetMyContractDealsResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
                val request =
                    SmartContractProto.GetMyContractDealsRequest
                        .newBuilder()
                        .setUserId(userId)
                        .build()
                val response = stub.getMyContractDeals(request)
                Result.success(response)
            }
        }

    suspend fun contractDealStatusChanged(
        request: SmartContractProto.ContractDealUpdateRequest,
    ): Result<SmartContractProto.ContractDealUpdateResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
                val response = stub.contractDealStatusChanged(request)
                Result.success(response)
            }
        }

    suspend fun contractDealStatusExpertChanged(
        request: SmartContractProto.ContractDealUpdateRequest,
    ): Result<SmartContractProto.ContractDealUpdateResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
                val response = stub.contractDealStatusExpertChanged(request)
                Result.success(response)
            }
        }

    suspend fun deploySmartContract(
        request: SmartContractProto.DeploySmartContractRequest,
    ): Result<SmartContractProto.DeploySmartContractResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
                val response = stub.deploySmartContract(request)
                Result.success(response)
            }
        }

    suspend fun getResourceQuote(request: SmartContractProto.ResourceQuoteRequest): Result<SmartContractProto.ResourceQuoteResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
                val response = stub.getResourceQuote(request)
                Result.success(response)
            }
        }

    suspend fun callContract(request: SmartContractProto.CallContractRequest): Result<SmartContractProto.CallContractResponse> =
        token.safeGrpcCall {
            withContext(ioDispatcher) {
                val response = stub.callContract(request)
                Result.success(response)
            }
        }

    fun shutdown() {
        channel.shutdown()
    }
}

package com.example.telegramWallet.backend.grpc

import com.example.telegramWallet.data.flow_db.token.SharedPrefsTokenProvider
import com.example.telegramWallet.utils.safeGrpcCall
import io.grpc.ManagedChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.example.protobuf.smart.SmartContractProto
import org.example.protobuf.smart.SmartContractServerGrpc

class SmartContractGrpcClient(
    private val channel: ManagedChannel,
    val token: SharedPrefsTokenProvider,
) {
    private val stub: SmartContractServerGrpc.SmartContractServerBlockingStub = SmartContractServerGrpc.newBlockingStub(channel)

    suspend fun getMyContractDeals(userId: Long): Result<SmartContractProto.GetMyContractDealsResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
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
            withContext(Dispatchers.IO) {
                val response = stub.contractDealStatusChanged(request)
                Result.success(response)
            }
        }

    suspend fun contractDealStatusExpertChanged(
        request: SmartContractProto.ContractDealUpdateRequest,
    ): Result<SmartContractProto.ContractDealUpdateResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val response = stub.contractDealStatusExpertChanged(request)
                Result.success(response)
            }
        }

    suspend fun deploySmartContract(
        request: SmartContractProto.DeploySmartContractRequest,
    ): Result<SmartContractProto.DeploySmartContractResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val response = stub.deploySmartContract(request)
                Result.success(response)
            }
        }

    suspend fun getResourceQuote(request: SmartContractProto.ResourceQuoteRequest): Result<SmartContractProto.ResourceQuoteResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val response = stub.getResourceQuote(request)
                Result.success(response)
            }
        }

    suspend fun callContract(request: SmartContractProto.CallContractRequest): Result<SmartContractProto.CallContractResponse> =
        token.safeGrpcCall {
            withContext(Dispatchers.IO) {
                val response = stub.callContract(request)
                Result.success(response)
            }
        }

    fun shutdown() {
        channel.shutdown()
    }
}

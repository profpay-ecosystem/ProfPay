package com.example.telegramWallet.data.flow_db.repo

import com.example.telegramWallet.AppConstants
import com.example.telegramWallet.backend.grpc.GrpcClientFactory
import com.example.telegramWallet.backend.grpc.SmartContractGrpcClient
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.example.protobuf.smart.SmartContractProto
import org.example.protobuf.smart.SmartContractProto.FeePolicy
import javax.inject.Inject

data class SmartContractModalData(
    val isActive: Boolean,
    val text: String,
    val buttonType: SmartContractButtonType? = null,
    val deal: SmartContractProto.ContractDealListResponse? = null
)

data class EstimateResourcePriceResult(
    val commission: Long,
    val feePolicy: FeePolicy
)

enum class SmartContractButtonType {
    ACCEPT, REJECT
}

interface SmartContractRepo {
    val deals: Flow<List<SmartContractProto.ContractDealListResponse>>
    val smartContractModalData: Flow<SmartContractModalData>
    suspend fun setSmartContractModalData(isActive: Boolean, text: String)
    suspend fun getMyContractDeals()
    suspend fun contractDealStatusChanged(request: SmartContractProto.ContractDealUpdateRequest)
    suspend fun contractDealStatusExpertChanged(request: SmartContractProto.ContractDealUpdateRequest)
    suspend fun getUserId(): Long
    suspend fun deploySmartContract(request: SmartContractProto.DeploySmartContractRequest)
    suspend fun getResourceQuote(request: SmartContractProto.ResourceQuoteRequest)
    val estimateResourcePrice: Flow<EstimateResourcePriceResult>
    suspend fun initSmartContractModalAndStart(isActive: Boolean, buttonType: SmartContractButtonType?, deal: SmartContractProto.ContractDealListResponse?)
    suspend fun callContract(request: SmartContractProto.CallContractRequest)
}

class SmartContractRepoImpl @Inject constructor(
    val profileRepo: ProfileRepo,
    grpcClientFactory: GrpcClientFactory
) : SmartContractRepo {
    private val _deals = MutableSharedFlow<List<SmartContractProto.ContractDealListResponse>>(replay = 1)
    override val deals: Flow<List<SmartContractProto.ContractDealListResponse>> = _deals.asSharedFlow()

    private val _smartContractModalData = MutableSharedFlow<SmartContractModalData>(replay = 1)
    override val smartContractModalData: Flow<SmartContractModalData> = _smartContractModalData.asSharedFlow()

    private val _estimateResourcePrice = MutableSharedFlow<EstimateResourcePriceResult>(replay = 1)
    override val estimateResourcePrice: Flow<EstimateResourcePriceResult> = _estimateResourcePrice.asSharedFlow()
    
    override suspend fun setSmartContractModalData(isActive: Boolean, text: String) {
        _smartContractModalData.emit(SmartContractModalData(isActive, text))
    }

    override suspend fun initSmartContractModalAndStart(isActive: Boolean, buttonType: SmartContractButtonType?, deal: SmartContractProto.ContractDealListResponse?) {
        _smartContractModalData.emit(SmartContractModalData(isActive, "", buttonType, deal))
    }

    private val smartContractGrpcClient: SmartContractGrpcClient = grpcClientFactory.getGrpcClient(
        SmartContractGrpcClient::class.java,
        AppConstants.Network.GRPC_ENDPOINT,
        AppConstants.Network.GRPC_PORT
    )

    override suspend fun getMyContractDeals() {
        try {
            val result = smartContractGrpcClient.getMyContractDeals(getUserId())
            result.fold(
                onSuccess = {
                    _deals.emit(it.dealsList)
                },
                onFailure = {
                    Sentry.captureException(it)
                    // TODO: Создать кастом
                    throw RuntimeException(it)
                }
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch smart contracts", e)
        }
    }

    override suspend fun contractDealStatusChanged(request: SmartContractProto.ContractDealUpdateRequest) {
        try {
            val result = smartContractGrpcClient.contractDealStatusChanged(request)
            result.fold(
                onSuccess = {
                    if (it.updateStatus) {
                        getMyContractDeals()
                    }
                },
                onFailure = {
                    Sentry.captureException(it)
                    throw RuntimeException(it)
                }
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch smart contracts", e)
        }
    }

    override suspend fun contractDealStatusExpertChanged(request: SmartContractProto.ContractDealUpdateRequest) {
        try {
            val result = smartContractGrpcClient.contractDealStatusExpertChanged(request)
            result.fold(
                onSuccess = {
                    if (it.updateStatus) {
                        getMyContractDeals()
                    }
                },
                onFailure = {
                    // TODO: Создать кастом
                    throw RuntimeException(it)
                }
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch smart contracts", e)
        }
    }

    override suspend fun getUserId(): Long {
        return profileRepo.getProfileUserId()
    }

    override suspend fun deploySmartContract(request: SmartContractProto.DeploySmartContractRequest) {
        try {
            val result = smartContractGrpcClient.deploySmartContract(request)
            result.fold(
                onSuccess = { },
                onFailure = {
                    // TODO: Создать кастом
                    throw RuntimeException(it)
                }
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch smart contracts", e)
        }
    }

    override suspend fun getResourceQuote(request: SmartContractProto.ResourceQuoteRequest) {
        try {
            val result = smartContractGrpcClient.getResourceQuote(request)
            result.fold(
                onSuccess = {
                    _estimateResourcePrice.emit(
                        EstimateResourcePriceResult(
                            commission = it.commission,
                            feePolicy = it.feePolicy
                        )
                    )
                },
                onFailure = {
                    // TODO: Создать кастом
                    throw RuntimeException(it)
                }
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch smart contracts", e)
        }
    }

    override suspend fun callContract(request: SmartContractProto.CallContractRequest) {
        try {
            val result = smartContractGrpcClient.callContract(request)
            result.fold(
                onSuccess = { },
                onFailure = {
                    Sentry.captureException(it)
                    throw RuntimeException(it)
                }
            )
        } catch (e: Exception) {
            throw RuntimeException("Failed to fetch smart contracts", e)
        }
    }
}
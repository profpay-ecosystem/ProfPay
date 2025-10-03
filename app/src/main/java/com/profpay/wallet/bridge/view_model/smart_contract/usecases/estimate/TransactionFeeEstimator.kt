package com.profpay.wallet.bridge.view_model.smart_contract.usecases.estimate

import com.profpay.wallet.data.database.entities.wallet.AddressEntity
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.flow_db.module.IoDispatcher
import com.profpay.wallet.data.utils.toBigInteger
import com.profpay.wallet.tron.Tron
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.example.protobuf.smart.SmartContractProto
import org.tron.trident.abi.TypeReference
import org.tron.trident.abi.datatypes.Address
import org.tron.trident.abi.datatypes.Bool
import org.tron.trident.abi.datatypes.DynamicArray
import org.tron.trident.abi.datatypes.Function
import org.tron.trident.abi.datatypes.Type
import org.tron.trident.abi.datatypes.Utf8String
import org.tron.trident.abi.datatypes.generated.Uint256
import java.math.BigInteger
import javax.inject.Inject

class TransactionFeeEstimator
    @Inject
    constructor(
        private val addressRepo: AddressRepo,
        private val tron: Tron,
        @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) {
        suspend fun createDeal(deal: SmartContractProto.ContractDealListResponse): TransactionEstimatorResult {
            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(deal.buyer.address)
                }

            if (addressData == null) {
                return TransactionEstimatorResult.Error(
                    EstimateType.DEFAULT,
                    "Address data is null",
                )
            }

            val admins =
                listOf(
                    Address(deal.adminsList[0].address),
                    Address(deal.adminsList[1].address),
                    Address(deal.adminsList[2].address),
                )

            val adminStatuses =
                listOf(
                    Utf8String(deal.adminsList[0].status.nameCode),
                    Utf8String(deal.adminsList[1].status.nameCode),
                    Utf8String(deal.adminsList[2].status.nameCode),
                )

            val params =
                mutableListOf<Type<*>>().apply {
                    add(Address(deal.seller.address)) // Seller Address
                    add(Address(deal.buyer.address)) // Buyer Address
                    add(Uint256(deal.amount.toBigInteger())) // Amount
                    add(DynamicArray(Address::class.java, admins)) // Admins Array
                    add(DynamicArray(Utf8String::class.java, adminStatuses)) // Admin Statuses Array
                }

            val createDealCostResult =
                estimateTransactionCost(
                    function =
                        Function(
                            "createDeal",
                            params,
                            listOf(object : TypeReference<Uint256?>() {}),
                        ),
                    contractAddress = deal.smartContractAddress,
                    addressData = addressData,
                )

            return TransactionEstimatorResult.Success(
                executorAddress = addressData.address,
                requiredEnergyInTrx = createDealCostResult.requiredEnergyInTrx,
                requiredEnergy = createDealCostResult.requiredEnergy,
                requiredBandwidthInTrx = createDealCostResult.requiredBandwidthInTrx,
                requiredBandwidth = createDealCostResult.requiredBandwidth,
                estimateType = EstimateType.DEFAULT,
            )
        }

        suspend fun approveAndDepositDeal(deal: SmartContractProto.ContractDealListResponse): TransactionEstimatorResult {
            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(deal.buyer.address)
                } ?: return TransactionEstimatorResult.Error(EstimateType.DEFAULT, "Address data is null")

            val balance = getBalance(addressData.address)
            val isAllowanceUnlimited =
                tron.accounts.isAllowanceUnlimited(
                    spender = deal.smartContractAddress,
                    ownerAddress = addressData.address,
                    privateKey = "addressData.privateKey",
                )

            val approveCostResult =
                estimateTransactionCost(
                    function =
                        Function(
                            "approve",
                            listOf(
                                Address(deal.buyer.address),
                                Uint256(BigInteger.valueOf(Long.MAX_VALUE).multiply(BigInteger.valueOf(10L).pow(6))),
                            ),
                            emptyList<TypeReference<Bool>>(),
                        ),
                    contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
                    addressData = addressData,
                )

            if (((balance < approveCostResult.requiredEnergyInTrx) && !isAllowanceUnlimited) || !isAllowanceUnlimited) {
                return TransactionEstimatorResult.Success(
                    executorAddress = addressData.address,
                    requiredEnergyInTrx = approveCostResult.requiredEnergyInTrx,
                    requiredEnergy = approveCostResult.requiredEnergy,
                    requiredBandwidthInTrx = approveCostResult.requiredBandwidthInTrx,
                    requiredBandwidth = approveCostResult.requiredBandwidth,
                    estimateType = EstimateType.APPROVE,
                )
            }

            val depositCostResult =
                estimateTransactionCost(
                    function =
                        Function(
                            "depositDeal",
                            listOf(Uint256(deal.dealBlockchainId)),
                            emptyList<TypeReference<*>>(),
                        ),
                    contractAddress = deal.smartContractAddress,
                    addressData = addressData,
                )

            return TransactionEstimatorResult.Success(
                executorAddress = addressData.address,
                requiredEnergyInTrx = depositCostResult.requiredEnergyInTrx,
                requiredEnergy = depositCostResult.requiredEnergy,
                requiredBandwidthInTrx = depositCostResult.requiredBandwidthInTrx,
                requiredBandwidth = depositCostResult.requiredBandwidth,
                EstimateType.DEFAULT,
            )
        }

        suspend fun approveAndPaySellerExpertFee(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
        ): TransactionEstimatorResult {
            val address =
                if (userId == deal.buyer.userId) {
                    deal.buyer.address
                } else {
                    deal.seller.address
                }

            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(address)
                } ?: return TransactionEstimatorResult.Error(EstimateType.DEFAULT, "Address data is null")

            val balance = getBalance(addressData.address)
            val allowanceValue =
                tron.accounts.allowance(
                    spender = deal.smartContractAddress,
                    ownerAddress = addressData.address,
                    privateKey = "addressData.privateKey",
                )

            val approveAmount = deal.dealData.totalExpertCommissions / 2
            val approveCompare = allowanceValue!!.compareTo(approveAmount.toBigInteger()) == -1

            val approveCostResult =
                estimateTransactionCost(
                    function =
                        Function(
                            "approve",
                            listOf(
                                Address(address),
                                Uint256(approveAmount),
                            ),
                            emptyList<TypeReference<Bool>>(),
                        ),
                    contractAddress = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t",
                    addressData = addressData,
                )

            if (((balance < approveCostResult.requiredEnergyInTrx) && approveCompare) || approveCompare) {
                return TransactionEstimatorResult.Success(
                    executorAddress = addressData.address,
                    requiredEnergyInTrx = approveCostResult.requiredEnergyInTrx,
                    requiredEnergy = approveCostResult.requiredEnergy,
                    requiredBandwidthInTrx = approveCostResult.requiredBandwidthInTrx,
                    requiredBandwidth = approveCostResult.requiredBandwidth,
                    estimateType = EstimateType.APPROVE,
                )
            }

            val paySellerExpertCostResult =
                estimateTransactionCost(
                    function =
                        Function(
                            "paySellerExpertFee",
                            listOf<Type<*>>(
                                Uint256(BigInteger.valueOf(deal.dealBlockchainId)),
                            ),
                            emptyList<TypeReference<*>>(),
                        ),
                    contractAddress = deal.smartContractAddress,
                    addressData = addressData,
                )

            return TransactionEstimatorResult.Success(
                executorAddress = addressData.address,
                requiredEnergyInTrx = paySellerExpertCostResult.requiredEnergyInTrx,
                requiredEnergy = paySellerExpertCostResult.requiredEnergy,
                requiredBandwidthInTrx = paySellerExpertCostResult.requiredBandwidthInTrx,
                requiredBandwidth = paySellerExpertCostResult.requiredBandwidth,
                EstimateType.DEFAULT,
            )
        }

        suspend fun confirmDeal(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
        ): TransactionEstimatorResult {
            val address =
                if (userId == deal.buyer.userId) {
                    deal.buyer.address
                } else {
                    deal.seller.address
                }

            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(address)
                }

            if (addressData == null) {
                return TransactionEstimatorResult.Error(
                    EstimateType.DEFAULT,
                    "Address data is null",
                )
            }

            val voteDealCostResult =
                estimateTransactionCost(
                    function =
                        Function(
                            "voteDeal",
                            listOf(Uint256(deal.dealBlockchainId)),
                            emptyList<TypeReference<*>>(),
                        ),
                    contractAddress = deal.smartContractAddress,
                    addressData = addressData,
                )

            return TransactionEstimatorResult.Success(
                executorAddress = addressData.address,
                requiredEnergyInTrx = voteDealCostResult.requiredEnergyInTrx,
                requiredEnergy = voteDealCostResult.requiredEnergy,
                requiredBandwidthInTrx = voteDealCostResult.requiredBandwidthInTrx,
                requiredBandwidth = voteDealCostResult.requiredBandwidth,
                estimateType = EstimateType.DEFAULT,
            )
        }

        suspend fun rejectCancelDeal(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
        ): TransactionEstimatorResult {
            val address =
                if (userId == deal.buyer.userId) {
                    deal.buyer.address
                } else {
                    deal.seller.address
                }

            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(address)
                }

            if (addressData == null) {
                return TransactionEstimatorResult.Error(
                    EstimateType.DEFAULT,
                    "Address data is null",
                )
            }

            val cancelDealCostResult =
                estimateTransactionCost(
                    function =
                        Function(
                            "cancelDeal",
                            listOf(Uint256(deal.dealBlockchainId)),
                            emptyList<TypeReference<*>>(),
                        ),
                    contractAddress = deal.smartContractAddress,
                    addressData = addressData,
                )

            return TransactionEstimatorResult.Success(
                executorAddress = addressData.address,
                requiredEnergyInTrx = cancelDealCostResult.requiredEnergyInTrx,
                requiredEnergy = cancelDealCostResult.requiredEnergy,
                requiredBandwidthInTrx = cancelDealCostResult.requiredBandwidthInTrx,
                requiredBandwidth = cancelDealCostResult.requiredBandwidth,
                estimateType = EstimateType.DEFAULT,
            )
        }

        suspend fun executeDisputed(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
        ): TransactionEstimatorResult {
            val address =
                if (userId == deal.buyer.userId) {
                    deal.buyer.address
                } else {
                    deal.seller.address
                }

            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(address)
                }

            if (addressData == null) {
                return TransactionEstimatorResult.Error(
                    EstimateType.DEFAULT,
                    "Address data is null",
                )
            }

            val executeDisputedCostResult =
                estimateTransactionCost(
                    function =
                        Function(
                            "executeDisputed",
                            listOf(Uint256(deal.dealBlockchainId)),
                            emptyList<TypeReference<*>>(),
                        ),
                    contractAddress = deal.smartContractAddress,
                    addressData = addressData,
                )

            return TransactionEstimatorResult.Success(
                executorAddress = addressData.address,
                requiredEnergyInTrx = executeDisputedCostResult.requiredEnergyInTrx,
                requiredEnergy = executeDisputedCostResult.requiredEnergy,
                requiredBandwidthInTrx = executeDisputedCostResult.requiredBandwidthInTrx,
                requiredBandwidth = executeDisputedCostResult.requiredBandwidth,
                estimateType = EstimateType.DEFAULT,
            )
        }

        suspend fun assignDecisionAdminAndSetAmounts(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
            sellerValue: Long,
            buyerValue: Long,
        ): TransactionEstimatorResult {
            val admin =
                deal.adminsList.find { it.userId == userId } ?: return TransactionEstimatorResult.Error(
                    EstimateType.DEFAULT,
                    "None admin",
                )

            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(admin.address)
                }

            if (addressData == null) {
                return TransactionEstimatorResult.Error(
                    EstimateType.DEFAULT,
                    "Address data is null",
                )
            }

            val assignDecisionAdminCostResult =
                estimateTransactionCost(
                    function =
                        Function(
                            "assignDecisionAdminAndSetAmounts",
                            listOf(
                                Uint256(deal.dealBlockchainId),
                                Uint256(BigInteger.valueOf(sellerValue)),
                                Uint256(BigInteger.valueOf(buyerValue)),
                            ),
                            emptyList<TypeReference<*>>(),
                        ),
                    contractAddress = deal.smartContractAddress,
                    addressData = addressData,
                )

            return TransactionEstimatorResult.Success(
                executorAddress = addressData.address,
                requiredEnergyInTrx = assignDecisionAdminCostResult.requiredEnergyInTrx,
                requiredEnergy = assignDecisionAdminCostResult.requiredEnergy,
                requiredBandwidthInTrx = assignDecisionAdminCostResult.requiredBandwidthInTrx,
                requiredBandwidth = assignDecisionAdminCostResult.requiredBandwidth,
                estimateType = EstimateType.DEFAULT,
            )
        }

        suspend fun voteOnDisputeResolution(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
        ): TransactionEstimatorResult {
            val address =
                deal.adminsList.firstOrNull { it.userId == userId }?.address
                    ?: if (userId == deal.buyer.userId) {
                        deal.buyer.address
                    } else if (userId == deal.seller.userId) {
                        deal.seller.address
                    } else {
                        return TransactionEstimatorResult.Error(EstimateType.DEFAULT, "None address")
                    }

            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(address)
                }

            if (addressData == null) {
                return TransactionEstimatorResult.Error(
                    EstimateType.DEFAULT,
                    "Address data is null",
                )
            }

            val voteOnDisputeResolutionCostResult =
                estimateTransactionCost(
                    function =
                        Function(
                            "voteOnDisputeResolution",
                            listOf(Uint256(deal.dealBlockchainId)),
                            emptyList<TypeReference<*>>(),
                        ),
                    contractAddress = deal.smartContractAddress,
                    addressData = addressData,
                )

            return TransactionEstimatorResult.Success(
                executorAddress = addressData.address,
                requiredEnergyInTrx = voteOnDisputeResolutionCostResult.requiredEnergyInTrx,
                requiredEnergy = voteOnDisputeResolutionCostResult.requiredEnergy,
                requiredBandwidthInTrx = voteOnDisputeResolutionCostResult.requiredBandwidthInTrx,
                requiredBandwidth = voteOnDisputeResolutionCostResult.requiredBandwidth,
                estimateType = EstimateType.DEFAULT,
            )
        }

        suspend fun declineDisputeResolution(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
        ): TransactionEstimatorResult {
            val address =
                deal.adminsList.firstOrNull { it.userId == userId }?.address
                    ?: if (userId == deal.buyer.userId) {
                        deal.buyer.address
                    } else if (userId == deal.seller.userId) {
                        deal.seller.address
                    } else {
                        return TransactionEstimatorResult.Error(EstimateType.DEFAULT, "None address")
                    }

            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(address)
                }

            if (addressData == null) {
                return TransactionEstimatorResult.Error(
                    EstimateType.DEFAULT,
                    "Address data is null",
                )
            }

            val declineDisputeResolutionCostResult =
                estimateTransactionCost(
                    function =
                        Function(
                            "declineDisputeResolution",
                            listOf(Uint256(deal.dealBlockchainId)),
                            emptyList<TypeReference<*>>(),
                        ),
                    contractAddress = deal.smartContractAddress,
                    addressData = addressData,
                )

            return TransactionEstimatorResult.Success(
                executorAddress = addressData.address,
                requiredEnergyInTrx = declineDisputeResolutionCostResult.requiredEnergyInTrx,
                requiredEnergy = declineDisputeResolutionCostResult.requiredEnergy,
                requiredBandwidthInTrx = declineDisputeResolutionCostResult.requiredBandwidthInTrx,
                requiredBandwidth = declineDisputeResolutionCostResult.requiredBandwidth,
                estimateType = EstimateType.DEFAULT,
            )
        }

        private fun estimateTransactionCost(
            function: Function,
            contractAddress: String,
            addressData: AddressEntity,
        ): EstimateResult {
            val estimateEnergy =
                tron.transactions.estimateEnergy(
                    function = function,
                    contractAddress = contractAddress,
                    address = addressData.address,
                    privateKey = "addressData.privateKey".toByteArray(),
                )
            val estimateBandwidth =
                tron.transactions.estimateBandwidth(
                    function = function,
                    contractAddress = contractAddress,
                    address = addressData.address,
                    privateKey = "addressData.privateKey".toByteArray(),
                )
            return EstimateResult(
                requiredEnergyInTrx = estimateEnergy.energyInTrx,
                requiredEnergy = estimateEnergy.energy,
                requiredBandwidthInTrx = estimateBandwidth.bandwidthInTrx,
                requiredBandwidth = estimateBandwidth.bandwidth,
            )
        }

        private fun getBalance(address: String): BigInteger = tron.addressUtilities.getTrxBalance(address)

        data class EstimateResult(
            val requiredEnergyInTrx: BigInteger,
            val requiredEnergy: Long,
            val requiredBandwidthInTrx: Double,
            val requiredBandwidth: Long,
        )
    }

sealed class TransactionEstimatorResult(
    open val estimateType: EstimateType,
    open val executorAddress: String? = null,
    open val requiredEnergyInTrx: BigInteger? = null,
    open val requiredEnergy: Long? = null,
    open val requiredBandwidthInTrx: Double? = null,
    open val requiredBandwidth: Long? = null,
    open val errorMessage: String? = null,
) {
    data class Success(
        override val executorAddress: String,
        override val requiredEnergyInTrx: BigInteger,
        override val requiredEnergy: Long,
        override val requiredBandwidthInTrx: Double,
        override val requiredBandwidth: Long,
        override val estimateType: EstimateType,
    ) : TransactionEstimatorResult(
            estimateType,
            executorAddress,
            requiredEnergyInTrx,
            requiredEnergy,
            requiredBandwidthInTrx,
            requiredBandwidth,
        )

    data class Error(
        override val estimateType: EstimateType,
        override val errorMessage: String,
    ) : TransactionEstimatorResult(estimateType, errorMessage = errorMessage)
}

enum class EstimateType {
    DEFAULT,
    APPROVE,
}

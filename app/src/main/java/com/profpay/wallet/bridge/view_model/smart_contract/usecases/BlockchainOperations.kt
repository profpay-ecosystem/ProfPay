package com.profpay.wallet.bridge.view_model.smart_contract.usecases

import com.google.protobuf.ByteString
import com.profpay.wallet.data.database.entities.wallet.AddressEntity
import com.profpay.wallet.data.database.repositories.wallet.AddressRepo
import com.profpay.wallet.data.di.module.IoDispatcher
import com.profpay.wallet.data.utils.toBigInteger
import com.profpay.wallet.tron.Tron
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.example.protobuf.smart.SmartContractProto
import org.tron.trident.abi.datatypes.Address
import org.tron.trident.abi.datatypes.DynamicArray
import org.tron.trident.abi.datatypes.Function
import org.tron.trident.abi.datatypes.Type
import org.tron.trident.abi.datatypes.Utf8String
import org.tron.trident.abi.datatypes.generated.Uint256
import java.math.BigInteger
import javax.inject.Inject

class BlockchainOperations
    @Inject
    constructor(
        private val addressRepo: AddressRepo,
        private val tron: Tron,
        @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    ) {
        suspend fun createDeal(deal: SmartContractProto.ContractDealListResponse): DealActionResult {
            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(deal.buyer.address)
                }

            if (addressData == null) throw Error("Not address")

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

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.createDeal(
                        ownerAddress = addressData.address,
                        contractAddress = deal.smartContractAddress,
                        privateKey = "addressData.privateKey",
                        params = params,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun approveAndDepositDeal(deal: SmartContractProto.ContractDealListResponse): DealActionResult {
            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(deal.buyer.address)
                } ?: return DealActionResult.Error("Address data is null")

            val isAllowanceUnlimited =
                tron.accounts.isAllowanceUnlimited(
                    spender = deal.smartContractAddress,
                    ownerAddress = addressData.address,
                    privateKey = "addressData.privateKey",
                )

            if (!isAllowanceUnlimited) {
                val signedTransaction: ByteString =
                    withContext(ioDispatcher) {
                        tron.smartContracts.multiSigWrite.approve(
                            ownerAddress = deal.buyer.address,
                            privateKey = "addressData.privateKey",
                            contractAddress = deal.smartContractAddress,
                        )
                    }
                return DealActionResult.Success(signedTransaction)
            }

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.depositDeal(
                        id = deal.dealBlockchainId,
                        ownerAddress = deal.buyer.address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.smartContractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun approveAndPaySellerExpertFee(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
        ): DealActionResult {
            val address =
                if (userId == deal.buyer.userId) {
                    deal.buyer.address
                } else {
                    deal.seller.address
                }

            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(address)
                } ?: return DealActionResult.Error("Address data is null")

            val allowanceValue =
                tron.accounts.allowance(
                    spender = deal.smartContractAddress,
                    ownerAddress = addressData.address,
                    privateKey = "addressData.privateKey",
                )

            val approveAmount = deal.dealData.totalExpertCommissions / 2
            val approveCompare = allowanceValue!!.compareTo(approveAmount.toBigInteger()) == -1

            if (approveCompare) {
                val signedTransaction: ByteString =
                    withContext(ioDispatcher) {
                        tron.smartContracts.multiSigWrite.approve(
                            ownerAddress = address,
                            privateKey = "addressData.privateKey",
                            contractAddress = deal.smartContractAddress,
                        )
                    }
                return DealActionResult.Success(signedTransaction)
            }

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.paySellerExpertFee(
                        id = deal.dealBlockchainId,
                        ownerAddress = address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.smartContractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun confirmDeal(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
        ): DealActionResult {
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

            if (addressData == null) return DealActionResult.Error("Address data is null")

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.voteDeal(
                        id = deal.dealBlockchainId,
                        ownerAddress = address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.smartContractAddress!!,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun rejectCancelDeal(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
        ): DealActionResult {
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

            if (addressData == null) return DealActionResult.Error("Address data is null")

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.cancelDeal(
                        id = deal.dealBlockchainId,
                        ownerAddress = addressData.address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.smartContractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun executeDisputed(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
        ): DealActionResult {
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

            if (addressData == null) return DealActionResult.Error("Address data is null")

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.executeDisputed(
                        id = deal.dealBlockchainId,
                        ownerAddress = address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.smartContractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun assignDecisionAdminAndSetAmounts(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
            sellerValue: BigInteger,
            buyerValue: BigInteger,
        ): DealActionResult {
            val admin = deal.adminsList.find { it.userId == userId } ?: return DealActionResult.Error("None admin")

            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(admin.address)
                }

            if (addressData == null) return DealActionResult.Error("Address data is null")

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.assignDecisionAdminAndSetAmounts(
                        id = deal.dealBlockchainId,
                        ownerAddress = admin.address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.smartContractAddress,
                        sellerValue = sellerValue,
                        buyerValue = buyerValue,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun voteOnDisputeResolution(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
        ): DealActionResult {
            val address =
                deal.adminsList.firstOrNull { it.userId == userId }?.address
                    ?: if (userId == deal.buyer.userId) {
                        deal.buyer.address
                    } else if (userId == deal.seller.userId) {
                        deal.seller.address
                    } else {
                        return DealActionResult.Error("None address")
                    }

            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(address)
                }

            if (addressData == null) return DealActionResult.Error("Address data is null")

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.voteOnDisputeResolution(
                        id = deal.dealBlockchainId,
                        ownerAddress = address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.smartContractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        suspend fun declineDisputeResolution(
            deal: SmartContractProto.ContractDealListResponse,
            userId: Long,
        ): DealActionResult {
            val address =
                deal.adminsList.firstOrNull { it.userId == userId }?.address
                    ?: if (userId == deal.buyer.userId) {
                        deal.buyer.address
                    } else if (userId == deal.seller.userId) {
                        deal.seller.address
                    } else {
                        return DealActionResult.Error("None address")
                    }

            val addressData =
                withContext(ioDispatcher) {
                    addressRepo.getAddressEntityByAddress(address)
                }

            if (addressData == null) return DealActionResult.Error("Address data is null")

            val signedTransaction: ByteString =
                withContext(ioDispatcher) {
                    tron.smartContracts.multiSigWrite.declineDisputeResolution(
                        id = deal.dealBlockchainId,
                        ownerAddress = address,
                        privateKey = "addressData.privateKey",
                        contractAddress = deal.smartContractAddress,
                    )
                }
            return DealActionResult.Success(signedTransaction)
        }

        private fun estimateTransactionCost(
            function: Function,
            contractAddress: String,
            addressData: AddressEntity,
        ): BigInteger {
            val estimate =
                tron.transactions.estimateEnergy(
                    function = function,
                    contractAddress = contractAddress,
                    address = addressData.address,
                    privateKey = "addressData.privateKey".toByteArray(),
                )
            return estimate.energyInTrx
        }

        private fun getBalance(address: String): BigInteger = tron.addressUtilities.getTrxBalance(address)
    }

sealed class DealActionResult(
    open val transaction: ByteString? = null,
    open val amountRequired: BigInteger? = null,
    open val reason: String? = null,
) {
    data class Success(
        override val transaction: ByteString,
    ) : DealActionResult(transaction = transaction)

    data class InsufficientFunds(
        val type: Type,
        override val amountRequired: BigInteger,
    ) : DealActionResult(amountRequired = amountRequired) {
        enum class Type { BALANCE, APPROVAL }
    }

    data class Error(
        override val reason: String,
    ) : DealActionResult(reason = reason)
}

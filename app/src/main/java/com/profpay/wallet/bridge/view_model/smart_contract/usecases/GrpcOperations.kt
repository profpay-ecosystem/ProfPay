package com.profpay.wallet.bridge.view_model.smart_contract.usecases

import com.profpay.wallet.data.database.repositories.ProfileRepo
import com.profpay.wallet.data.repository.flow.SmartContractRepo
import org.example.protobuf.smart.SmartContractProto
import org.example.protobuf.smart.SmartContractProto.CallContractTransactionData
import org.example.protobuf.smart.SmartContractProto.DealContractChangeStatuses
import javax.inject.Inject

class GrpcOperations
    @Inject
    constructor(
        private val smartContractRepo: SmartContractRepo,
        private val profileRepo: ProfileRepo,
    ) {
        suspend fun contractDealStatusChanged(
            deal: SmartContractProto.ContractDealListResponse,
            contractAddress: String,
            status: DealContractChangeStatuses,
        ) {
            smartContractRepo.contractDealStatusChanged(
                SmartContractProto.ContractDealUpdateRequest
                    .newBuilder()
                    .setAppId(profileRepo.getProfileAppId())
                    .setChangeStatus(status)
                    .setDeal(
                        SmartContractProto.ContractDealListResponse
                            .newBuilder()
                            .setDealBlockchainId(deal.dealBlockchainId)
                            .setDealBaseId(deal.dealBaseId)
                            .setSmartContractId(deal.smartContractId)
                            .setSmartContractAddress(contractAddress)
                            .setAmount(deal.amount)
                            .setBuyer(deal.buyer)
                            .setSeller(deal.seller)
                            .addAllAdmins(deal.adminsList)
                            .setDealData(deal.dealData),
                    ).build(),
            )
        }

        suspend fun contractDealStatusExpertChanged(
            deal: SmartContractProto.ContractDealListResponse,
            contractAddress: String,
            status: DealContractChangeStatuses,
        ) {
            smartContractRepo.contractDealStatusExpertChanged(
                SmartContractProto.ContractDealUpdateRequest
                    .newBuilder()
                    .setAppId(profileRepo.getProfileAppId())
                    .setChangeStatus(status)
                    .setDeal(
                        SmartContractProto.ContractDealListResponse
                            .newBuilder()
                            .setDealBlockchainId(deal.dealBlockchainId)
                            .setDealBaseId(deal.dealBaseId)
                            .setSmartContractId(deal.smartContractId)
                            .setSmartContractAddress(contractAddress)
                            .setAmount(deal.amount)
                            .setBuyer(deal.buyer)
                            .setSeller(deal.seller)
                            .addAllAdmins(deal.adminsList)
                            .setDealData(deal.dealData),
                    ).build(),
            )
        }

        suspend fun callContract(
            userId: Long,
            status: DealContractChangeStatuses,
            deal: SmartContractProto.ContractDealListResponse,
            contractTransactionData: CallContractTransactionData,
            commissionTransactionData: CallContractTransactionData,
        ) {
            smartContractRepo.callContract(
                SmartContractProto.CallContractRequest
                    .newBuilder()
                    .setUserId(userId)
                    .setContractAddress(deal.smartContractAddress)
                    .setDealBlockchainId(deal.dealBlockchainId)
                    .setDealBaseId(deal.dealBaseId)
                    .setChangeStatus(status)
                    .setContract(contractTransactionData)
                    .setCommission(commissionTransactionData)
                    .build(),
            )
        }
    }

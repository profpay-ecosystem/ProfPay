package com.example.telegramWallet.bridge.view_model.smart_contract.usecases

import android.util.Log
import com.example.telegramWallet.bridge.view_model.smart_contract.CompleteReturnData
import com.example.telegramWallet.bridge.view_model.smart_contract.CompleteStatusesEnum
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.estimate.ProcessContractEstimatorUseCase
import com.example.telegramWallet.data.database.repositories.ProfileRepo
import com.example.telegramWallet.data.flow_db.repo.SmartContractRepo
import com.example.telegramWallet.data.utils.toByteString
import com.example.telegramWallet.data.utils.toSunAmount
import org.example.protobuf.smart.SmartContractProto
import org.example.protobuf.smart.SmartContractProto.CallContractTransactionData
import java.math.BigDecimal
import java.math.BigInteger
import javax.inject.Inject

class ProcessSmartContractUseCase
    @Inject
    constructor(
        private val profileRepo: ProfileRepo,
        private val smartContractRepo: SmartContractRepo,
        private val blockchainOperations: BlockchainOperations,
        private val grpcOperations: GrpcOperations,
        private val processContractEstimatorUseCase: ProcessContractEstimatorUseCase,
        private val commissionFeeBuilder: CommissionFeeBuilder,
    ) {
        suspend fun processCompleteSmartContract(
            commission: BigDecimal,
            deal: SmartContractProto.ContractDealListResponse,
        ): CompleteReturnData {
            val userId = profileRepo.getProfileUserId()
            val estimatorResult = processContractEstimatorUseCase.processCompleteSmartContract(deal)
            val commissionFeeBuilderResult =
                commissionFeeBuilder.build(
                    commission = commission.toSunAmount(),
                    userId = userId,
                    deal = deal,
                )

            val result: DealActionResult? =
                when {
                    isBuyerRequestInitialized(deal, userId) -> {
                        smartContractRepo.setSmartContractModalData(true, "Создание сделки в смарт-контракте..")
                        blockchainOperations.createDeal(deal).also { dealResult ->
                            if (dealResult is DealActionResult.Success) {
                                grpcOperations.callContract(
                                    userId = userId,
                                    status = SmartContractProto.DealContractChangeStatuses.BUYER_CREATED,
                                    deal = deal,
                                    contractTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setEstimateEnergy(estimatorResult!!.requiredEnergy!!)
                                            .setBandwidthRequired(estimatorResult.requiredBandwidth!!)
                                            .setTxnBytes(dealResult.transaction)
                                            .build(),
                                    commissionTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAmount(commission.toSunAmount().toByteString())
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setBandwidthRequired(commissionFeeBuilderResult.requiredBandwidth!!)
                                            .setTxnBytes(commissionFeeBuilderResult.transaction!!)
                                            .build(),
                                )
                            }
                        }
                    }
                    isBuyerNotDeposited(deal, userId) -> {
                        smartContractRepo.setSmartContractModalData(true, "Выдача approve адресу и перевод средств.")
                        blockchainOperations.approveAndDepositDeal(deal).also { dealResult ->
                            if (dealResult is DealActionResult.Success) {
                                grpcOperations.callContract(
                                    userId = userId,
                                    status = SmartContractProto.DealContractChangeStatuses.BUYER_DEPOSITED,
                                    deal = deal,
                                    contractTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setEstimateEnergy(estimatorResult!!.requiredEnergy!!)
                                            .setBandwidthRequired(estimatorResult.requiredBandwidth!!)
                                            .setTxnBytes(dealResult.transaction)
                                            .build(),
                                    commissionTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAmount(commission.toSunAmount().toByteString())
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setBandwidthRequired(commissionFeeBuilderResult.requiredBandwidth!!)
                                            .setTxnBytes(commissionFeeBuilderResult.transaction!!)
                                            .build(),
                                )
                            }
                        }
                    }
                    isSellerNotPayedExpertFee(deal, userId) -> {
                        smartContractRepo.setSmartContractModalData(true, "Оплата комиссии экспертам")
                        blockchainOperations.approveAndPaySellerExpertFee(deal, userId).also { dealResult ->
                            if (dealResult is DealActionResult.Success) {
                                grpcOperations.callContract(
                                    userId = userId,
                                    status = SmartContractProto.DealContractChangeStatuses.SELLER_PAID_EXPERT_FEE,
                                    deal = deal,
                                    contractTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setEstimateEnergy(estimatorResult!!.requiredEnergy!!)
                                            .setBandwidthRequired(estimatorResult.requiredBandwidth!!)
                                            .setTxnBytes(dealResult.transaction)
                                            .build(),
                                    commissionTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAmount(commission.toSunAmount().toByteString())
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setBandwidthRequired(commissionFeeBuilderResult.requiredBandwidth!!)
                                            .setTxnBytes(commissionFeeBuilderResult.transaction!!)
                                            .build(),
                                )
                            }
                        }
                    }
                    isContractAwaitingUserConfirmation(deal, userId) -> {
                        smartContractRepo.setSmartContractModalData(true, "Подтверждение исполнения условий договора")
                        val smartContractChangeStatus =
                            when {
                                deal.buyer.userId == userId -> SmartContractProto.DealContractChangeStatuses.BUYER_CONFIRMED
                                deal.seller.userId == userId -> SmartContractProto.DealContractChangeStatuses.SELLER_CONFIRMED
                                else -> return CompleteReturnData(status = CompleteStatusesEnum.UNKNOWN_CONTRACT_STATE)
                            }
                        blockchainOperations.confirmDeal(deal, userId).also { dealResult ->
                            if (dealResult is DealActionResult.Success) {
                                grpcOperations.callContract(
                                    userId = userId,
                                    status = smartContractChangeStatus,
                                    deal = deal,
                                    contractTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setEstimateEnergy(estimatorResult!!.requiredEnergy!!)
                                            .setBandwidthRequired(estimatorResult.requiredBandwidth!!)
                                            .setTxnBytes(dealResult.transaction)
                                            .build(),
                                    commissionTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAmount(commission.toSunAmount().toByteString())
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setBandwidthRequired(commissionFeeBuilderResult.requiredBandwidth!!)
                                            .setTxnBytes(commissionFeeBuilderResult.transaction!!)
                                            .build(),
                                )
                            }
                        }
                    }
                    isExpertNotDecision(deal, userId) -> {
                        return CompleteReturnData(status = CompleteStatusesEnum.CALL_EXPERT_AMOUNT_SHEET)
                    }
                    isDisputeNotAgreed(deal, userId) -> {
                        smartContractRepo.setSmartContractModalData(true, "Регистрация решения в блокчейне")
                        val smartContractChangeStatus =
                            deal.adminsList.firstOrNull { it.userId == userId }?.let {
                                SmartContractProto.DealContractChangeStatuses.EXPERT_DISPUTE_AGREED
                            } ?: when (userId) {
                                deal.buyer.userId -> SmartContractProto.DealContractChangeStatuses.BUYER_DISPUTE_AGREED
                                deal.seller.userId -> SmartContractProto.DealContractChangeStatuses.SELLER_DISPUTE_AGREED
                                else -> return CompleteReturnData(status = CompleteStatusesEnum.UNKNOWN_CONTRACT_STATE)
                            }
                        blockchainOperations.voteOnDisputeResolution(deal, userId).also { dealResult ->
                            if (dealResult is DealActionResult.Success) {
                                grpcOperations.callContract(
                                    userId = userId,
                                    status = smartContractChangeStatus,
                                    deal = deal,
                                    contractTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setEstimateEnergy(estimatorResult!!.requiredEnergy!!)
                                            .setBandwidthRequired(estimatorResult.requiredBandwidth!!)
                                            .setTxnBytes(dealResult.transaction)
                                            .build(),
                                    commissionTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAmount(commission.toSunAmount().toByteString())
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setBandwidthRequired(commissionFeeBuilderResult.requiredBandwidth!!)
                                            .setTxnBytes(commissionFeeBuilderResult.transaction!!)
                                            .build(),
                                )
                            }
                        }
                    }
                    else -> {
                        return CompleteReturnData(status = CompleteStatusesEnum.UNKNOWN_CONTRACT_STATE)
                    }
                }

            smartContractRepo.setSmartContractModalData(false, "")

            return when (result) {
                is DealActionResult.Success -> CompleteReturnData(status = CompleteStatusesEnum.OK)
                is DealActionResult.InsufficientFunds, is DealActionResult.Error ->
                    CompleteReturnData(status = CompleteStatusesEnum.OK, result = result)
                else -> CompleteReturnData(status = CompleteStatusesEnum.UNKNOWN_CONTRACT_STATE)
            }
        }

        suspend fun processRejectSmartContract(
            commission: BigDecimal,
            deal: SmartContractProto.ContractDealListResponse,
        ): CompleteReturnData {
            val userId = profileRepo.getProfileUserId()
            val estimatorResult = processContractEstimatorUseCase.processRejectSmartContract(deal)
            val commissionFeeBuilderResult =
                commissionFeeBuilder.build(
                    commission = commission.toSunAmount(),
                    userId = userId,
                    deal = deal,
                )

            var result: DealActionResult? =
                when {
                    isBuyerRequestInitialized(deal, userId) -> {
                        smartContractRepo.setSmartContractModalData(true, "Инициирование закрытия контракта..")
                        grpcOperations.contractDealStatusChanged(
                            deal,
                            deal.smartContractAddress,
                            SmartContractProto.DealContractChangeStatuses.BUYER_DELETE_CONTRACT,
                        )
                        null
                    }
                    isBuyerNotDeposited(deal, userId) -> {
                        smartContractRepo.setSmartContractModalData(true, "Инициирование закрытия контракта..")
                        blockchainOperations.rejectCancelDeal(deal, userId).also { actionResult ->
                            if (actionResult is DealActionResult.Success) {
                                grpcOperations.callContract(
                                    userId = userId,
                                    status = SmartContractProto.DealContractChangeStatuses.BUYER_CANCEL_CONTRACT,
                                    deal = deal,
                                    contractTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setEstimateEnergy(estimatorResult!!.requiredEnergy!!)
                                            .setBandwidthRequired(estimatorResult.requiredBandwidth!!)
                                            .setTxnBytes(actionResult.transaction)
                                            .build(),
                                    commissionTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAmount(commission.toSunAmount().toByteString())
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setBandwidthRequired(commissionFeeBuilderResult.requiredBandwidth!!)
                                            .setTxnBytes(commissionFeeBuilderResult.transaction!!)
                                            .build(),
                                )
                            }
                        }
                    }
                    isSellerNotPayedExpertFee(deal, userId) || isSellerNotPayedExpertFee(deal, getOppositeUserId(deal, userId)) -> {
                        smartContractRepo.setSmartContractModalData(
                            true,
                            "Инициирование закрытия контракта, возврат средств и отправка комиссий..",
                        )
                        val smartContractChangeStatus =
                            when {
                                deal.buyer.userId == userId -> SmartContractProto.DealContractChangeStatuses.BUYER_CANCEL_PAID_CONTRACT
                                deal.seller.userId == userId -> SmartContractProto.DealContractChangeStatuses.SELLER_CANCEL_CONTRACT
                                else -> {
                                    Log.e("GetSmartContractViewModel", "Unknown contract state")
                                    return CompleteReturnData(status = CompleteStatusesEnum.UNKNOWN_CONTRACT_STATE)
                                }
                            }
                        blockchainOperations.rejectCancelDeal(deal, userId).also { actionResult ->
                            if (actionResult is DealActionResult.Success) {
                                grpcOperations.callContract(
                                    userId = userId,
                                    status = smartContractChangeStatus,
                                    deal = deal,
                                    contractTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setEstimateEnergy(estimatorResult!!.requiredEnergy!!)
                                            .setBandwidthRequired(estimatorResult.requiredBandwidth!!)
                                            .setTxnBytes(actionResult.transaction)
                                            .build(),
                                    commissionTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAmount(commission.toSunAmount().toByteString())
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setBandwidthRequired(commissionFeeBuilderResult.requiredBandwidth!!)
                                            .setTxnBytes(commissionFeeBuilderResult.transaction!!)
                                            .build(),
                                )
                            }
                        }
                    }
                    isContractAwaitingUserConfirmation(deal, userId) -> {
                        smartContractRepo.setSmartContractModalData(true, "Открытие диспута..")
                        val smartContractChangeStatus =
                            if (userId == deal.buyer.userId) {
                                SmartContractProto.DealContractChangeStatuses.BUYER_OPEN_DISPUTE
                            } else {
                                SmartContractProto.DealContractChangeStatuses.SELLER_OPEN_DISPUTE
                            }
                        blockchainOperations.executeDisputed(deal, userId).also { actionResult ->
                            if (actionResult is DealActionResult.Success) {
                                grpcOperations.callContract(
                                    userId = userId,
                                    status = smartContractChangeStatus,
                                    deal = deal,
                                    contractTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setEstimateEnergy(estimatorResult!!.requiredEnergy!!)
                                            .setBandwidthRequired(estimatorResult.requiredBandwidth!!)
                                            .setTxnBytes(actionResult.transaction)
                                            .build(),
                                    commissionTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAmount(commission.toSunAmount().toByteString())
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setBandwidthRequired(commissionFeeBuilderResult.requiredBandwidth!!)
                                            .setTxnBytes(commissionFeeBuilderResult.transaction!!)
                                            .build(),
                                )
                            }
                        }
                    }
                    isDisputeNotDeclined(deal, userId) -> {
                        smartContractRepo.setSmartContractModalData(true, "Регистрация решения в блокчейне")
                        val smartContractChangeStatus =
                            deal.adminsList.firstOrNull { it.userId == userId }?.let {
                                SmartContractProto.DealContractChangeStatuses.EXPERT_DISPUTE_DECLINE
                            } ?: when (userId) {
                                deal.buyer.userId -> SmartContractProto.DealContractChangeStatuses.BUYER_DISPUTE_DECLINE
                                deal.seller.userId -> SmartContractProto.DealContractChangeStatuses.SELLER_DISPUTE_DECLINE
                                else -> {
                                    Log.e("GetSmartContractViewModel", "Unknown contract state")
                                    return CompleteReturnData(status = CompleteStatusesEnum.UNKNOWN_CONTRACT_STATE)
                                }
                            }
                        blockchainOperations.declineDisputeResolution(deal, userId).also { actionResult ->
                            if (actionResult is DealActionResult.Success) {
                                grpcOperations.callContract(
                                    userId = userId,
                                    status = smartContractChangeStatus,
                                    deal = deal,
                                    contractTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setEstimateEnergy(estimatorResult!!.requiredEnergy!!)
                                            .setBandwidthRequired(estimatorResult.requiredBandwidth!!)
                                            .setTxnBytes(actionResult.transaction)
                                            .build(),
                                    commissionTransactionData =
                                        CallContractTransactionData
                                            .newBuilder()
                                            .setAmount(commission.toSunAmount().toByteString())
                                            .setAddress(commissionFeeBuilderResult.executorAddress!!)
                                            .setBandwidthRequired(commissionFeeBuilderResult.requiredBandwidth!!)
                                            .setTxnBytes(commissionFeeBuilderResult.transaction!!)
                                            .build(),
                                )
                            }
                        }
                    }
                    else -> {
                        Log.e("GetSmartContractViewModel", "Unknown contract state")
                        return CompleteReturnData(status = CompleteStatusesEnum.UNKNOWN_CONTRACT_STATE)
                    }
                }

            // Закрываем модальное окно
            smartContractRepo.setSmartContractModalData(false, "")

            return when (result) {
                is DealActionResult.Success -> CompleteReturnData(status = CompleteStatusesEnum.OK)
                is DealActionResult.InsufficientFunds, is DealActionResult.Error ->
                    CompleteReturnData(status = CompleteStatusesEnum.OK, result = result)
                null -> CompleteReturnData(status = CompleteStatusesEnum.UNKNOWN_CONTRACT_STATE)
            }
        }

        suspend fun expertSetDecision(
            deal: SmartContractProto.ContractDealListResponse,
            sellerValue: BigInteger,
            buyerValue: BigInteger,
        ) {
            val userId = profileRepo.getProfileUserId()

            smartContractRepo.setSmartContractModalData(true, "Установка новых условий для контракта")
            val result =
                blockchainOperations.assignDecisionAdminAndSetAmounts(
                    deal = deal,
                    userId = userId,
                    sellerValue = sellerValue,
                    buyerValue = buyerValue,
                )

            when (result) {
                is DealActionResult.Success -> {
                    grpcOperations.contractDealStatusExpertChanged(
                        deal,
                        deal.smartContractAddress,
                        SmartContractProto.DealContractChangeStatuses.EXPERT_SET_DECISION,
                    )
                }
                is DealActionResult.InsufficientFunds -> {
                    if (result.type == DealActionResult.InsufficientFunds.Type.BALANCE) {
                        println("Insufficient funds. Need ${result.amountRequired}")
                    } else if (result.type == DealActionResult.InsufficientFunds.Type.APPROVAL) {
                        CompleteReturnData(
                            status = CompleteStatusesEnum.OK,
                            result = result,
                        )
                    }
                }
                is DealActionResult.Error -> println("Error occurred: ${result.reason}")
            }

            smartContractRepo.setSmartContractModalData(false, "")
        }
    }

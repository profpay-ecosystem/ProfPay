package com.profpay.wallet.bridge.view_model.smart_contract.usecases.estimate

import com.profpay.wallet.bridge.view_model.smart_contract.usecases.getOppositeUserId
import com.profpay.wallet.bridge.view_model.smart_contract.usecases.isBuyerNotDeposited
import com.profpay.wallet.bridge.view_model.smart_contract.usecases.isBuyerRequestInitialized
import com.profpay.wallet.bridge.view_model.smart_contract.usecases.isContractAwaitingUserConfirmation
import com.profpay.wallet.bridge.view_model.smart_contract.usecases.isDisputeNotAgreed
import com.profpay.wallet.bridge.view_model.smart_contract.usecases.isDisputeNotDeclined
import com.profpay.wallet.bridge.view_model.smart_contract.usecases.isExpertNotDecision
import com.profpay.wallet.bridge.view_model.smart_contract.usecases.isSellerNotPayedExpertFee
import com.profpay.wallet.data.database.repositories.ProfileRepo
import org.example.protobuf.smart.SmartContractProto
import javax.inject.Inject

class ProcessContractEstimatorUseCase
    @Inject
    constructor(
        private val profileRepo: ProfileRepo,
        private val transactionFeeEstimator: TransactionFeeEstimator,
    ) {
        suspend fun processCompleteSmartContract(deal: SmartContractProto.ContractDealListResponse): TransactionEstimatorResult? {
            val userId = profileRepo.getProfileUserId()
            return when {
                isBuyerRequestInitialized(deal, userId) ->
                    transactionFeeEstimator.createDeal(deal)
                isBuyerNotDeposited(deal, userId) ->
                    transactionFeeEstimator.approveAndDepositDeal(deal)
                isSellerNotPayedExpertFee(deal, userId) ->
                    transactionFeeEstimator.approveAndPaySellerExpertFee(deal, userId)
                isContractAwaitingUserConfirmation(deal, userId) ->
                    transactionFeeEstimator.confirmDeal(deal, userId)
                isExpertNotDecision(deal, userId) ->
                    null
                isDisputeNotAgreed(deal, userId) ->
                    transactionFeeEstimator.voteOnDisputeResolution(deal, userId)
                else ->
                    TransactionEstimatorResult.Error(EstimateType.DEFAULT, "Unknown contract state")
            }
        }

        suspend fun processRejectSmartContract(deal: SmartContractProto.ContractDealListResponse): TransactionEstimatorResult? {
            val userId = profileRepo.getProfileUserId()

            return when {
                isBuyerRequestInitialized(deal, userId) ->
                    null
                isBuyerNotDeposited(deal, userId) ->
                    transactionFeeEstimator.rejectCancelDeal(deal, userId)
                isSellerNotPayedExpertFee(deal, userId) ||
                    isSellerNotPayedExpertFee(
                        deal,
                        getOppositeUserId(deal, userId),
                    ) ->
                        transactionFeeEstimator.rejectCancelDeal(deal, userId)
                isContractAwaitingUserConfirmation(deal, userId) ->
                    transactionFeeEstimator.executeDisputed(deal, userId)
                isDisputeNotDeclined(deal, userId) ->
                    transactionFeeEstimator.declineDisputeResolution(deal, userId)
                else ->
                    TransactionEstimatorResult.Error(EstimateType.DEFAULT, "Unknown contract state")
            }
        }
    }

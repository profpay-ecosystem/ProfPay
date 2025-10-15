package com.profpay.wallet.bridge.viewmodel.smartcontract.usecases

import org.example.protobuf.smart.SmartContractProto

private const val TRON_ADDRESS_ZERO = "T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb"

fun isBuyerRequestInitialized(
    deal: SmartContractProto.ContractDealListResponse,
    userId: Long,
): Boolean = deal.buyer.userId == userId && deal.dealBlockchainId == 0L

fun isBuyerNotDeposited(
    deal: SmartContractProto.ContractDealListResponse,
    userId: Long,
): Boolean = deal.buyer.userId == userId && deal.dealBlockchainId != 0L && !deal.dealData.paymentStatus.buyerDepositAndExpertFeePaid

fun isSellerNotPayedExpertFee(
    deal: SmartContractProto.ContractDealListResponse,
    userId: Long,
): Boolean =
    deal.seller.userId == userId &&
        deal.dealBlockchainId != 0L &&
        !deal.dealData.paymentStatus.sellerExpertFeePaid &&
        deal.dealData.paymentStatus.buyerDepositAndExpertFeePaid &&
        !deal.dealData.agreementStatus.disputed

fun isContractAwaitingUserConfirmation(
    deal: SmartContractProto.ContractDealListResponse,
    userId: Long,
): Boolean {
    val isBuyer = deal.buyer.userId == userId
    val isSeller = deal.seller.userId == userId
    val buyerNotAgreed = !deal.dealData.agreementStatus.buyerAgreed
    val sellerNotAgreed = !deal.dealData.agreementStatus.sellerAgreed
    val sellerFeePaid = deal.dealData.paymentStatus.sellerExpertFeePaid
    val buyerFeePaid = deal.dealData.paymentStatus.buyerDepositAndExpertFeePaid

    val isAwaitingBuyerConfirmation = isBuyer && buyerNotAgreed
    val isAwaitingSellerConfirmation = isSeller && sellerNotAgreed

    return ((isAwaitingBuyerConfirmation || isAwaitingSellerConfirmation) && sellerFeePaid && buyerFeePaid) &&
        !deal.dealData.agreementStatus.disputed
}

fun isAddressZero(address: String): Boolean = address == TRON_ADDRESS_ZERO

fun isExpertNotDecision(
    deal: SmartContractProto.ContractDealListResponse,
    userId: Long,
): Boolean =
    deal.dealBlockchainId != 0L &&
        deal.dealData.agreementStatus.disputed &&
        isAddressZero(deal.disputeResolutionStatus.decisionAdmin) &&
        isUserExpertComparison(deal, userId)

fun isDisputeNotAgreed(
    deal: SmartContractProto.ContractDealListResponse,
    userId: Long,
): Boolean =
    deal.dealBlockchainId != 0L &&
        deal.dealData.agreementStatus.disputed &&
        !isAddressZero(deal.disputeResolutionStatus.decisionAdmin) &&
        (
            isUserExpertComparison(deal, userId) &&
                isExpertNotAgreedComparison(deal, userId)
        ) ||
        deal.dealData.agreementStatus.disputed &&
        (
            deal.seller.userId == userId &&
                !deal.disputeResolutionStatus.sellerAgreed ||
                deal.buyer.userId == userId &&
                !deal.disputeResolutionStatus.buyerAgreed
        )

fun isDisputeNotDeclined(
    deal: SmartContractProto.ContractDealListResponse,
    userId: Long,
): Boolean =
    deal.dealBlockchainId != 0L &&
        deal.dealData.agreementStatus.disputed &&
        !isAddressZero(deal.disputeResolutionStatus.decisionAdmin) &&
        (
            isUserExpertComparison(deal, userId) &&
                isExpertNotDeclinedComparison(deal, userId)
        ) ||
        deal.dealData.agreementStatus.disputed &&
        (
            deal.seller.userId == userId &&
                !deal.disputeResolutionStatus.sellerDeclined ||
                deal.buyer.userId == userId &&
                !deal.disputeResolutionStatus.buyerDeclined
        )

private fun isExpertNotDeclinedComparison(
    deal: SmartContractProto.ContractDealListResponse,
    userId: Long,
): Boolean {
    // Найти администратора с соответствующим userId
    val admin = deal.adminsList.find { it.userId == userId }

    // Если администратор найден и его адрес есть в списке отклоненных, вернуть false, иначе true
    return admin?.let {
        deal.disputeResolutionStatus.adminsDeclinedList.none { it == admin.address }
    } ?: true
}

private fun isExpertNotAgreedComparison(
    deal: SmartContractProto.ContractDealListResponse,
    userId: Long,
): Boolean {
    // Найти администратора с соответствующим userId
    val admin = deal.adminsList.find { it.userId == userId }

    // Если администратор найден и его адрес есть в списке согласованных, вернуть false, иначе true
    return admin?.let {
        deal.disputeResolutionStatus.adminsAgreedList.none { it == admin.address }
    } ?: true
}

private fun isUserExpertComparison(
    deal: SmartContractProto.ContractDealListResponse,
    userId: Long,
): Boolean =
    deal.adminsList.any {
        it.userId == userId
    }

fun getOppositeUserId(
    deal: SmartContractProto.ContractDealListResponse,
    userId: Long,
): Long {
    // Логика получения идентификатора противоположной стороны
    return if (deal.buyer.userId == userId) {
        deal.seller.userId
    } else {
        deal.buyer.userId
    }
}

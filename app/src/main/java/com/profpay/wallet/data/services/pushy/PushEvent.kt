package com.profpay.wallet.data.services.pushy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

sealed interface PushEvent {
    @Serializable data class AmlPaymentSuccess(
        @SerialName("txid")
        val transactionId: String,
    ) : PushEvent
    @Serializable data class AmlPaymentError(
        @SerialName("txid")
        val transactionId: String,
    ) : PushEvent
    @Serializable data class TransferError(
        @SerialName("address")
        val senderAddress: String,
        @SerialName("amount")
        val amount: Long,
        @SerialName("trs_type")
        val transactionType: String,
        @SerialName("tx_id")
        val transactionId: String,
    ) : PushEvent
    @Serializable data class TransferSuccess(
        @SerialName("txid")
        val txid: String,
        @SerialName("sender_address")
        val senderAddress: String,
        @SerialName("amount")
        val amount: Long,
        @SerialName("transaction_type")
        val transactionType: String,
    ) : PushEvent
    @Serializable data class DeployContractSuccess(val contractAddress: String, val address: String) : PushEvent
    @Serializable data class NewTransaction(
        val txid: String,
        @SerialName("target_address") val targetAddress: String,
        val from: String,
        val to: String,
        val amount: String,
        val token: TransactionToken,
        val type: TransactionType,
        @SerialName("block_timestamp") val blockTimestamp: Long,
    ) : PushEvent
}

@Serializable
enum class TransactionToken(val symbol: String) {
    TRX("TRX"),
    USDT("USDT");
}

@Serializable
enum class TransactionType(val type: String) {
    DEFAULT("DEFAULT"),
    CentralAddress("CentralAddress");
}

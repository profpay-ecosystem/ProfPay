package com.example.telegramWallet.data.database.models
import androidx.room.ColumnInfo
import com.example.telegramWallet.data.database.entities.wallet.TransactionEntity
import java.math.BigInteger

data class TransactionModel(
    @ColumnInfo(name = "transaction_id") val transactionId: Long? = null,
    @ColumnInfo(name = "tx_id") val txId: String,
    @ColumnInfo(name = "sender_address_id") val senderAddressId: Long?,
    @ColumnInfo(name = "receiver_address_id") val receiverAddressId: Long?,
    @ColumnInfo(name = "sender_address") val senderAddress: String,
    @ColumnInfo(name = "receiver_address") val receiverAddress: String,
    @ColumnInfo(name = "wallet_id") val walletId: Long,
    @ColumnInfo(name = "token_name") val tokenName: String,
    @ColumnInfo(name = "amount") val amount: BigInteger,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "status") val status: String,
    @ColumnInfo(name = "is_processed", defaultValue = "0") val isProcessed: Boolean = false,
    @ColumnInfo(name = "server_response_received", defaultValue = "0") val serverResponseReceived: Boolean = false,
    @ColumnInfo(name = "transaction_date") val transactionDate: String,
    @ColumnInfo(name = "type") val type: Int,
    @ColumnInfo(name = "status_code") val statusCode: Int,
) {
    fun toEntity(): TransactionEntity =
        TransactionEntity(
            transactionId = transactionId,
            txId = txId,
            senderAddressId = senderAddressId,
            receiverAddressId = receiverAddressId,
            senderAddress = senderAddress,
            receiverAddress = receiverAddress,
            walletId = walletId,
            tokenName = tokenName,
            amount = amount,
            timestamp = timestamp,
            status = status,
            isProcessed = isProcessed,
            type = type,
            statusCode = statusCode,
        )
}

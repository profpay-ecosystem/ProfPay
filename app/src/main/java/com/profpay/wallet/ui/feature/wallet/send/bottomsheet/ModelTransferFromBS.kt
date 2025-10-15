package com.profpay.wallet.ui.feature.wallet.send.bottomsheet

import com.profpay.wallet.bridge.viewmodel.dto.TokenName
import com.profpay.wallet.data.database.models.AddressWithTokens
import org.example.protobuf.transfer.TransferProto
import java.math.BigDecimal

data class ModelTransferFromBS(
    val amount: BigDecimal,
    val tokenName: TokenName,
    val addressReceiver: String,
    val addressSender: String,
    val commission: BigDecimal,
    val addressWithTokens: AddressWithTokens?,
    val commissionResult: TransferProto.EstimateCommissionResponse,
)

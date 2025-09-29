package com.profpay.wallet.bridge.view_model.dto

import com.profpay.wallet.R

enum class TokenName(
    val tokenName: String,
    val shortName: String,
    val blockchainName: String,
    val paintIconId: Int,
) {
    TRX("TRX", shortName = "TRX", blockchainName = "Tron", paintIconId = R.drawable.trx_tron),
    USDT("USDT", shortName = "USDT", blockchainName = "Tron", paintIconId = R.drawable.usdt_tron),
}

enum class BlockchainName(
    val blockchainName: String,
    val tokens: List<TokenName>,
) {
    TRON("Tron", tokens = listOfNotNull(TokenName.TRX, TokenName.USDT)),
}

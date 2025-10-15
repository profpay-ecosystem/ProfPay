package com.profpay.wallet.ui.feature.wallet.walletAddress.model

import com.profpay.wallet.data.database.models.TransactionModel

data class GroupedTransactions(
    var all: List<List<TransactionModel?>>,
    var sender: List<List<TransactionModel?>>,
    var receiver: List<List<TransactionModel?>>,
)

package com.example.telegramWallet.ui.feature.wallet.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.telegramWallet.bridge.view_model.dto.TokenName
import com.example.telegramWallet.data.database.models.TransactionModel
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.ui.app.theme.BackgroundIcon
import com.example.telegramWallet.ui.screens.wallet.formatDate
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

@Composable
fun CATransactionListHistoryFeature(address: String, groupedTransaction: List<List<TransactionModel?>>) {
    val symbols = DecimalFormatSymbols().apply {
        groupingSeparator = '.'
        decimalSeparator = ','
    }
    val decimalFormat = DecimalFormat("#,###.###", symbols)

    if (groupedTransaction.isNotEmpty() && groupedTransaction[0].isNotEmpty()) {
        if (groupedTransaction[0][0] != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = 8.dp
                    ),
                contentPadding = PaddingValues(
                    horizontal = 0.dp,
                    vertical = 0.dp
                ),
            ) {
                groupedTransaction.forEach { list ->
                    item {
                        Text(
                            text = formatDate(list[0]!!.transactionDate),
                            modifier = Modifier.padding(
                                start = 4.dp,
                                top = 12.dp,
                                bottom = 4.dp
                            ),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    itemsIndexed(list) { _, item ->
                        if (item != null) {
                            val currentTokenName =
                                TokenName.entries.stream()
                                    .filter { it.tokenName == item.tokenName }
                                    .findFirst()
                                    .orElse(TokenName.USDT)

                            CACardHistoryTransactionsFeature(
                                paintIconId = currentTokenName.paintIconId,
                                shortNameToken = currentTokenName.shortName,
                                transactionEntity = item.toEntity(),
                                amount = decimalFormat.format(item.amount.toTokenAmount()),
                                address = address
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.size(10.dp)) }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "У вас пока нет транзакций...",
                style = MaterialTheme.typography.titleMedium,
                color = BackgroundIcon
            )
        }
    }
}
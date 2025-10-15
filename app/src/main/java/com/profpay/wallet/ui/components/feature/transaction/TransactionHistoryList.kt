package com.profpay.wallet.ui.components.feature.transaction

import StackedSnakbarHostState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.profpay.wallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.ui.app.theme.BackgroundIcon
import com.profpay.wallet.ui.shared.utils.formatDate


@Composable
fun TransactionHistoryList(
    groupedTransaction: List<List<TransactionModel?>>,
    type: TransactionCardType,
    viewModel: WalletAddressViewModel? = null,
    addressWithTokens: AddressWithTokens? = null,
    stackedSnackbarHostState: StackedSnakbarHostState? = null,
    goToSystemTRX: () -> Unit = {},
    goToTXDetailsScreen: () -> Unit = {},
    emptyText: String = "У вас пока нет транзакций...",
) {
    val condition =
        groupedTransaction.isNotEmpty() && groupedTransaction[0].isNotEmpty() && groupedTransaction[0][0] != null

    if (condition) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(0.dp),
            verticalArrangement = Arrangement.Top,
        ) {
            groupedTransaction.forEach { list ->
                item {
                    Text(
                        text = formatDate(list[0]!!.transactionDate),
                        modifier = Modifier.padding(start = 4.dp, top = 12.dp, bottom = 4.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }

                items(list) { item ->
                    if (item != null) {
                        UnifiedTransactionCard(
                            transaction = item,
                            type = type,
                            viewModel = viewModel,
                            addressWithTokens = addressWithTokens,
                            stackedSnackbarHostState = stackedSnackbarHostState,
                            goToSystemTRX = goToSystemTRX,
                            goToTXDetailsScreen = goToTXDetailsScreen,
                        )
                    }
                }
            }
            item { Spacer(modifier = Modifier.size(100.dp)) }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.titleMedium,
                color = BackgroundIcon,
            )
        }
    }
}

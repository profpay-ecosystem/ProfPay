package com.profpay.wallet.ui.feature.wallet.walletAddress.horizontalListsTrans

import StackedSnakbarHostState
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.profpay.wallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.ui.components.feature.transaction.TransactionCardType
import com.profpay.wallet.ui.components.feature.transaction.TransactionHistoryList

@Composable
fun TransactionsHPagerWalletAddressFeature(
    pagerState: PagerState,
    viewModel: WalletAddressViewModel,
    stackedSnackbarHostState: StackedSnakbarHostState,
    groupedAllTransaction: List<List<TransactionModel?>>,
    transactionsByAddressSender: List<TransactionModel>,
    transactionsByAddressReceiver: List<TransactionModel>,
    goToTXDetailsScreen: () -> Unit,
    goToSystemTRX: () -> Unit,
    addressWithTokens: AddressWithTokens?,
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
    ) { page ->
        when (page) {
            // Все транзакции
            0 -> TransactionHistoryList(
                groupedTransaction = groupedAllTransaction,
                type = TransactionCardType.WA,
                viewModel = viewModel,
                addressWithTokens = addressWithTokens,
                stackedSnackbarHostState = stackedSnackbarHostState,
                goToSystemTRX = { goToSystemTRX() },
                goToTXDetailsScreen = { goToTXDetailsScreen() },
            )
            // Отправленные
            1 -> {
                val groupedTransaction =
                    transactionsByTypeWalletAddressFeature(transactionsByAddressSender, viewModel)
                TransactionHistoryList(
                    groupedTransaction = groupedTransaction,
                    type = TransactionCardType.WA,
                    viewModel = viewModel,
                    addressWithTokens = addressWithTokens,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                    goToSystemTRX = { goToSystemTRX() },
                    goToTXDetailsScreen = { goToTXDetailsScreen() },
                )
            }
            // Полученные
            2 -> {
                val groupedTransaction =
                    transactionsByTypeWalletAddressFeature(transactionsByAddressReceiver, viewModel)
                TransactionHistoryList(
                    groupedTransaction = groupedTransaction,
                    type = TransactionCardType.WA,
                    viewModel = viewModel,
                    addressWithTokens = addressWithTokens,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                    goToSystemTRX = { goToSystemTRX() },
                    goToTXDetailsScreen = { goToTXDetailsScreen() },
                )
            }
        }
    }
}

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
            0 -> TransactionsTabWalletAddressFeature(
                groupedTransaction = groupedAllTransaction,
                viewModel = viewModel,
                stackedSnackbarHostState = stackedSnackbarHostState,
                goToTXDetailsScreen = goToTXDetailsScreen,
                goToSystemTRX = goToSystemTRX,
                addressWithTokens = addressWithTokens,
            )

            // Отправленные
            1 -> {
                val groupedTransaction =
                    transactionsByTypeWalletAddressFeature(transactionsByAddressSender, viewModel)
                TransactionsTabWalletAddressFeature(
                    groupedTransaction = groupedTransaction,
                    viewModel = viewModel,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                    goToTXDetailsScreen = goToTXDetailsScreen,
                    goToSystemTRX = goToSystemTRX,
                    addressWithTokens = addressWithTokens,
                )
            }

            // Полученные
            2 -> {
                val groupedTransaction =
                    transactionsByTypeWalletAddressFeature(transactionsByAddressReceiver, viewModel)
                TransactionsTabWalletAddressFeature(
                    groupedTransaction = groupedTransaction,
                    viewModel = viewModel,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                    goToTXDetailsScreen = goToTXDetailsScreen,
                    goToSystemTRX = goToSystemTRX,
                    addressWithTokens = addressWithTokens,
                )
            }
        }
    }
}

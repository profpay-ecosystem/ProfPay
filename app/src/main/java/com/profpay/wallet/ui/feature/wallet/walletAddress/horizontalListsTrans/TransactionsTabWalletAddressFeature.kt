package com.profpay.wallet.ui.feature.wallet.walletAddress.horizontalListsTrans

import StackedSnakbarHostState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.profpay.wallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.models.TransactionModel

@Composable
fun TransactionsTabWalletAddressFeature(
    groupedTransaction: List<List<TransactionModel?>>,
    viewModel: WalletAddressViewModel,
    stackedSnackbarHostState: StackedSnakbarHostState,
    goToTXDetailsScreen: () -> Unit,
    goToSystemTRX: () -> Unit,
    addressWithTokens: AddressWithTokens?
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        LazyListTransactionsFeature(
            viewModel = viewModel,
            isForWA = Pair(true, stackedSnackbarHostState),
            groupedTransaction = groupedTransaction,
            goToTXDetailsScreen = goToTXDetailsScreen,
            goToSystemTRX = goToSystemTRX,
            addressWithTokens = addressWithTokens,
        )
    }
}

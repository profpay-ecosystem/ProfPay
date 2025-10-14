package com.profpay.wallet.ui.feature.wallet.walletAddress.horizontalListsTrans
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.profpay.wallet.data.database.models.TransactionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun transactionsByTypeWalletAddressFeature(
    transactions: List<TransactionModel>,
    viewModel: WalletAddressViewModel
): List<List<TransactionModel?>> {
    return viewModel.getListTransactionToTimestamp(transactions)
}

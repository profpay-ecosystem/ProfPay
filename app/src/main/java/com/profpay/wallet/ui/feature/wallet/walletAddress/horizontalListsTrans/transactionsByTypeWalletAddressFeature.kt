package com.profpay.wallet.ui.feature.wallet.walletAddress.horizontalListsTrans
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.profpay.wallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.profpay.wallet.data.database.models.TransactionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun transactionsByTypeWalletAddressFeature(
    transactions: List<TransactionModel>,
    viewModel: WalletAddressViewModel
): List<List<TransactionModel?>> {
    var groupedTransaction by remember {
        mutableStateOf<List<List<TransactionModel?>>>(listOf(listOf(null)))
    }

    LaunchedEffect(transactions) {
        withContext(Dispatchers.IO) {
            groupedTransaction = viewModel.getListTransactionToTimestamp(transactions)
        }
    }

    return groupedTransaction
}

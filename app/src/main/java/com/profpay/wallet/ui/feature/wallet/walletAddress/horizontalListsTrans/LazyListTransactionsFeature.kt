package com.profpay.wallet.ui.feature.wallet.walletAddress.horizontalListsTrans
import StackedSnakbarHostState
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
import androidx.core.content.edit
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.bridge.view_model.dto.TokenName
import com.profpay.wallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.BackgroundIcon
import com.profpay.wallet.ui.feature.wallet.walletInfo.CardHistoryTransactionsFeature
import com.profpay.wallet.ui.shared.sharedPref
import com.profpay.wallet.ui.shared.utils.formatDate
import com.profpay.wallet.utils.decimalFormat


@Composable
fun LazyListTransactionsFeature(
    viewModel: WalletAddressViewModel? = null,
    addressWithTokens: AddressWithTokens? = null,
    isForWA: Pair<Boolean, StackedSnakbarHostState?> = Pair(false, null),
    groupedTransaction: List<List<TransactionModel?>>,
    goToTXDetailsScreen: () -> Unit,
    goToSystemTRX: () -> Unit = {},
) {
    val sharedPref = sharedPref()
    val addressWa = sharedPref.getString(PrefKeys.ADDRESS_FOR_WALLET_ADDRESS, "")

    if (groupedTransaction.isNotEmpty() && groupedTransaction[0].isNotEmpty()) {
        if (groupedTransaction[0][0] != null) {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(
                            horizontal = 8.dp,
                        ),
                contentPadding =
                    PaddingValues(
                        horizontal = 0.dp,
                        vertical = 0.dp,
                    ),
            ) {
                groupedTransaction.forEach { list ->
                    item {
                        Text(
                            text = formatDate(list[0]!!.transactionDate),
                            modifier =
                                Modifier.padding(
                                    start = 4.dp,
                                    top = 12.dp,
                                    bottom = 4.dp,
                                ),
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    itemsIndexed(list) { _, item ->
                        if (item != null) {
                            val currentTokenName =
                                TokenName.entries
                                    .stream()
                                    .filter { it.tokenName == item.tokenName }
                                    .findFirst()
                                    .orElse(TokenName.USDT)

                            if (isForWA.first) {
                                val currentAddress =
                                    if (item.receiverAddress == addressWa) {
                                        item.senderAddress
                                    } else {
                                        item.receiverAddress
                                    }

                                CardHistoryTransactionsForWAFeature(
                                    viewModel = viewModel!!,
                                    onClick = {
                                        sharedPref.edit {
                                            putLong(
                                                "transaction_id",
                                                item.transactionId!!,
                                            )
                                        }
                                        goToTXDetailsScreen()
                                    },
                                    paintIconId = currentTokenName.paintIconId,
                                    shortNameToken = currentTokenName.shortName,
                                    amount = decimalFormat(item.amount.toTokenAmount()),
                                    typeTransaction = item.type,
                                    address = currentAddress,
                                    transactionEntity = item.toEntity(),
                                    stackedSnackbarHostState = isForWA.second!!,
                                    goToSystemTRX = { goToSystemTRX() },
                                    addressWithTokens = addressWithTokens!!,
                                )
                            } else {
                                val currentAddress =
                                    if (item.receiverAddress == addressWa) {
                                        item.senderAddress
                                    } else {
                                        item.receiverAddress
                                    }

                                CardHistoryTransactionsFeature(
                                    onClick = {
                                        sharedPref.edit {
                                            putLong(
                                                "transaction_id",
                                                item.transactionId!!,
                                            )
                                        }
                                        goToTXDetailsScreen()
                                    },
                                    paintIconId = currentTokenName.paintIconId,
                                    shortNameToken = currentTokenName.shortName,
                                    transactionEntity = item.toEntity(),
                                    amount = decimalFormat(item.amount.toTokenAmount()),
                                    typeTransaction = item.type,
                                    address = currentAddress,
                                )
                            }
                        }
                    }
                }

                if (isForWA.first) {
                    item { Spacer(modifier = Modifier.size(100.dp)) }
                } else {
                    item { Spacer(modifier = Modifier.size(10.dp)) }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "У вас пока нет транзакций...",
                style = MaterialTheme.typography.titleMedium,
                color = BackgroundIcon,
            )
        }
    }
}
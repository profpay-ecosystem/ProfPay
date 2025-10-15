package com.profpay.wallet.ui.screens.wallet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.bridge.viewmodel.dto.TokenName
import com.profpay.wallet.bridge.viewmodel.wallet.transaction.CentralAddressTxHistoryViewModel
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.components.feature.transaction.TransactionCardType
import com.profpay.wallet.ui.components.feature.transaction.TransactionHistoryList
import com.profpay.wallet.ui.feature.wallet.walletAddress.TopCardForWalletAddressFeature
import rememberStackedSnackbarHostState
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CentralAddressTxHistoryScreen(
    goToBack: () -> Unit,
    viewModel: CentralAddressTxHistoryViewModel = hiltViewModel(),
) {
    val groupedAllTransaction by viewModel.groupedAllTransaction.collectAsStateWithLifecycle()

    var address by remember { mutableStateOf("empty") }
    var balanceTRX by remember { mutableStateOf(BigInteger.ZERO) }

    val stackedSnackbarHostState = rememberStackedSnackbarHostState()

    val transactionsByAddressSender by viewModel
        .getTransactionsByAddressAndTokenLD(
            walletId = 0,
            address = address,
            tokenName = "TRX",
            isSender = true,
            isCentralAddress = true,
        ).observeAsState(emptyList())

    val transactionsByAddressReceiver by viewModel
        .getTransactionsByAddressAndTokenLD(
            walletId = 0,
            address = address,
            tokenName = "TRX",
            isSender = false,
            isCentralAddress = true,
        ).observeAsState(emptyList())

    val centralAddress by viewModel.getCentralAddressLiveData().observeAsState()

    val allTransaction: List<TransactionModel> =
        transactionsByAddressSender + transactionsByAddressReceiver

    LaunchedEffect(allTransaction) {
        if (allTransaction.isEmpty()) return@LaunchedEffect
        viewModel.groupTransactions(allTransaction)
    }

    LaunchedEffect(centralAddress) {
        if (centralAddress != null) {
            address = centralAddress!!.address
            balanceTRX = centralAddress!!.balance
        }
    }
    CustomScaffoldWallet(stackedSnackbarHostState = stackedSnackbarHostState) { bottomPadding ->
        CustomTopAppBar(title = "Центральный адрес", goToBack = { goToBack() })
        TopCardForWalletAddressFeature(
            tokenNameObj = TokenName.TRX,
            tokenBalanceWithoutFrozen = "${balanceTRX.toTokenAmount()}",
        )
        CustomBottomCard(
            modifier = Modifier.weight(0.8f),
            modifierColumn = Modifier.padding(vertical = 4.dp),
            bottomPadding = bottomPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TransactionHistoryList(
                groupedTransaction = groupedAllTransaction,
                type = TransactionCardType.CA,
            )
        }
    }
}

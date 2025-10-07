package com.profpay.wallet.ui.screens.wallet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.bridge.view_model.dto.TokenName
import com.profpay.wallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTabRow
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.feature.wallet.tx_details.bottomSheet.bottomSheetRejectReceipt
import com.profpay.wallet.ui.feature.wallet.walletAddress.ActionsRowWalletAddressFeature
import com.profpay.wallet.ui.feature.wallet.walletAddress.TopCardForWalletAddressFeature
import com.profpay.wallet.ui.feature.wallet.walletAddress.horizontalListsTrans.TransactionsHPagerWalletAddressFeature
import com.profpay.wallet.ui.shared.sharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rememberStackedSnackbarHostState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WalletAddressScreen(
    viewModel: WalletAddressViewModel = hiltViewModel(),
    goToSendWalletAddress: (addressId: Long, tokenName: String) -> Unit,
    goToBack: () -> Unit,
    goToSystemTRX: () -> Unit,
    goToTXDetailsScreen: () -> Unit,
    goToReceive: () -> Unit,
) {
    val sharedPref = sharedPref()
    val coroutineScope = rememberCoroutineScope()

    val walletId = sharedPref.getLong("wallet_id", 1)
    val address = sharedPref.getString(PrefKeys.ADDRESS_FOR_WALLET_ADDRESS, "") ?: ""
    val tokenName =
        sharedPref.getString("token_name", TokenName.USDT.tokenName) ?: TokenName.USDT.tokenName

    val isActivated by viewModel.isActivated.collectAsState()

    val addressWithTokens by viewModel.getAddressWithTokensByAddressLD(address).observeAsState()

    val tokenNameObj =
        TokenName.entries
            .stream()
            .filter { it.tokenName == tokenName }
            .findFirst()
            .orElse(TokenName.USDT)

    val tokenEntity =
        addressWithTokens
            ?.tokens
            ?.stream()
            ?.filter { it.token.tokenName == tokenName }
            ?.findFirst()
            ?.orElse(null)

    val transactionsByAddressSender by viewModel
        .getTransactionsByAddressAndTokenLD(
            walletId = walletId,
            address = address,
            tokenName = tokenName,
            isSender = true,
            isCentralAddress = false,
        ).observeAsState(emptyList())

    val transactionsByAddressReceiver by viewModel
        .getTransactionsByAddressAndTokenLD(
            walletId = walletId,
            address = address,
            tokenName = tokenName,
            isSender = false,
            isCentralAddress = false,
        ).observeAsState(emptyList())

    val allTransaction: List<TransactionModel> =
        transactionsByAddressSender + transactionsByAddressReceiver

    val (groupedAllTransaction, setGroupedAllTransaction) =
        remember {
            mutableStateOf<List<List<TransactionModel?>>>(listOf(listOf(null)))
        }

    LaunchedEffect(allTransaction) {
        withContext(Dispatchers.IO) {
            setGroupedAllTransaction(viewModel.getListTransactionToTimestamp(allTransaction))
        }
    }

    LaunchedEffect(addressWithTokens) {
        if (addressWithTokens == null) return@LaunchedEffect
        viewModel.checkActivation(addressWithTokens!!.addressEntity.address)
    }

    val stackedSnackbarHostState = rememberStackedSnackbarHostState()

    val (_, setIsOpenRejectReceiptSheet) =
        bottomSheetRejectReceipt(
            viewModel = viewModel,
            addressWithTokens = addressWithTokens,
            snackbar = stackedSnackbarHostState,
            tokenName = tokenName,
        )

    CustomScaffoldWallet(stackedSnackbarHostState = stackedSnackbarHostState) { bottomPadding ->
        CustomTopAppBar(
            title = "${address.take(4)}...${address.takeLast(4)}",
            goToBack = { goToBack() },
        )
        TopCardForWalletAddressFeature(
            tokenNameObj = tokenNameObj,
            tokenBalanceWithoutFrozen = "${
                tokenEntity?.balanceWithoutFrozen?.toTokenAmount()
            }",
        )
        CustomBottomCard(
            modifier = Modifier.weight(0.8f),
            modifierColumn = Modifier.padding(vertical = 4.dp),
            bottomPadding = bottomPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val titles = listOf("All", "Send", "Receive")
            val pagerState = rememberPagerState(pageCount = { titles.size })

            CustomTabRow(titles = titles, pagerState = pagerState, coroutineScope = coroutineScope)

            TransactionsHPagerWalletAddressFeature(
                pagerState = pagerState,
                viewModel = viewModel,
                stackedSnackbarHostState = stackedSnackbarHostState,
                groupedAllTransaction = groupedAllTransaction,
                transactionsByAddressSender = transactionsByAddressSender,
                transactionsByAddressReceiver = transactionsByAddressReceiver,
                goToTXDetailsScreen = { goToTXDetailsScreen() },
                goToSystemTRX = { goToSystemTRX() },
                addressWithTokens = addressWithTokens
            )
        }
    }
    ActionsRowWalletAddressFeature(
        tokenEntity = tokenEntity,
        addressWithTokens = addressWithTokens,
        isActivated = isActivated,
        stackedSnackbarHostState = stackedSnackbarHostState,
        goToSendWalletAddress = goToSendWalletAddress,
        goToSystemTRX = { goToSystemTRX() },
        goToReceive = { goToReceive() },
        sharedPref = sharedPref,
        address = address,
        tokenName = tokenName,
        setIsOpenRejectReceiptSheet = { setIsOpenRejectReceiptSheet(true) },
    )
}

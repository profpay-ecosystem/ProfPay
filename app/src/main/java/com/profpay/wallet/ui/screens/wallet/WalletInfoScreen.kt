package com.profpay.wallet.ui.screens.wallet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.bridge.view_model.wallet.WalletInfoViewModel
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.feature.wallet.walletInfo.DotIndexPagerStateFeature
import com.profpay.wallet.ui.feature.wallet.walletInfo.HorizontalPagerWalletInfoFeature
import com.profpay.wallet.ui.feature.wallet.walletInfo.WalletInfoCardInfoFeature
import com.profpay.wallet.ui.feature.wallet.walletInfo.bottomSheetChoiceTokenToSend
import com.profpay.wallet.ui.shared.sharedPref
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WalletInfoScreen(
    viewModel: WalletInfoViewModel = hiltViewModel(),
    goToSendWalletInfo: (addressId: Long, tokenName: String) -> Unit,
    goToWalletSystem: () -> Unit,
    goToWalletSystemTRX: () -> Unit,
    goToWalletSots: () -> Unit,
    goToTXDetailsScreen: () -> Unit,
) {
    val sharedPref = sharedPref()
    val walletId = sharedPref.getLong("wallet_id", 1)

    val walletName by viewModel.walletName.collectAsStateWithLifecycle()
    val tokensWithTotalBalance by viewModel.tokensWithTotalBalance.collectAsStateWithLifecycle()
    val totalBalance by viewModel.totalBalance.collectAsStateWithLifecycle()
    val totalPercentage24h by viewModel.totalPercentage24h.collectAsStateWithLifecycle()
    val transactionsByDate by viewModel.transactionsByDate.collectAsStateWithLifecycle()

    val addressesSotsWithTokens by viewModel
        .getAddressesSotsWithTokens(
            walletId = walletId,
        ).observeAsState(emptyList())

    val allRelatedTransaction by viewModel
        .getAllRelatedTransactions(
            walletId = walletId,
        ).observeAsState(emptyList())

    LaunchedEffect(Unit) {
        snapshotFlow { addressesSotsWithTokens }
            .distinctUntilChanged()
            .collectLatest { addresses ->
                viewModel.getWalletNameById(walletId)
                viewModel.loadTokensWithTotalBalance(addresses)
                viewModel.updateTokenBalances(addresses)
            }
    }

    LaunchedEffect(tokensWithTotalBalance) {
        viewModel.calculateTotalBalance(tokensWithTotalBalance)
        viewModel.calculateTotalPercentage24h(tokensWithTotalBalance)
    }

    LaunchedEffect(allRelatedTransaction) {
        viewModel.groupTransactionsByDate(allRelatedTransaction)
    }

    val (_, setIsOpenSheetChoiceTokenToSend) =
        bottomSheetChoiceTokenToSend(
            listTokensWithTotalBalance = tokensWithTotalBalance,
            goToSendWalletInfo = goToSendWalletInfo,
        )

    CustomScaffoldWallet { bottomPadding ->
        CustomTopAppBar(
            title = walletName ?: "",
            goToNext = { goToWalletSystem() },
            iconNext = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        )
        WalletInfoCardInfoFeature(
            totalBalance = totalBalance,
            pricePercentage24h = totalPercentage24h,
            setIsOpenSheetChoiceTokenToSend = { setIsOpenSheetChoiceTokenToSend(true) },
            goToWalletSystemTRX = { goToWalletSystemTRX() },
        )

        CustomBottomCard(
            modifierColumn = Modifier.padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            bottomPadding = bottomPadding,
        ) {
            val pagerState = rememberPagerState(pageCount = { 2 })
            DotIndexPagerStateFeature(pagerState)
            HorizontalPagerWalletInfoFeature(
                viewModel = viewModel,
                pagerState = pagerState,
                listTokensWithTotalBalance = tokensWithTotalBalance,
                groupedTransaction = transactionsByDate,
                goToWalletSots = { goToWalletSots() },
                goToTXDetailsScreen = { goToTXDetailsScreen() },
            )
        }
    }
}

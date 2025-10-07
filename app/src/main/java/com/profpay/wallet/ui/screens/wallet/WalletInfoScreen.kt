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
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.profpay.wallet.bridge.view_model.wallet.WalletInfoViewModel
import com.profpay.wallet.data.database.entities.wallet.TokenEntity
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.feature.wallet.walletInfo.DotIndexPagerStateFeature
import com.profpay.wallet.ui.feature.wallet.walletInfo.HorizontalPagerWalletInfoFeature
import com.profpay.wallet.ui.feature.wallet.walletInfo.WalletInfoCardInfoFeature
import com.profpay.wallet.ui.feature.wallet.walletInfo.bottomSheetChoiceTokenToSend
import com.profpay.wallet.ui.shared.sharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WalletInfoScreen(
    viewModel: WalletInfoViewModel = hiltViewModel(),
    goToSendWalletInfo: (addressId: Long, tokenName: String) -> Unit,
    goToWalletSystem: () -> Unit,
    goToWalletSystemTRX: () -> Unit,
    goToWalletSots: () -> Unit,
    goToTXDetailsScreen: () -> Unit,
    navController: NavController,
) {
    val sharedPref = sharedPref()
    val walletId = sharedPref.getLong("wallet_id", 1)

    val addressesSotsWithTokens by viewModel
        .getAddressesSotsWithTokens(
            walletId = walletId,
        ).observeAsState(emptyList())

    val allRelatedTransaction by viewModel
        .getAllRelatedTransactions(
            walletId = walletId,
        ).observeAsState(emptyList())

    val (walletName, setWalletName) = remember { mutableStateOf("") }
    val (listTokensWithTotalBalance, setListTokensWithTotalBalance) =
        remember {
            mutableStateOf<List<TokenEntity?>>(listOf(null))
        }
    val (totalBalance, setTotalBalance) = remember { mutableStateOf(BigInteger.ZERO) }
    val (totalPPercentage24, setTotalPPercentage24) = remember { mutableDoubleStateOf(0.0) }

    LaunchedEffect(Unit) {
        snapshotFlow { addressesSotsWithTokens }
            .distinctUntilChanged()
            .collectLatest { addresses ->
                withContext(Dispatchers.IO) {
                    setWalletName(viewModel.getWalletNameById(walletId) ?: "")
                    setListTokensWithTotalBalance(viewModel.getListTokensWithTotalBalance(addresses))
                    viewModel.updateTokenBalances(addresses)
                }
            }
//        val telegramId = viewModel.getProfileTelegramId()
//        if (telegramId != null && telegramId != 0L) {
//            try {
//                viewModel.getUserPermissions(sharedPref, navController)
//            } catch (e: Exception) {
//                navController.navigate(WalletInfo.NotNetworkScreen.route) {
//                    popUpTo(0) { inclusive = true }
//                    launchSingleTop = true
//                }
//            }
//            snapshotFlow { addressesSotsWithTokens }
//                .distinctUntilChanged()
//                .collectLatest { addresses ->
//                    withContext(Dispatchers.IO) {
//                        setWalletName(viewModel.getWalletNameById(walletId) ?: "")
//                        setListTokensWithTotalBalance(viewModel.getListTokensWithTotalBalance(addresses))
//                        viewModel.updateTokenBalances(addresses)
//                    }
//                }
//        } else {
//            navController.navigate(SettingsS.SettingsAccount.route) {
//                popUpTo(0) { inclusive = true }
//                launchSingleTop = true
//            }
//        }
    }

    LaunchedEffect(listTokensWithTotalBalance) {
        setTotalBalance(viewModel.getTotalBalance(listTokensWithTotalBalance.filterNotNull()))
        setTotalPPercentage24(viewModel.getTotalPPercentage24(listTokensWithTotalBalance.filterNotNull()))
    }

    val (groupedTransaction, setGroupedTransaction) =
        remember {
            mutableStateOf<List<List<TransactionModel?>>>(listOf(listOf(null)))
        }

    LaunchedEffect(allRelatedTransaction) {
        withContext(Dispatchers.IO) {
            setGroupedTransaction(viewModel.getListTransactionToTimestamp(allRelatedTransaction))
        }
    }

    val (_, setIsOpenSheetChoiceTokenToSend) =
        bottomSheetChoiceTokenToSend(
            listTokensWithTotalBalance = listTokensWithTotalBalance,
            goToSendWalletInfo = goToSendWalletInfo,
        )

    CustomScaffoldWallet() { bottomPadding ->
        CustomTopAppBar(
            title = walletName,
            goToNext = { goToWalletSystem() },
            iconNext = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        )
        WalletInfoCardInfoFeature(
            totalBalance = totalBalance,
            pricePercentage24h = totalPPercentage24,
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
                listTokensWithTotalBalance = listTokensWithTotalBalance,
                groupedTransaction = groupedTransaction,
                goToWalletSots = { goToWalletSots() },
                goToTXDetailsScreen = { goToTXDetailsScreen() },
            )
        }
    }
}

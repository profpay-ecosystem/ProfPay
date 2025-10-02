package com.profpay.wallet.ui.screens.wallet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.profpay.wallet.R
import com.profpay.wallet.bridge.view_model.dto.TokenName
import com.profpay.wallet.bridge.view_model.wallet.WalletInfoViewModel
import com.profpay.wallet.data.database.entities.wallet.TokenEntity
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.database.entities.wallet.TransactionType
import com.profpay.wallet.data.database.models.TransactionModel
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.app.theme.RedColor
import com.profpay.wallet.ui.feature.wallet.walletInfo.CardForWalletInfoFeature
import com.profpay.wallet.ui.feature.wallet.walletInfo.bottomSheetChoiceTokenToSend
import com.profpay.wallet.ui.shared.sharedPref
import com.profpay.wallet.utils.decimalFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    val bottomPadding by remember { mutableFloatStateOf(sharedPref.getFloat("bottomPadding", 54f)) }

    Scaffold(
        modifier = Modifier,
    ) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .paint(
                        painterResource(id = R.drawable.wallet_background),
                        contentScale = ContentScale.FillBounds,
                    ),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            TopAppBar(
                title = {
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { goToWalletSystem() },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            modifier = Modifier.padding(start = 2.dp),
                            text = walletName,
                            style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                        )
                        Icon(
                            modifier = Modifier.size(30.dp),
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                actions = {
                    run {
                        IconButton(onClick = { /*goToBack()*/ }) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_alert),
                                contentDescription = "Back",
                                tint = Color.White,
                            )
                        }
                    }
                },
            )
            WalletInfoCardInfoFeature(
                totalBalance = totalBalance,
                pricePercentage24h = totalPPercentage24,
                setIsOpenSheetChoiceTokenToSend = { setIsOpenSheetChoiceTokenToSend(true) },
                goToWalletSystemTRX = { goToWalletSystemTRX() },
            )

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        .fillMaxHeight(),
//                    .weight(0.5f),
            ) {
                val pagerState = rememberPagerState(pageCount = { 2 })
                Column(
                    modifier =
                        Modifier
                            .padding(bottom = bottomPadding.dp)
                            .padding(horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        for (i in 0..1) {
                            val color =
                                if (i == pagerState.currentPage) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.surfaceBright
                                }
                            Box(
                                modifier =
                                    Modifier
                                        .padding(horizontal = 4.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .size(10.dp),
                            )
                        }
                    }
                    HorizontalPager(state = pagerState) { page ->
                        when (page) {
                            0 -> {
                                LazyColumn(
                                    modifier =
                                        Modifier
                                            .padding(
                                                vertical = 8.dp,
                                                horizontal = 8.dp,
                                            ).fillMaxSize(),
                                ) {
                                    itemsIndexed(listTokensWithTotalBalance) { _, tokenEntity ->
                                        if (tokenEntity != null) {
                                            val currentTokenName =
                                                TokenName.entries
                                                    .stream()
                                                    .filter { it.tokenName == tokenEntity.tokenName }
                                                    .findFirst()
                                                    .orElse(TokenName.USDT)

                                            CardForWalletInfoFeature(
                                                onClick = {
                                                    sharedPref.edit {
                                                        putString(
                                                            "token_name",
                                                            tokenEntity.tokenName,
                                                        )
                                                    }
                                                    goToWalletSots()
                                                },
                                                paintIconId = currentTokenName.paintIconId,
                                                label = tokenEntity.tokenName,
                                                shortNameToken = currentTokenName.shortName,
                                                balance = tokenEntity.balance,
                                                balanceForLastMonth = 32.0,
                                                viewModel = viewModel,
                                            )
                                        }
                                    }
                                    item { Spacer(modifier = Modifier.size(10.dp)) }
                                }
                            }

                            1 -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    LazyListTransactionsFeature(
                                        groupedTransaction = groupedTransaction,
                                        goToTXDetailsScreen = goToTXDetailsScreen,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WalletInfoCardInfoFeature(
    totalBalance: BigInteger,
    pricePercentage24h: Double,
    setIsOpenSheetChoiceTokenToSend: () -> Unit,
    goToWalletSystemTRX: () -> Unit,
) {
    Card(
        modifier =
            Modifier
                .padding(bottom = 10.dp, start = 10.dp, end = 10.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .clip(RoundedCornerShape(16.dp)),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterStart,
        ) {
            if (totalBalance.toTokenAmount() > BigDecimal(0)) {
                ColorBoxOnCardInfoFeature(pricePercentage24h)
            }
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(vertical = 16.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // row 1
                Row(
                    modifier =
                        Modifier
                            .padding()
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(modifier = Modifier.weight(0.8f)) {
                        Text(
                            text = "Total balance",
                            style = MaterialTheme.typography.headlineSmall,
                        )
                    }
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .clickable { },
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Box(
                                modifier =
                                    Modifier
                                        .size(20.dp)
                                        .paint(
                                            painterResource(id = R.drawable.trx_tron),
                                            contentScale = ContentScale.FillBounds,
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {}
                            Spacer(modifier = Modifier.size(4.dp))
                            Text(
                                modifier = Modifier.padding(),
                                text = "Tron",
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
                // row 2
                Row(
                    modifier =
                        Modifier
                            .padding(vertical = 10.dp)
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(modifier = Modifier) {
//                                Canvas(modifier = Modifier.size(width = 140.dp, height = 70.dp)) {
//                                    drawRect(Color.Green.copy(alpha = 0.4f))
//                                }
                            Text(
                                text = "$${decimalFormat(totalBalance.toTokenAmount())}",
                                fontSize = 35.sp,
                                style = MaterialTheme.typography.displayMedium,
                            )
                        }
                    }
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.End,
                    ) {
                        Text(
                            text = "24 hours:",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        if (pricePercentage24h >= 0.0) {
                            Text(
                                "+${decimalFormat(pricePercentage24h.toBigDecimal())}%",
                                color = GreenColor,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        } else {
                            Text(
                                "${decimalFormat(pricePercentage24h.toBigDecimal())}%",
                                color = RedColor,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
                // row 3
                Row(
                    modifier =
                        Modifier
                            .padding(top = 20.dp)
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        modifier =
                            Modifier
                                .padding(end = 8.dp)
                                .fillMaxWidth()
                                .weight(0.5f)
                                .height(50.dp)
                                .shadow(7.dp, RoundedCornerShape(10.dp))
                                .clickable {
                                    setIsOpenSheetChoiceTokenToSend()
                                },
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_send),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(text = "Отправить")
                        }
                    }
                    Card(
                        modifier =
                            Modifier
                                .padding(start = 8.dp)
                                .fillMaxWidth()
                                .weight(0.5f)
                                .height(50.dp)
                                .shadow(7.dp, RoundedCornerShape(10.dp))
                                .clickable {
                                    goToWalletSystemTRX()
                                },
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_get),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onBackground,
                            )
                            Text(text = "Системный TRX")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ColorBoxOnCardInfoFeature(pricePercentage24h: Double) {
    val color =
        if (pricePercentage24h >= 0.0) {
            GreenColor.copy(alpha = 0.6f, green = 1f)
        } else {
            Color.Red.copy(alpha = 0.4f)
        }

    Box(
        modifier =
            Modifier
                .fillMaxSize(0.7f)
                .background(Color.Transparent),
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush =
                    Brush.radialGradient(
                        colors = listOf(color, Color.Transparent),
                        center = Offset(size.width * 0.35f, size.height * 0.3f),
                        radius = size.minDimension * 0.5f,
                    ),
                radius = size.minDimension * 0.8f,
            )
        }
    }
}

@Composable
fun CardHistoryTransactionsFeature(
    address: String,
    typeTransaction: Int,
    paintIconId: Int,
    amount: String,
    shortNameToken: String,
    transactionEntity: TransactionEntity,
    onClick: () -> Unit = {},
) {
    val sharedPref = sharedPref()

    val label =
        when (typeTransaction) {
            TransactionType.SEND.index -> "Отправлено"
            TransactionType.RECEIVE.index -> "Получено"
            TransactionType.BETWEEN_YOURSELF.index -> "Между своими"
            else -> {
                ""
            }
        }
    val label2 =
        when (typeTransaction) {
            TransactionType.SEND.index -> "Куда: ${transactionEntity.receiverAddress.take(5)}...${transactionEntity.receiverAddress.takeLast(5)}"
            TransactionType.RECEIVE.index -> "Откуда: ${transactionEntity.senderAddress.take(5)}...${transactionEntity.senderAddress.takeLast(5)}"
            TransactionType.BETWEEN_YOURSELF.index ->
                "Откуда: ${transactionEntity.senderAddress.take(5)}..." +
                    "${transactionEntity.senderAddress.takeLast(5)}\n" +
                    "Куда: ${transactionEntity.receiverAddress.take(5)}..." +
                    transactionEntity.receiverAddress.takeLast(5)

            else -> return
        }

    Card(
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .shadow(7.dp, RoundedCornerShape(10.dp)),
        onClick = { onClick() },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(start = 10.dp, end = 16.dp)
                        .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp)
                                .size(40.dp)
                                .paint(
                                    painterResource(id = paintIconId),
                                    contentScale = ContentScale.FillBounds,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {}
                    Column(modifier = Modifier.padding(horizontal = 12.dp, 8.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Text(text = label2, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 10.dp)
                        .fillMaxWidth()
                        .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
            ) {
                Text(
                    text = "$amount $shortNameToken",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

fun formatDate(inputDate: String): String {
    val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val outputFormat =
        SimpleDateFormat(
            "dd MMMM",
            Locale("ru", "RU"),
        ) // Устанавливаем локаль для русского языка

    val date = inputFormat.parse(inputDate)
    return outputFormat.format(date!!)
}

fun convertTimestampToDateTime(timestamp: Long): String {
    val date = Date(timestamp)
    val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())
    return sdf.format(date)
}

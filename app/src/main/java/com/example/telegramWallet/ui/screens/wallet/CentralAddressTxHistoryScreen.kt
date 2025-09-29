package com.example.telegramWallet.ui.screens.wallet

import StackedSnackbarHost
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.wallet.transaction.CentralAddressTxHistoryViewModel
import com.example.telegramWallet.data.database.models.TransactionModel
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.ui.app.theme.BackgroundIcon
import com.example.telegramWallet.ui.feature.wallet.transaction.CATransactionListHistoryFeature
import com.example.telegramWallet.ui.shared.sharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rememberStackedSnackbarHostState
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CentralAddressTxHistoryScreen(
    goToBack: () -> Unit,
    viewModel: CentralAddressTxHistoryViewModel = hiltViewModel(),
) {
    val bottomPadding = sharedPref().getFloat("bottomPadding", 54f)
    var address by remember { mutableStateOf("empty") }
    var balanceTRX by remember { mutableStateOf(BigInteger.ZERO) }

    val coroutineScope = rememberCoroutineScope()
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

    val allTransaction: List<TransactionModel> = transactionsByAddressSender + transactionsByAddressReceiver

    val (groupedAllTransaction, setGroupedAllTransaction) =
        remember {
            mutableStateOf<List<List<TransactionModel?>>>(listOf(listOf(null)))
        }

    LaunchedEffect(allTransaction) {
        withContext(Dispatchers.IO) {
            setGroupedAllTransaction(viewModel.getListTransactionToTimestamp(allTransaction))
        }
    }

    LaunchedEffect(centralAddress) {
        if (centralAddress != null) {
            address = centralAddress!!.address
            balanceTRX = centralAddress!!.balance
        }
    }

    Scaffold(
        modifier = Modifier,
        snackbarHost = {
            StackedSnackbarHost(
                hostState = stackedSnackbarHostState,
                modifier =
                    Modifier
                        .padding(8.dp, (bottomPadding + 50).dp),
            )
        },
    ) { padding ->

        Column(
            modifier =
                Modifier
                    .padding()
                    .fillMaxSize()
                    .paint(
                        painterResource(id = R.drawable.wallet_background),
                        contentScale = ContentScale.FillBounds,
                    ),
            verticalArrangement = Arrangement.Bottom,
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Центральный адрес",
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                navigationIcon = {
                    run {
                        IconButton(onClick = { goToBack() }) {
                            Icon(
                                modifier = Modifier.size(34.dp),
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = Color.White,
                            )
                        }
                    }
                },
                actions = {
                    run {
                        IconButton(onClick = { /*goToBack()*/ }) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_alert),
                                contentDescription = "",
                                tint = Color.White,
                            )
                        }
                    }
                },
            )

            Card(
                modifier =
                    Modifier
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp)
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .clip(
                            RoundedCornerShape(15.dp),
                        ),
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
                                .padding(horizontal = 10.dp)
                                .fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier =
                                    Modifier
                                        .padding(vertical = 8.dp)
                                        .size(45.dp)
                                        .fillMaxSize(0.1f)
                                        .paint(
                                            painterResource(id = R.drawable.trx_tron),
                                            contentScale = ContentScale.FillBounds,
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {}
                            Column(modifier = Modifier.padding(horizontal = 12.dp, 0.dp)) {
                                Text(
                                    text = "TRX",
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                )
                                Text(
                                    text = "${balanceTRX.toTokenAmount()}",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                }
            }

            Card(
                modifier =
                    Modifier
                        .padding()
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 0.dp,
                            ),
                        ).weight(0.8f),
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(bottom = bottomPadding.dp)
                            .padding(vertical = 4.dp, horizontal = 0.dp)
                            .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val titles = listOf("All", "Send", "Receive")
                    val pagerState = rememberPagerState(pageCount = { titles.size })
                    TabRow(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        selectedTabIndex = pagerState.currentPage,
                        containerColor = Color.Transparent,
                        divider = {},
                        indicator = {},
                    ) {
                        titles.forEachIndexed { index, title ->
                            Tab(
                                selectedContentColor = MaterialTheme.colorScheme.onPrimary,
                                unselectedContentColor = BackgroundIcon,
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.scrollToPage(index)
                                    }
                                },
                                text = {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 18.sp),
                                        maxLines = 2,
                                    )
                                },
                            )
                        }
                    }
                    HorizontalPager(
                        state = pagerState,
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .fillMaxHeight(),
                    ) { page ->
                        when (page) {
                            0 -> {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top,
                                ) {
                                    CATransactionListHistoryFeature(
                                        address = address,
                                        groupedTransaction = groupedAllTransaction,
                                    )
                                }
                            }

                            1 -> {
                                val (groupedTransaction, setGroupedTransaction) =
                                    remember {
                                        mutableStateOf<List<List<TransactionModel?>>>(
                                            listOf(
                                                listOf(
                                                    null,
                                                ),
                                            ),
                                        )
                                    }

                                LaunchedEffect(allTransaction) {
                                    withContext(Dispatchers.IO) {
                                        setGroupedTransaction(
                                            viewModel.getListTransactionToTimestamp(
                                                transactionsByAddressSender,
                                            ),
                                        )
                                    }
                                }

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top,
                                ) {
                                    CATransactionListHistoryFeature(
                                        address = address,
                                        groupedTransaction = groupedTransaction,
                                    )
                                }
                            }

                            2 -> {
                                val (groupedTransaction, setGroupedTransaction) =
                                    remember {
                                        mutableStateOf<List<List<TransactionModel?>>>(
                                            listOf(
                                                listOf(
                                                    null,
                                                ),
                                            ),
                                        )
                                    }

                                LaunchedEffect(allTransaction) {
                                    withContext(Dispatchers.IO) {
                                        setGroupedTransaction(
                                            viewModel.getListTransactionToTimestamp(
                                                transactionsByAddressReceiver,
                                            ),
                                        )
                                    }
                                }
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Top,
                                ) {
                                    CATransactionListHistoryFeature(
                                        address = address,
                                        groupedTransaction = groupedTransaction,
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

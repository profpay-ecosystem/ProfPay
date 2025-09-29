package com.example.telegramWallet.ui.screens.wallet

import StackedSnackbarHost
import StackedSnakbarHostState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.PrefKeys
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.dto.TokenName
import com.example.telegramWallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.example.telegramWallet.data.database.entities.wallet.TransactionEntity
import com.example.telegramWallet.data.database.entities.wallet.TransactionType
import com.example.telegramWallet.data.database.models.AddressWithTokens
import com.example.telegramWallet.data.database.models.TransactionModel
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.ui.app.theme.BackgroundIcon
import com.example.telegramWallet.ui.feature.wallet.tx_details.bottomSheetRejectReceipt
import com.example.telegramWallet.ui.feature.wallet.tx_details.bottomSheetTransOnGeneralReceipt
import com.example.telegramWallet.ui.shared.sharedPref
import com.example.telegramWallet.ui.widgets.dialog.AlertDialogWidget
import com.example.telegramWallet.utils.decimalFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rememberStackedSnackbarHostState
import java.math.BigInteger

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
    val tokenName = sharedPref.getString("token_name", TokenName.USDT.tokenName) ?: TokenName.USDT.tokenName

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

    LaunchedEffect(addressWithTokens) {
        if (addressWithTokens == null) return@LaunchedEffect
        viewModel.checkActivation(addressWithTokens!!.addressEntity.address)
    }

    val stackedSnackbarHostState = rememberStackedSnackbarHostState()
    val bottomPadding = sharedPref().getFloat("bottomPadding", 54f)

    val (_, setIsOpenRejectReceiptSheet) =
        bottomSheetRejectReceipt(
            viewModel = viewModel,
            addressWithTokens = addressWithTokens,
            snackbar = stackedSnackbarHostState,
            tokenName = tokenName,
        )

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
                        text = "${address.take(4)}...${address.takeLast(4)}",
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
                                            painterResource(id = tokenNameObj.paintIconId),
                                            contentScale = ContentScale.FillBounds,
                                        ),
                                contentAlignment = Alignment.Center,
                            ) {}
                            Column(modifier = Modifier.padding(horizontal = 12.dp, 0.dp)) {
                                Text(
                                    text = tokenNameObj.shortName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                                )
                                Text(
                                    text = "${
                                        tokenEntity?.balanceWithoutFrozen?.toTokenAmount()
                                    }",
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
//                    Surface(modifier = Modifier.padding(padding)) {
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
                                    LazyListTransactionsFeature(
                                        viewModel = viewModel,
                                        isForWA = Pair(true, stackedSnackbarHostState),
                                        groupedTransaction = groupedAllTransaction,
                                        goToTXDetailsScreen = { goToTXDetailsScreen() },
                                        goToSystemTRX = { goToSystemTRX() },
                                        addressWithTokens = addressWithTokens,
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
                                    LazyListTransactionsFeature(
                                        viewModel = viewModel,
                                        isForWA = Pair(true, stackedSnackbarHostState),
                                        groupedTransaction = groupedTransaction,
                                        goToTXDetailsScreen = { goToTXDetailsScreen() },
                                        goToSystemTRX = { goToSystemTRX() },
                                        addressWithTokens = addressWithTokens,
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
                                    LazyListTransactionsFeature(
                                        viewModel = viewModel,
                                        isForWA = Pair(true, stackedSnackbarHostState),
                                        groupedTransaction = groupedTransaction,
                                        goToTXDetailsScreen = { goToTXDetailsScreen() },
                                        goToSystemTRX = { goToSystemTRX() },
                                        addressWithTokens = addressWithTokens,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    Column(
        modifier =
            Modifier
                .padding(bottom = bottomPadding.dp)
                .fillMaxSize()
                .background(Color.Transparent),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Row(
            modifier =
                Modifier
                    .height(IntrinsicSize.Min)
                    .padding(8.dp)
                    .fillMaxSize()
                    .background(Color.Transparent)/*delete this?*/,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (tokenEntity
                    ?.balanceWithoutFrozen
                    ?.let { it > BigInteger.ZERO } == true &&
                addressWithTokens?.addressEntity?.isGeneralAddress!!
            ) {
                Card(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .weight(0.5f)
                            .shadow(7.dp, RoundedCornerShape(10.dp))
                            .clickable {
                                addressWithTokens?.addressEntity?.addressId?.let {
                                    goToSendWalletAddress(it, tokenName)
                                }
                            },
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp),
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
            } else if (tokenEntity
                    ?.balanceWithoutFrozen
                    ?.let { it > BigInteger.ZERO } == true &&
                !addressWithTokens?.addressEntity?.isGeneralAddress!!
            ) {
                Card(
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .weight(0.5f)
                            .shadow(7.dp, RoundedCornerShape(10.dp))
                            .clickable {
                                if (!isActivated) {
                                    stackedSnackbarHostState.showErrorSnackbar(
                                        title = "Перевод валюты невозможен",
                                        description = "Для перевода необходимо активировать адрес, отправив 1 TRX.",
                                        actionTitle = "Перейти",
                                        action = { goToSystemTRX() },
                                    )
                                } else {
                                    setIsOpenRejectReceiptSheet(true)
                                }
                            },
                ) {
                    Column(
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .padding(vertical = 4.dp),
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
            }

            var openDialog by remember { mutableStateOf(false) }
            Card(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth()
                        .weight(0.5f)
                        .height(IntrinsicSize.Min)
                        .shadow(7.dp, RoundedCornerShape(10.dp))
                        .clickable {
                            if (addressWithTokens?.addressEntity?.isGeneralAddress == true) {
                                openDialog = !openDialog
                            } else {
                                sharedPref
                                    .edit {
                                        putString(
                                            PrefKeys.ADDRESS_FOR_RECEIVE,
                                            address,
                                        )
                                    }
                                goToReceive()
                            }
                        },
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_get),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(text = "Пополнить")
                }
            }
            if (openDialog) {
                AlertDialogWidget(
                    onConfirmation = {
                        sharedPref
                            .edit {
                                putString(PrefKeys.ADDRESS_FOR_RECEIVE, address)
                            }
                        goToReceive()
                        openDialog = !openDialog
                    },
                    onDismissRequest = {
                        openDialog = !openDialog
                    },
                    dialogTitle = "Главный адрес",
                    dialogText =
                        "Пополнение главной соты не рекомендуется " +
                            "вместо этого скопируйте любую доп-соту и пополните ее.\nПосле AML проверки " +
                            "Вы сможете перевести валюту на центральную соту, " +
                            "так Ваш центральный адрес будет чист всегда.",
                    textConfirmButton = "Всё-равно продолжить",
                    textDismissButton = "Закрыть",
                )
            }
        }
    }
}

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

@Composable
fun CardHistoryTransactionsForWAFeature(
    viewModel: WalletAddressViewModel,
    transactionEntity: TransactionEntity,
    addressWithTokens: AddressWithTokens,
    address: String,
    typeTransaction: Int,
    paintIconId: Int,
    amount: String,
    shortNameToken: String,
    onClick: () -> Unit = {},
    goToSystemTRX: () -> Unit = {},
    stackedSnackbarHostState: StackedSnakbarHostState,
) {
    val sharedPref = sharedPref()
    val addressWa = sharedPref.getString(PrefKeys.ADDRESS_FOR_WALLET_ADDRESS, "")

    val isActivated by viewModel.isActivated.collectAsState()

    val isGeneralAddressReceive by produceState(initialValue = false) {
        value = viewModel.isGeneralAddress(transactionEntity.receiverAddress)
    }

    val transactionTitle =
        when (typeTransaction) {
            TransactionType.SEND.index -> "Отправлено"
            TransactionType.RECEIVE.index -> "Получено"
            TransactionType.BETWEEN_YOURSELF.index -> "Между своими"
            else -> ""
        }

    val transactionDetails =
        when (typeTransaction) {
            TransactionType.SEND.index -> "Куда: ${address.take(5)}...${address.takeLast(5)}"
            TransactionType.RECEIVE.index -> "Откуда: ${address.take(5)}...${address.takeLast(5)}"
            TransactionType.BETWEEN_YOURSELF.index ->
                buildString {
                    append("Откуда: ${transactionEntity.senderAddress.take(5)}...${transactionEntity.senderAddress.takeLast(5)}\n")
                    append("Куда: ${transactionEntity.receiverAddress.take(5)}...${transactionEntity.receiverAddress.takeLast(5)}")
                }
            else -> return
        }

    LaunchedEffect(Unit) {
        viewModel.checkActivation(transactionEntity.receiverAddress)
    }

    val betweenYourselfReceiver =
        typeTransaction == TransactionType.BETWEEN_YOURSELF.index && transactionEntity.receiverAddress == addressWa

    val (_, setIsOpenTransOnGeneralReceiptSheet) =
        bottomSheetTransOnGeneralReceipt(
            viewModel = viewModel,
            addressWithTokens = addressWithTokens,
            snackbar = stackedSnackbarHostState,
            tokenName = transactionEntity.tokenName,
            walletId = transactionEntity.walletId,
            balance = transactionEntity.amount,
        )

    Card(
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .shadow(7.dp, RoundedCornerShape(10.dp)),
        onClick = { onClick() },
    ) {
        Column(
            modifier =
                Modifier
                    .padding(top = 8.dp, bottom = 8.dp),
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
                            .padding(start = 10.dp, end = 12.dp)
                            .fillMaxHeight(),
                    verticalArrangement = Arrangement.Center,
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier =
                                Modifier
                                    .size(40.dp)
                                    .paint(
                                        painterResource(id = paintIconId),
                                        contentScale = ContentScale.FillBounds,
                                    ),
                            contentAlignment = Alignment.Center,
                        ) {}
                        Column(modifier = Modifier.padding(horizontal = 10.dp)) {
                            Text(
                                text = transactionTitle,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                            Text(text = transactionDetails, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .padding(start = 10.dp)
                            .fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                ) {
                    Text(
                        modifier = Modifier.weight(0.8f),
                        textAlign = TextAlign.End,
                        text = "$amount $shortNameToken",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Icon(
                        modifier =
                            Modifier
                                .size(20.dp)
                                .weight(0.2f),
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_more_vert),
                        contentDescription = "Back",
                    )
                }
            }

            if ((!isGeneralAddressReceive && typeTransaction == TransactionType.RECEIVE.index && !transactionEntity.isProcessed) ||
                (!isGeneralAddressReceive && betweenYourselfReceiver && !transactionEntity.isProcessed)
            ) {
                Row(
                    modifier =
                        Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                ) {
                    Card(
                        modifier =
                            Modifier
                                .padding(horizontal = 8.dp)
                                .fillMaxWidth()
                                .weight(0.5f)
                                .shadow(7.dp, RoundedCornerShape(7.dp))
                                .clickable {
                                    viewModel.viewModelScope.launch {
                                        if (!isActivated) {
                                            stackedSnackbarHostState.showErrorSnackbar(
                                                title = "Перевод валюты невозможен",
                                                description = "Для активации необходимо перейти в «Системный TRX»",
                                                actionTitle = "Перейти",
                                                action = { goToSystemTRX() },
                                            )
                                        } else {
                                            setIsOpenTransOnGeneralReceiptSheet(true)
                                        }
                                    }
                                },
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Принять на Главный адрес",
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

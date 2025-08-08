package com.example.telegramWallet.ui.screens.wallet

import StackedSnackbarHost
import StackedSnakbarHostState
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.R
import com.example.telegramWallet.backend.http.models.binance.BinanceSymbolEnum
import com.example.telegramWallet.bridge.view_model.dto.TokenName
import com.example.telegramWallet.bridge.view_model.wallet.TXDetailsViewModel
import com.example.telegramWallet.data.flow_db.repo.AmlResult
import com.example.telegramWallet.data.utils.toBigInteger
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.exceptions.aml.ServerAmlException
import com.example.telegramWallet.tron.Transactions
import com.example.telegramWallet.ui.app.theme.BackgroundContainerButtonLight
import com.example.telegramWallet.ui.app.theme.GreenColor
import com.example.telegramWallet.ui.app.theme.PubAddressDark
import com.example.telegramWallet.ui.feature.wallet.tx_details.KnowAMLFeature
import com.example.telegramWallet.ui.feature.wallet.tx_details.UnknownAMLFeature
import com.example.telegramWallet.ui.shared.sharedPref
import com.example.telegramWallet.ui.widgets.dialog.AlertDialogWidget
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import rememberStackedSnackbarHostState
import java.math.BigInteger
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TXDetailsScreen(
    goToBack: () -> Unit,
    viewModel: TXDetailsViewModel = hiltViewModel()
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val amlState by viewModel.state.collectAsStateWithLifecycle()
    val decimalFormat = DecimalFormat("#.###")
    val sharedPref = sharedPref()

    val walletId = sharedPref.getLong("wallet_id", 1)
    val transactionId = sharedPref.getLong("transaction_id", 1)

    val commissionBySending by remember { mutableDoubleStateOf(0.01) }

    val stackedSnackbarHostState = rememberStackedSnackbarHostState()

    val transactionEntity by viewModel.getTransactionLiveDataById(transactionId).observeAsState()

    val amlFeeResult by viewModel.amlFeeResult.collectAsStateWithLifecycle()

    val (walletName, setWalletName) = remember { mutableStateOf("") }
    val (isReceive, setIsReceive) = remember { mutableStateOf(false) }
    val (_, setIsProcessed) = remember { mutableStateOf(false) }
    val (amlReleaseDialog, setAmlReleaseDialog) = remember { mutableStateOf(false) }
    var dollarAmount by remember { mutableStateOf("0.0") }

    LaunchedEffect(transactionEntity) {
        if (transactionEntity != null) {
            if (transactionEntity!!.receiverAddressId != null) {
                setIsReceive(true)
            }

            if (transactionEntity!!.tokenName == "USDT") {
                dollarAmount = decimalFormat.format(
                    (transactionEntity?.amount ?: BigInteger.ONE).toTokenAmount()
                )
            } else {
                val trxToUsdtRate =
                    viewModel.exchangeRatesRepo.getExchangeRateValue(BinanceSymbolEnum.TRX_USDT.symbol)
                dollarAmount = decimalFormat.format(
                    (transactionEntity?.amount ?: BigInteger.ONE).toTokenAmount()
                        .toDouble() * trxToUsdtRate
                )
            }

            if (transactionEntity!!.receiverAddressId != null) {
                viewModel.getAmlFromTransactionId(
                    transactionEntity!!.receiverAddress,
                    transactionEntity!!.txId,
                    tokenName = transactionEntity!!.tokenName
                )
            }

            setIsProcessed(transactionEntity!!.isProcessed)
        }
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            setWalletName(viewModel.getWalletNameById(walletId) ?: "")
        }
    }

    val currentTokenName = TokenName.entries.find {
        if (transactionEntity != null) {
            it.tokenName == transactionEntity!!.tokenName
        } else false
    } ?: TokenName.USDT

    val bottomPadding = sharedPref().getFloat("bottomPadding", 54f)

    Scaffold(
        modifier = Modifier,
        snackbarHost = {
            StackedSnackbarHost(
                hostState = stackedSnackbarHostState,
                modifier = Modifier
                    .padding(8.dp, 90.dp)
            )
        }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painterResource(id = R.drawable.wallet_background),
                    contentScale = ContentScale.FillBounds
                ), verticalArrangement = Arrangement.Bottom
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "TX Details",
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color.White)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    run {
                        IconButton(onClick = { goToBack() }) {
                            Icon(
                                modifier = Modifier.size(34.dp),
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = Color.White
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
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .weight(0.8f)
                    .shadow(7.dp, RoundedCornerShape(16.dp))
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = bottomPadding.dp)
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                ) {

                    Text(
                        text = "Кошелёк",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 10.dp, horizontal = 16.dp)
                                .fillMaxWidth(),
                        ) {
                            Text(text = walletName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    CardAndDropDownMenuForTxDetails(
                        title = "Адрес отправителя",
                        contentText = transactionEntity?.senderAddress,
                        stackedSnackbarHostState = stackedSnackbarHostState,
                        isHashTransaction = false
                    )
                    CardAndDropDownMenuForTxDetails(
                        title = "Адрес получения",
                        contentText = transactionEntity?.receiverAddress,
                        stackedSnackbarHostState = stackedSnackbarHostState,
                        isHashTransaction = false
                    )
                    CardAndDropDownMenuForTxDetails(
                        title = "Хэш транзакции",
                        contentText = transactionEntity?.txId,
                        stackedSnackbarHostState = stackedSnackbarHostState,
                        isHashTransaction = true
                    )

                    Text(
                        text = "Сумма",
                        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 4.dp, top = 12.dp),
                    )
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 10.dp, horizontal = 16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${decimalFormat.format((transactionEntity?.amount ?: BigInteger.ONE).toTokenAmount())} ${currentTokenName.shortName}",
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Text(
                                text = "~${dollarAmount}$",
                                style = MaterialTheme.typography.bodySmall,
                                color = PubAddressDark
                            )
                        }
                    }

                    if (!isReceive) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(top = 0.dp, bottom = 4.dp)
                                .fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 10.dp, horizontal = 16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Комиссия", style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    text = "$commissionBySending USDT",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                            }
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(vertical = 0.dp)
                            .fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 10.dp, horizontal = 16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Дата", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = convertTimestampToDateTime(
                                    transactionEntity?.timestamp ?: 1
                                ),
                                style = MaterialTheme.typography.bodySmall,
                            )

                        }
                    }
                    if (isReceive) {
                        when (val result = amlState) {
                            is AmlResult.Success -> {
                                if (result.response.amlId.isNotEmpty()) {
                                    when {
                                        result.response.riskyScore >= 70.0 -> KnowAMLFeature(
                                            viewModel = viewModel,
                                            amlType = AMLType.HIGH_RISC,
                                            amlState = result.response,
                                            transactionEntity = transactionEntity!!,
                                            stackedSnackbarHostState = stackedSnackbarHostState
                                        )

                                        result.response.riskyScore in 50.0..69.9 -> KnowAMLFeature(
                                            viewModel = viewModel,
                                            amlType = AMLType.MEDIUM_RISC,
                                            amlState = result.response,
                                            transactionEntity = transactionEntity!!,
                                            stackedSnackbarHostState = stackedSnackbarHostState
                                        )

                                        result.response.riskyScore < 50.0 -> KnowAMLFeature(
                                            viewModel = viewModel,
                                            amlType = AMLType.LOW_RISC,
                                            amlState = result.response,
                                            transactionEntity = transactionEntity!!,
                                            stackedSnackbarHostState = stackedSnackbarHostState
                                        )
                                    }
                                } else {
                                    UnknownAMLFeature()
                                }
                            }

                            is AmlResult.Error -> {
                                LaunchedEffect(stackedSnackbarHostState) {
                                    if (result.throwable.message == "ABORTED: Time has not yet passed since the last request") {
                                        stackedSnackbarHostState.showErrorSnackbar(
                                            "Ошибка запроса",
                                            "Запрос на перевыпуск AML разрешен раз в день.",
                                            "Закрыть"
                                        )
                                        viewModel.getAmlFromTransactionId(
                                            transactionEntity!!.receiverAddress,
                                            transactionEntity!!.txId,
                                            transactionEntity!!.tokenName
                                        )
                                    } else if (result.throwable.message == "FAILED_PRECONDITION: This AML was not paid by the client") {
                                        stackedSnackbarHostState.showErrorSnackbar(
                                            "Запрос отклонен",
                                            "Для получения данного AML его необходимо оплатить.",
                                            "Закрыть"
                                        )
                                    } else {
                                        stackedSnackbarHostState.showErrorSnackbar(
                                            "Ошибка запроса",
                                            "Сервер вернул ошибку, пожалуйста, сообщите поддержке и повторите через минуту.",
                                            "Закрыть"
                                        )
                                        Sentry.captureException(
                                            ServerAmlException(
                                                result.throwable.message
                                                    ?: "Пустое сообщение, анализируйте сервер.",
                                                result.throwable
                                            )
                                        )
                                    }
                                }
                                UnknownAMLFeature()
                            }

                            is AmlResult.Empty -> UnknownAMLFeature()
                            is AmlResult.Loading -> UnknownAMLFeature()
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(bottom = 10.dp),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            Button(
                                onClick = {
                                    setAmlReleaseDialog(true)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = GreenColor,
                                    contentColor = BackgroundContainerButtonLight
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Обновить AML",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                }
            }

            if (amlReleaseDialog) {
                AlertDialogWidget(
                    onConfirmation = {
                        viewModel.viewModelScope.launch {
                            withContext(Dispatchers.IO) {
                                setAmlReleaseDialog(false)
                                val (status, message) = viewModel.processedAmlReport(
                                    walletId = walletId,
                                    receiverAddress = transactionEntity!!.receiverAddress,
                                    txId = transactionEntity!!.txId
                                )

                                if (status) {
                                    stackedSnackbarHostState.showSuccessSnackbar(
                                        "Успешное действие",
                                        message,
                                        "Закрыть"
                                    )
                                } else {
                                    stackedSnackbarHostState.showErrorSnackbar(
                                        "Ошибка запроса",
                                        message,
                                        "Закрыть"
                                    )
                                }
                            }
                        }
                    },
                    onDismissRequest = {
                        setAmlReleaseDialog(false)
                    },
                    dialogTitle = "Выпуск AML",
                    dialogText = "Для получения AML необходимо внести плату за его выпуск или перевыпуск в размере ${
                        amlFeeResult?.toBigInteger()?.toTokenAmount() ?: 0
                    } TRX.\n\n" +
                            "Это обязательная процедура, которая обеспечивает актуализацию и соответствие AML требованиям текущего законодательства и стандартов.\n\n" +
                            "Сумма будет списана с центрального адреса которому принадлежит данный адрес!",
                    textConfirmButton = "Оплатить и получить",
                    textDismissButton = "Закрыть",
                )
            }
        }
    }
}

@Composable
fun CardAndDropDownMenuForTxDetails(
    title: String,
    contentText: String?,
    stackedSnackbarHostState: StackedSnakbarHostState,
    isHashTransaction: Boolean,

) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var expandedDropdownMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
        modifier = Modifier.padding(bottom = 4.dp, top = 12.dp),
    )
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp)
                .padding(start = 16.dp, end = 8.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                modifier = Modifier.weight(0.9f),
                text = contentText ?: "...",
                style = MaterialTheme.typography.bodySmall,

                )
            IconButton(
                modifier = Modifier.size(30.dp),
                onClick = {
                    expandedDropdownMenu = !expandedDropdownMenu
                }
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    imageVector = ImageVector.vectorResource(id = R.drawable.icon_more_vert),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )

                DropdownMenu(
                    expanded = expandedDropdownMenu,
                    onDismissRequest = { expandedDropdownMenu = false }
                ) {
                    DropdownMenuItem(
                        onClick = {
                            clipboardManager.setText(
                                AnnotatedString(contentText ?: "")
                            )
                            stackedSnackbarHostState.showSuccessSnackbar(
                                "Успешное действие",
                                "Копирование выполнено успешно",
                                "Закрыть"
                            )
                            expandedDropdownMenu = false
                        },
                        text = { Text("Скопировать") }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = if(isHashTransaction){
                                    "https://tronscan.org/#/transaction/${contentText}".toUri()
                                } else {
                                    "https://tronscan.org/#/address/${contentText}".toUri()
                                }
                            }
                            context.startActivity(intent)
                            expandedDropdownMenu = false
                        },
                        text = { Text("Перейти в Tron Scan", fontWeight = FontWeight.SemiBold) }
                    )
                }
            }
        }
    }
}

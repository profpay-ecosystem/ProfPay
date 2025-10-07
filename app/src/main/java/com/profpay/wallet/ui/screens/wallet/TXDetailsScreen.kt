package com.profpay.wallet.ui.screens.wallet

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.bridge.view_model.dto.TokenName
import com.profpay.wallet.bridge.view_model.wallet.TXDetailsViewModel
import com.profpay.wallet.data.database.entities.wallet.TransactionStatusCode
import com.profpay.wallet.data.database.entities.wallet.getTransactionStatusName
import com.profpay.wallet.data.utils.toBigInteger
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.feature.wallet.tx_details.CardTextForTxDetailsFeature
import com.profpay.wallet.ui.feature.wallet.tx_details.aml.AmlAndButtonGetAmlForTXDetailsFeature
import com.profpay.wallet.ui.shared.sharedPref
import com.profpay.wallet.ui.shared.utils.convertTimestampToDateTime
import com.profpay.wallet.utils.decimalFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rememberStackedSnackbarHostState
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TXDetailsScreen(
    goToBack: () -> Unit,
    viewModel: TXDetailsViewModel = hiltViewModel(),
) {
    val amlState by viewModel.state.collectAsStateWithLifecycle()

    val sharedPref = sharedPref()

    val walletId = sharedPref.getLong("wallet_id", 1)
    val transactionId = sharedPref.getLong("transaction_id", 1)

    val stackedSnackbarHostState = rememberStackedSnackbarHostState()

    val transactionEntity by viewModel.getTransactionLiveDataById(transactionId).observeAsState()

    val amlFeeResult by viewModel.amlFeeResult.collectAsStateWithLifecycle()
    val amlIsPending by viewModel.amlIsPending.collectAsStateWithLifecycle()

    val (walletName, setWalletName) = remember { mutableStateOf("") }
    val (isReceive, setIsReceive) = remember { mutableStateOf(false) }
    val (amlButtonIsEnabled, setAmlButtonIsEnabled) = remember { mutableStateOf(true) }
    val (_, setIsProcessed) = remember { mutableStateOf(false) }
    val (amlReleaseDialog, setAmlReleaseDialog) = remember { mutableStateOf(false) }
    var dollarAmount by remember { mutableStateOf("0.0") }

    LaunchedEffect(transactionEntity?.txId) {
        val tx = transactionEntity ?: return@LaunchedEffect

        viewModel.getAmlIsPendingResult(tx.txId)

        if (tx.receiverAddressId != null) {
            setIsReceive(true)
            viewModel.getAmlFromTransactionId(
                tx.receiverAddress,
                tx.txId,
                tokenName = tx.tokenName,
            )
        }

        if (tx.tokenName == "USDT") {
            dollarAmount = decimalFormat(tx.amount.toTokenAmount())
        } else {
            val rate = viewModel.exchangeRatesRepo.getExchangeRateValue("TRXUSDT")
            dollarAmount = decimalFormat(tx.amount.toTokenAmount() * rate.toBigDecimal())
        }

        setIsProcessed(tx.isProcessed)
    }

    LaunchedEffect(amlIsPending) {
        setAmlButtonIsEnabled(!amlIsPending)
    }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            setWalletName(viewModel.getWalletNameById(walletId) ?: "")
        }
    }

    val currentTokenName =
        TokenName.entries.find {
            if (transactionEntity != null) {
                it.tokenName == transactionEntity!!.tokenName
            } else {
                false
            }
        } ?: TokenName.USDT

    CustomScaffoldWallet(stackedSnackbarHostState = stackedSnackbarHostState) { bottomPadding ->
        CustomTopAppBar(title = "TX Details", goToBack = { goToBack() })
        CustomBottomCard(
            modifier = Modifier
                .weight(0.8f)
                .shadow(7.dp, RoundedCornerShape(16.dp)),
            modifierColumn = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            bottomPadding = bottomPadding,
        ) {
            CardTextForTxDetailsFeature(
                title = "Кошелёк",
                contentText = walletName,
                stackedSnackbarHostState = stackedSnackbarHostState,
                isDropdownMenu = false,
            )
            CardTextForTxDetailsFeature(
                title = "Статус транзакции",
                contentText = getTransactionStatusName(
                    TransactionStatusCode.fromIndex(
                        transactionEntity?.statusCode ?: 3
                    )
                ),
                stackedSnackbarHostState = stackedSnackbarHostState,
                isDropdownMenu = false,
            )
            CardTextForTxDetailsFeature(
                title = "Адрес отправителя",
                contentText = transactionEntity?.senderAddress,
                stackedSnackbarHostState = stackedSnackbarHostState,
            )
            CardTextForTxDetailsFeature(
                title = "Адрес получения",
                contentText = transactionEntity?.receiverAddress,
                stackedSnackbarHostState = stackedSnackbarHostState,
            )
            CardTextForTxDetailsFeature(
                title = "Хэш транзакции",
                contentText = transactionEntity?.txId,
                stackedSnackbarHostState = stackedSnackbarHostState,
                isHashTransaction = true,
            )

            Text(
                text = "Сумма",
                style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 4.dp, top = 12.dp),
            )
            CardTextForTxDetailsFeature(
                title = "${
                    decimalFormat((transactionEntity?.amount ?: BigInteger.ONE).toTokenAmount())
                } ${currentTokenName.shortName}",
                title2 = "~$dollarAmount$",
            )
            if (!isReceive) {
                CardTextForTxDetailsFeature(
                    title = "Комиссия",
                    title2 = "${transactionEntity?.commission?.toTokenAmount()} USDT",
                )
            }
            CardTextForTxDetailsFeature(
                title = "Дата",
                title2 = convertTimestampToDateTime(transactionEntity?.timestamp ?: 1),
            )
            if (amlIsPending) {
                LaunchedEffect(Unit) {
                    stackedSnackbarHostState.showInfoSnackbar(
                        "AML",
                        "Ваш AML находится в обработке, ожидайте.",
                        "Закрыть",
                    )
                }
            }
            if (isReceive) {
                AmlAndButtonGetAmlForTXDetailsFeature(
                    amlState = amlState,
                    viewModel = viewModel,
                    transactionEntity = transactionEntity!!,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                    amlReleaseDialog = amlReleaseDialog,
                    setAmlReleaseDialog = setAmlReleaseDialog,
                    amlButtonIsEnabled = amlButtonIsEnabled,
                    setAmlButtonIsEnabled = setAmlButtonIsEnabled,
                    amlFeeResultText = (amlFeeResult?.toBigInteger()?.toTokenAmount()
                        ?: 0).toString(),
                )

            }
        }
    }
}




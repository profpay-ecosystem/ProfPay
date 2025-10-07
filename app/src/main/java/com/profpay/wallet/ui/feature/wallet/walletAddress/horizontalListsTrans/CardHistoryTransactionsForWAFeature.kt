package com.profpay.wallet.ui.feature.wallet.walletAddress.horizontalListsTrans
import StackedSnakbarHostState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.R
import com.profpay.wallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.database.entities.wallet.TransactionType
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.ui.feature.wallet.tx_details.bottomSheetTransOnGeneralReceipt
import com.profpay.wallet.ui.shared.sharedPref
import kotlinx.coroutines.launch


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
                    append(
                        "Откуда: ${transactionEntity.senderAddress.take(5)}...${
                            transactionEntity.senderAddress.takeLast(
                                5
                            )
                        }\n"
                    )
                    append(
                        "Куда: ${transactionEntity.receiverAddress.take(5)}...${
                            transactionEntity.receiverAddress.takeLast(
                                5
                            )
                        }"
                    )
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
                            Text(
                                text = transactionDetails,
                                style = MaterialTheme.typography.labelLarge
                            )
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

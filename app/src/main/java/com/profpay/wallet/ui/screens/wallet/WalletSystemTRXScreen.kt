package com.profpay.wallet.ui.screens.wallet

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.R
import com.profpay.wallet.bridge.viewmodel.wallet.WalletSystemTRXScreenViewModel
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.components.custom.ActionButtonData
import com.profpay.wallet.ui.components.custom.CustomActionButtonsRow
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomQRCodeAddressBlock
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.feature.wallet.bottomSheetReissueAddress
import com.profpay.wallet.ui.feature.wallet.walletSystemTRX.InfoCardSystemTrxFeature
import com.profpay.wallet.utils.generateQRCode
import kotlinx.coroutines.launch
import rememberStackedSnackbarHostState
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
@Suppress("UNUSED_VARIABLE") // Sonar считает setIsOpenRejectReceiptSheet неиспользуемой переменной.
fun WalletSystemTRXScreen(
    goToBack: () -> Unit,
    goToCentralAddressTxHistory: () -> Unit,
    viewModel: WalletSystemTRXScreenViewModel = hiltViewModel(),
) {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    val context = LocalContext.current

    var address by remember { mutableStateOf("") }
    var balanceTRX by remember { mutableStateOf(BigInteger.ZERO) }

    val centralAddress by viewModel.getCentralAddressLiveData().observeAsState()

    LaunchedEffect(centralAddress) {
        if (centralAddress != null) {
            balanceTRX = centralAddress!!.balance
            address = centralAddress!!.address
        }
    }

    val (_, setIsOpenRejectReceiptSheet) = bottomSheetReissueAddress()

    val stackedSnackbarHostState = rememberStackedSnackbarHostState()

    val textInfo = buildString {
        appendLine("Для удобства и безопасности, в нашем приложении все ваши кошельки связаны с общим TRX-адресом.")
        appendLine("Этот адрес необходим для следующих целей:\n")
        appendLine("1. Активации кошельков — автоматически активирует все ваши кошельки.")
        appendLine("2. Оплаты комиссий — покрывает расходы на смарт-контракты.\n")
        append("Используйте его для всех операций активации и оплаты комиссий. ")
        append("При создании нового кошелька внесите на этот адрес 20 TRX для активации адресов.")
    }

    val actions = remember(address) {
        listOf(
            ActionButtonData(
                iconRes = R.drawable.icon_restart,
                contentDescription = "Restart",
                onClick = { setIsOpenRejectReceiptSheet(true) }
            ),
            ActionButtonData(
                iconRes = R.drawable.icon_copy,
                contentDescription = "Copy address",
                onClick = {
                    scope.launch {
                        clipboard.setClipEntry(
                            ClipData.newPlainText("Wallet address", address).toClipEntry()
                        )
                    }
                    stackedSnackbarHostState.showSuccessSnackbar(
                        "Успешное действие",
                        "Адрес кошелька успешно скопирован.",
                        "Закрыть"
                    )
                }
            ),
            ActionButtonData(
                iconRes = R.drawable.icon_share,
                contentDescription = "Share address",
                onClick = {
                    val extraText =
                        "Мой публичный адрес для получения TRX:\n" +
                            "$address\n\n" +
                            "Данное сообщение отправлено с помощью приложения ProfPay Wallet"

                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, "Пополнение кошелька")
                        putExtra(Intent.EXTRA_TEXT, extraText)
                    }

                    ContextCompat.startActivity(
                        context,
                        Intent.createChooser(intent, "ShareWith"),
                        null
                    )
                }
            ),
            ActionButtonData(
                iconRes = R.drawable.icon_tx_history,
                contentDescription = "Transaction history",
                onClick = { goToCentralAddressTxHistory() }
            )
        )
    }

    CustomScaffoldWallet(stackedSnackbarHostState = stackedSnackbarHostState) { bottomPadding ->
        CustomTopAppBar(title = "System TRX", goToBack = { goToBack() })
        CustomBottomCard(
            modifier = Modifier.weight(0.8f),
            modifierColumn =
                Modifier
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            bottomPadding = bottomPadding,
        ) {
            InfoCardSystemTrxFeature(text = textInfo)
            if (address.isNotEmpty()) {
                CustomQRCodeAddressBlock(
                    qrBitmap = generateQRCode(address),
                    address = address,
                    balanceText = "${balanceTRX.toTokenAmount()} TRX"
                )
                CustomActionButtonsRow(actions = actions)
            }
            Spacer(modifier = Modifier.size(10.dp))
        }
    }
}


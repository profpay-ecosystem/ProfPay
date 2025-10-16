package com.profpay.wallet.ui.screens.wallet

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.R
import com.profpay.wallet.ui.components.custom.ActionButtonData
import com.profpay.wallet.ui.components.custom.CustomActionButtonsRow
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
import com.profpay.wallet.ui.feature.wallet.receiveFromWalletSots.CardForReceiveFromWalletSotsFeature
import com.profpay.wallet.ui.components.custom.CustomQRCodeAddressBlock
import com.profpay.wallet.ui.shared.sharedPref
import com.profpay.wallet.utils.generateQRCode
import kotlinx.coroutines.launch
import rememberStackedSnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceiveFromWalletSotsScreen(goToBack: () -> Unit) {
    val clipboard = LocalClipboard.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val addressForReceive = sharedPref().getString(PrefKeys.ADDRESS_FOR_RECEIVE, "")
    val qrCodeBitmap = generateQRCode(addressForReceive!!)

    val stackedSnackbarHostState = rememberStackedSnackbarHostState()

    val extraText =
        "Мой публичный адрес для получения USDT:\n" +
                "${addressForReceive}\n\n" +
                "Данное сообщение отправлено с помощью приложения ProfPay Wallet"

    val intent = Intent(Intent.ACTION_SEND)
    intent.type = "text/plain"
    intent.putExtra(Intent.EXTRA_SUBJECT, "Пополнение кошелька")
    intent.putExtra(Intent.EXTRA_TEXT, extraText)

    val actionsButtonDataList = listOf(
        ActionButtonData(
            R.drawable.icon_copy,
            "Copy",
            onClick = {
                scope.launch {
                    clipboard.setClipEntry(
                        ClipData.newPlainText("Wallet address", addressForReceive)
                            .toClipEntry()
                    )
                }
                stackedSnackbarHostState.showSuccessSnackbar(
                    "Успешное действие",
                    "Адрес кошелька успешно скопирован.",
                    "Закрыть",
                )
            }
        ),
        ActionButtonData(
            R.drawable.icon_share,
            "Share",
            onClick = {
                ContextCompat.startActivity(
                    context,
                    Intent.createChooser(intent, "ShareWith"),
                    null,
                )
            }
        )
    )

    CustomScaffoldWallet(stackedSnackbarHostState = stackedSnackbarHostState) { bottomPadding ->
        CustomTopAppBar(title = "Receive", goToBack = { goToBack() })
        CustomBottomCard(
            modifier = Modifier.weight(0.8f),
            modifierColumn = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            bottomPadding = bottomPadding,
        ) {
            CardForReceiveFromWalletSotsFeature(title = "Сеть", networkName = "Tron")
            CustomQRCodeAddressBlock(qrBitmap = qrCodeBitmap, address = addressForReceive)
            if (addressForReceive.isNotEmpty()) {
                CustomActionButtonsRow(actionsButtonDataList)
            }
        }
    }
}

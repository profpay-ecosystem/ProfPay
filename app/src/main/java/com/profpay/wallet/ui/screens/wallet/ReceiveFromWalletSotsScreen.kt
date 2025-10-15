package com.profpay.wallet.ui.screens.wallet

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.R
import com.profpay.wallet.ui.components.custom.CustomBottomCard
import com.profpay.wallet.ui.components.custom.CustomScaffoldWallet
import com.profpay.wallet.ui.components.custom.CustomTopAppBar
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
            Row(
                modifier =
                    Modifier
                        .padding(top = 16.dp, start = 6.dp)
                        .fillMaxWidth(),
            ) {
                Text(text = "Сеть", style = MaterialTheme.typography.titleMedium)
            }
            Card(
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(top = 12.dp, bottom = 20.dp),
                elevation = CardDefaults.cardElevation(10.dp),
                colors =
                    CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                    ),
            ) {
                Row(
                    modifier =
                        Modifier
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                            .fillMaxWidth(),
                ) {
                    Text(text = "Tron", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Column(
                modifier =
                    Modifier
                        .padding()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.onPrimary),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                qrCodeBitmap?.asImageBitmap()?.let {
                    Image(
                        bitmap = it,
                        contentDescription = "",
                        modifier = Modifier.size(300.dp),
                    )
                }
                Text(
                    text = "${addressForReceive.dropLast(10)}\n ${
                        addressForReceive.takeLast(
                            10,
                        )
                    }",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 0.dp, end = 0.dp, bottom = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    minLines = 2,
                    textAlign = TextAlign.Center,
                )
            }
            if (addressForReceive.isNotEmpty()) {
                Row(modifier = Modifier.padding(top = 16.dp)) {
                    IconButton(
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
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.onPrimary),
                        modifier = Modifier.size(50.dp),
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Spacer(modifier = Modifier.size(16.dp))
                    IconButton(
                        onClick = {
                            ContextCompat.startActivity(
                                context,
                                Intent.createChooser(intent, "ShareWith"),
                                null,
                            )
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.onPrimary),
                        modifier = Modifier.size(50.dp),
                    ) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            imageVector = ImageVector.vectorResource(id = R.drawable.icon_share),
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}



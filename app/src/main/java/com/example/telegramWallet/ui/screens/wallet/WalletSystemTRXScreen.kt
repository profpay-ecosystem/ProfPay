package com.example.telegramWallet.ui.screens.wallet

import StackedSnackbarHost
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.wallet.WalletSystemTRXScreenViewModel
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.ui.feature.wallet.bottomSheetReissueAddress
import com.example.telegramWallet.ui.shared.sharedPref
import com.example.telegramWallet.utils.generateQRCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import rememberStackedSnackbarHostState
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSystemTRXScreen(
    goToBack: () -> Unit,
    goToCentralAddressTxHistory: () -> Unit,
    viewModel: WalletSystemTRXScreenViewModel = hiltViewModel(),
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    var address by remember { mutableStateOf("empty") }
    var balanceTRX by remember { mutableStateOf(BigInteger.ZERO) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            viewModel.centralAddressRepo.insertIfNotExists()
        }
    }

    val centralAddress by viewModel.getCentralAddressLiveData().observeAsState()

    LaunchedEffect(centralAddress) {
        if (centralAddress != null) {
            address = centralAddress!!.address
            balanceTRX = centralAddress!!.balance
        }
    }

    val (_, setIsOpenRejectReceiptSheet) = bottomSheetReissueAddress()

    val stackedSnackbarHostState = rememberStackedSnackbarHostState()

    val bottomPadding = sharedPref().getFloat("bottomPadding", 54f)

    Scaffold(
        modifier = Modifier,
        snackbarHost = {
            StackedSnackbarHost(
                hostState = stackedSnackbarHostState,
                modifier =
                    Modifier
                        .padding(8.dp, 90.dp),
            )
        },
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
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Wallet System",
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
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 0.dp,
                            ),
                        ).weight(0.8f)
                        .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier = Modifier.padding(bottom = bottomPadding.dp).padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier =
                            Modifier
                                .padding(top = 12.dp, bottom = 20.dp),
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .padding(vertical = 16.dp, horizontal = 16.dp)
                                    .fillMaxWidth(),
                        ) {
                            Text(
                                text =
                                    "Для удобства и безопасности, в нашем приложении все ваши " +
                                        "кошельки связаны с общим TRX-адресом. Этот адрес необходим " +
                                        "для следующих целей:\n" +
                                        "\n" +
                                        "1. Активации кошельков: Автоматически активирует все ваши кошельки.\n" +
                                        "2. Оплаты комиссий: Покрывает расходы на смарт-контракты.\n" +
                                        "\n" +
                                        "Используйте его для всех операций активации и оплаты комиссий, " +
                                        "при создании нового кошелька внесите на этот адрес 20 TRX для активации адресов.",
                                style = MaterialTheme.typography.bodyMedium,
                            )
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
                        if (address != "empty") {
                            val qrCodeBitmap = generateQRCode(address)
                            qrCodeBitmap?.let {
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(290.dp),
                                )
                            } ?: run {
                                println("QR code generation failed for address: $address")
                            }
                        }

                        Text(
                            text = "${address.dropLast(10)}\n ${
                                address.takeLast(
                                    10,
                                )
                            }",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 0.dp, end = 0.dp, bottom = 16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            minLines = 2,
                            textAlign = TextAlign.Center,
                        )

                        Text(
                            text = "Balance ${balanceTRX.toTokenAmount()} TRX",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp),
                        )
                    }
                    if (address.isNotEmpty()) {
                        Row(modifier = Modifier.padding(top = 16.dp)) {
                            IconButton(
                                onClick = {
                                    setIsOpenRejectReceiptSheet(true)
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.onPrimary),
                                modifier = Modifier.size(50.dp),
                            ) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    imageVector = ImageVector.vectorResource(id = R.drawable.icon_restart),
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                            Spacer(modifier = Modifier.fillMaxWidth(0.05f))
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(address))
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
                            Spacer(modifier = Modifier.fillMaxWidth(0.05f))
                            IconButton(
                                onClick = {
                                    val extraText =
                                        "Мой публичный адрес для получения TRX:\n" +
                                            "${address}\n\n" +
                                            "Данное сообщение отправлено с помощью приложения ProfPay Wallet"

                                    val intent = Intent(Intent.ACTION_SEND)
                                    intent.type = "text/plain"
                                    intent.putExtra(Intent.EXTRA_SUBJECT, "Пополнение кошелька")
                                    intent.putExtra(Intent.EXTRA_TEXT, extraText)

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
                            Spacer(modifier = Modifier.fillMaxWidth(0.05f))
                            IconButton(
                                onClick = {
                                    goToCentralAddressTxHistory()
                                },
                                colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.onPrimary),
                                modifier = Modifier.size(50.dp),
                            ) {
                                Icon(
                                    modifier = Modifier.size(24.dp),
                                    painter = painterResource(id = R.drawable.tx_history),
                                    contentDescription = "",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}

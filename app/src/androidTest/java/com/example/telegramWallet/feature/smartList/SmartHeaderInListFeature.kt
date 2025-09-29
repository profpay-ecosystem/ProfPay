package com.example.telegramWallet.new_feature.smartList

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.telegramWallet.AppConstants
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.smart_contract.GetSmartContractViewModel
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.ui.app.theme.LocalFontSize
import com.example.telegramWallet.ui.feature.smartList.bottomSheets.bottomSheetReissueContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

@Composable
fun SmartHeaderInListFeature(
    balance: BigDecimal,
    address: String,
    viewModel: GetSmartContractViewModel,
) {
    val context = LocalContext.current
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var expandedDropdownMenu by remember { mutableStateOf(false) }

    val deployEstimateCommission by viewModel.stateEstimateResourcePrice.collectAsStateWithLifecycle()
    val (commission, setCommission) =
        remember {
            mutableStateOf(BigDecimal.ZERO)
        }
    val (openDeals, setOpenDeals) =
        remember {
            mutableLongStateOf(0)
        }
    val (closedDeals, setClosedDeals) =
        remember {
            mutableLongStateOf(0)
        }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val generalAddress = viewModel.addressRepo.getGeneralAddressByWalletId(1L)
            viewModel.getResourceQuote(
                address = generalAddress,
                energy = AppConstants.SmartContract.PUBLISH_ENERGY_REQUIRED,
                bandwidth = AppConstants.SmartContract.PUBLISH_BANDWIDTH_REQUIRED,
            )
// todo: java.lang.IndexOutOfBoundsException: Index: 0, появилась на строке ниже
            val contractStats =
                viewModel.tron.smartContracts.multiSigRead
                    .getContractStats("", "", address)
            setOpenDeals(contractStats.first.toLong())
            setClosedDeals(contractStats.second.toLong())
        }
    }

    LaunchedEffect(deployEstimateCommission) {
        if (deployEstimateCommission.commission == 0L) return@LaunchedEffect

        setCommission(deployEstimateCommission.commission.toBigInteger().toTokenAmount())
    }

    val (_, setIsOpenCreateContractSheet) = bottomSheetReissueContract(commission = commission, viewModel = viewModel)
    Column(modifier = Modifier.padding(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Баланс контракта",
                        fontSize = LocalFontSize.Large.fS,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .clickable { expandedDropdownMenu = !expandedDropdownMenu },
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
                                text =
                                    "${address.take(4)}..." +
                                        "${address.takeLast(4)} ",
                                fontSize = LocalFontSize.Small.fS,
                                fontWeight = FontWeight.SemiBold,
                            )
                            Icon(
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )

                            DropdownMenu(
                                expanded = expandedDropdownMenu,
                                onDismissRequest = { expandedDropdownMenu = false },
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(address))
                                        expandedDropdownMenu = false
                                    },
                                    text = { Text("Скопировать") },
                                )
                                HorizontalDivider()
                                DropdownMenuItem(
                                    onClick = {
                                        val intent =
                                            Intent(Intent.ACTION_VIEW).apply {
                                                data =
                                                    "https://tronscan.org/#/address/$address".toUri()
                                            }
                                        context.startActivity(intent)
                                        expandedDropdownMenu = false
                                    },
                                    text = {
                                        Text(
                                            "Перейти в Tron Scan",
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "$balance USDT",
                        fontSize = LocalFontSize.Huge.fS,
                        fontWeight = FontWeight.SemiBold,
                        modifier =
                            Modifier
                                .padding(end = 8.dp),
                    )
                }
            }
        }

        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Открытых сделок",
                fontSize = LocalFontSize.Small.fS,
            )
            Text(
                text = "$openDeals",
                fontSize = LocalFontSize.Small.fS,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 0.dp, horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "Закрытых сделок",
                fontSize = LocalFontSize.Small.fS,
            )
            Text(
                text = "$closedDeals",
                fontSize = LocalFontSize.Small.fS,
                fontWeight = FontWeight.SemiBold,
            )
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Card(
                modifier =
                    Modifier
                        .padding(start = 6.dp, end = 6.dp, top = 12.dp)
                        .height(40.dp)
                        .fillMaxWidth()
                        .shadow(7.dp, RoundedCornerShape(10.dp))
                        .clickable {
                            setIsOpenCreateContractSheet(true)
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
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_restart),
                        contentDescription = "",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Перевыпуск контракта",
                        fontSize = LocalFontSize.Small.fS,
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.size(5.dp))
}

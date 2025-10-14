package com.profpay.wallet.ui.feature.smartList.smartCard

import android.content.ClipData
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.profpay.wallet.bridge.view_model.smart_contract.ContractButtonVisibleType
import com.profpay.wallet.bridge.view_model.smart_contract.StatusData
import com.profpay.wallet.data.utils.toBigInteger
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.LocalFontSize
import com.profpay.wallet.ui.app.theme.RedColor
import com.profpay.wallet.ui.app.theme.greenColor
import com.profpay.wallet.ui.app.theme.redColor
import com.profpay.wallet.ui.feature.wallet.HexagonShape
import kotlinx.coroutines.launch
import org.example.protobuf.smart.SmartContractProto
import java.math.BigInteger


@Composable
internal fun SmartCardWidget(
    indexToString: String,
    status: StatusData?,
    oppositeUsername: String?,
    oppositeUserId: Long?,
    clickableDetails: () -> Unit,
    item: SmartContractProto.ContractDealListResponse,
    isBuyerNotDeposited: Boolean,
    isSellerNotPayedExpertFee: Boolean,
    isButtonVisible: ContractButtonVisibleType,
    onClickButtonCancel: () -> Unit,
    onClickButtonAgree: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    var expandedDropdownMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        elevation = CardDefaults.cardElevation(10.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .clip(HexagonShape(true))
                            .border(2.dp, Color(0xFF6A0E8D), HexagonShape(true))
                            .size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = indexToString,
                        fontSize = LocalFontSize.ExtraLarge.fS,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Column(
                    modifier =
                        Modifier
                            .padding(horizontal = 12.dp, 8.dp)
                            .weight(1f),
                ) {
                    Text(
                        text = status?.status ?: "Загрузка...",
                        fontSize = LocalFontSize.Medium.fS,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = "$oppositeUsername, ID №$oppositeUserId",
                        fontSize = LocalFontSize.ExtraSmall.fS,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .clickable { clickableDetails() },
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    "Детали",
                                    fontSize = LocalFontSize.ExtraSmall.fS,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                }
            }

            Row(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .padding(top = 8.dp, bottom = 4.dp)
                        .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$${item.amount.toBigInteger().toTokenAmount()} (+$${
                        (
                                item.dealData.totalExpertCommissions.toBigInteger() /
                                        BigInteger.valueOf(
                                            2,
                                        )
                                ).toTokenAmount()
                    })",
                    fontSize = LocalFontSize.Huge.fS,
                    color = RedColor,
                )
            }
            RowForSmartCardFeature(
                label = "Адрес контракта",
                address = item.smartContractAddress,
                clipboard = clipboard,
            ) {
                DropdownMenu(
                    expanded = expandedDropdownMenu,
                    onDismissRequest = { expandedDropdownMenu = false },
                ) {
                    DropdownMenuItem(
                        onClick = {
                            scope.launch {
                                clipboard.setClipEntry(
                                    ClipData.newPlainText("Wallet address", item.smartContractAddress ?: "").toClipEntry()
                                )
                            }
                        },
                        text = { Text("Скопировать") },
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        onClick = {
                            val intent =
                                Intent(Intent.ACTION_VIEW).apply {
                                    data =
                                        "https://tronscan.org/#/address/${item.smartContractAddress}".toUri()
                                }
                            context.startActivity(intent)
                        },
                        text = {
                            Text(
                                "Перейти в Tron Scan",
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                    )
                }
            }
            RowForSmartCardFeature(
                label = "Получатель:",
                address = item.seller.address,
                clipboard = clipboard,
            )
            RowForSmartCardFeature(
                label = "Отправитель:",
                address = item.buyer.address,
                clipboard = clipboard,
            )
            if (isBuyerNotDeposited || isSellerNotPayedExpertFee) {
                Row(
                    modifier =
                        Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.redColor),
                    ) {
                        Text(
                            modifier =
                                Modifier
                                    .padding(8.dp),
                            text =
                                "Будет списана половина комиссии на адрес смарт-контракта\n" +
                                        "в размере $${((item.dealData.totalExpertCommissions / 2).toBigInteger()).toTokenAmount()}, " +
                                        "вторая часть будет списана у контрагента",
                            fontSize = LocalFontSize.Medium.fS,
                            color = MaterialTheme.colorScheme.redColor,
                        )
                    }
                }
            }
            Row(
                modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ActionButtonForSmartCardFeature(
                    text = status?.rejectButtonName ?: "Загрузка...",
                    enabled = isButtonVisible.cancelVisible,
                    onClick = onClickButtonCancel,
                    modifier = Modifier.weight(0.5f),
                    color = MaterialTheme.colorScheme.redColor,
                )
                ActionButtonForSmartCardFeature(
                    text = status?.completeButtonName ?: "Загрузка...",
                    enabled = isButtonVisible.agreeVisible,
                    onClick = onClickButtonAgree,
                    modifier = Modifier.weight(0.5f),
                    color = MaterialTheme.colorScheme.greenColor,
                )
            }
        }
    }
}

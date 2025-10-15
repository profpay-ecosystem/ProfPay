package com.profpay.wallet.ui.feature.smartList.bottomSheets

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.toClipEntry
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profpay.wallet.R
import com.profpay.wallet.bridge.viewmodel.smartcontract.usecases.isAddressZero
import com.profpay.wallet.data.utils.toBigInteger
import com.profpay.wallet.data.utils.toTokenAmount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.protobuf.smart.SmartContractProto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetDetails(contract: SmartContractProto.ContractDealListResponse): Pair<Boolean, (Boolean) -> Unit> {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true },
        )

    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()

    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }

    if (isOpenSheet) {
        ModalBottomSheet(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { Box(modifier = Modifier) },
            modifier = Modifier.height(IntrinsicSize.Min),
            onDismissRequest = {
                scope.launch {
                    sheetState.hide()
                    delay(400)
                    setIsOpenSheet(false)
                }
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(vertical = 8.dp),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(30.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Start,
                    ) {
                        IconButton(onClick = {
                            setIsOpenSheet(false)
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "",
                                modifier = Modifier.size(27.dp),
                            )
                        }
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Адрес контракта", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text =
                                "${contract.smartContractAddress.take(5)}..." +
                                    "${contract.smartContractAddress.takeLast(5)} ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        IconButton(
                            modifier = Modifier.size(35.dp),
                            onClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipData.newPlainText("Smart address", contract.smartContractAddress).toClipEntry(),
                                    )
                                }
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",
                            )
                        }
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 16.dp, end = 16.dp),
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Сумма", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            text = "${contract.amount.toBigInteger().toTokenAmount()}$",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 16.dp, end = 16.dp),
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Общая комиссия", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Text(
                            text = "${contract.dealData.totalExpertCommissions.toBigInteger().toTokenAmount()}$",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                if (!isAddressZero(contract.disputeResolutionStatus.decisionAdmin)) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 16.dp, end = 16.dp),
                    ) {
                        Row(modifier = Modifier.weight(0.5f)) {
                            Text("Сумма покупателю", fontSize = 16.sp)
                        }
                        Row(
                            modifier = Modifier.weight(0.5f),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Text(
                                text = "${contract.disputeResolutionStatus.amountToBuyer.toBigInteger().toTokenAmount()}$",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, start = 16.dp, end = 16.dp),
                    ) {
                        Row(modifier = Modifier.weight(0.5f)) {
                            Text("Сумма продавцу", fontSize = 16.sp)
                        }
                        Row(
                            modifier = Modifier.weight(0.5f),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Text(
                                text = "${contract.disputeResolutionStatus.amountToSeller.toBigInteger().toTokenAmount()}$",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Адрес получателя", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text =
                                "${contract.seller.address.take(5)}..." +
                                    "${contract.seller.address.takeLast(5)} ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        IconButton(
                            modifier = Modifier.size(35.dp),
                            onClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipData.newPlainText("Wallet address", contract.seller.address).toClipEntry(),
                                    )
                                }
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",
                            )
                        }
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Username получателя", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "@${contract.seller.username}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        IconButton(
                            modifier = Modifier.size(35.dp),
                            onClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipData.newPlainText("Username", contract.seller.username).toClipEntry(),
                                    )
                                }
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",
                            )
                        }
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Telegram ID получателя", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${contract.seller.telegramId}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        IconButton(
                            modifier = Modifier.size(35.dp),
                            onClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipData.newPlainText("Telegram ID", contract.seller.telegramId.toString()).toClipEntry(),
                                    )
                                }
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.size(10.dp))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Адрес отправителя", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text =
                                "${contract.buyer.address.take(5)}..." +
                                    "${contract.buyer.address.takeLast(5)} ",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        IconButton(
                            modifier = Modifier.size(35.dp),
                            onClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipData.newPlainText("Wallet address", contract.buyer.address).toClipEntry(),
                                    )
                                }
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",
                            )
                        }
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Username отправителя", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "@${contract.buyer.username}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        IconButton(
                            modifier = Modifier.size(35.dp),
                            onClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipData.newPlainText("Username", contract.buyer.username).toClipEntry(),
                                    )
                                }
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",
                            )
                        }
                    }
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 0.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Telegram ID отправителя", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "${contract.buyer.telegramId}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                        IconButton(
                            modifier = Modifier.size(35.dp),
                            onClick = {
                                scope.launch {
                                    clipboard.setClipEntry(
                                        ClipData.newPlainText("Telegram ID", contract.buyer.telegramId.toString()).toClipEntry(),
                                    )
                                }
                            },
                        ) {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",
                            )
                        }
                    }
                }
            }
        }
    }
    return isOpenSheet to { setIsOpenSheet(it) }
}

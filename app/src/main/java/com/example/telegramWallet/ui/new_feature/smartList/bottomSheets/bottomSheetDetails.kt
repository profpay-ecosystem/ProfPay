package com.example.telegramWallet.ui.new_feature.smartList.bottomSheets

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
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.smart_contract.GetSmartContractViewModel
import com.example.telegramWallet.bridge.view_model.smart_contract.usecases.isAddressZero
import com.example.telegramWallet.data.utils.toBigInteger
import com.example.telegramWallet.data.utils.toTokenAmount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.protobuf.smart.SmartContractProto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetDetails(
    contract: SmartContractProto.ContractDealListResponse,
    viewModel: GetSmartContractViewModel
): Pair<Boolean, (Boolean) -> Unit> {

    val coroutineScope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }

    if (isOpenSheet) {
        ModalBottomSheet(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { Box(modifier = Modifier) },
            modifier = Modifier.height(IntrinsicSize.Min),
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    delay(400)
                    setIsOpenSheet(false)
                }
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp),
                    horizontalArrangement = Arrangement.End
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
                                modifier = Modifier.size(27.dp)
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Адрес контракта", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${contract.smartContractAddress.take(5)}..." +
                                    "${contract.smartContractAddress.takeLast(5)} ",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            modifier = Modifier.size(35.dp),
                            onClick = {
                                clipboardManager.setText(AnnotatedString(contract.smartContractAddress))
                            }) {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",
                                )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Сумма", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${contract.amount.toBigInteger().toTokenAmount()}$",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                         )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 16.dp, end = 16.dp)
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Общая комиссия", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "${contract.dealData.totalExpertCommissions.toBigInteger().toTokenAmount()}$",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                if (!isAddressZero(contract.disputeResolutionStatus.decisionAdmin)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Row(modifier = Modifier.weight(0.5f)) {
                            Text("Сумма покупателю", fontSize = 16.sp)
                        }
                        Row(
                            modifier = Modifier.weight(0.5f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${contract.disputeResolutionStatus.amountToBuyer.toBigInteger().toTokenAmount()}$",
                                fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp, start = 16.dp, end = 16.dp)
                    ) {
                        Row(modifier = Modifier.weight(0.5f)) {
                            Text("Сумма продавцу", fontSize = 16.sp)
                        }
                        Row(
                            modifier = Modifier.weight(0.5f),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = "${contract.disputeResolutionStatus.amountToSeller.toBigInteger().toTokenAmount()}$",
                                fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.size(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Адрес получателя", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${contract.seller.address.take(5)}..." +
                                    "${contract.seller.address.takeLast(5)} ",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                        )
                        IconButton(modifier = Modifier.size(35.dp),
                            onClick = { clipboardManager.setText(AnnotatedString(contract.seller.address)) }) {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",

                                )
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Username получателя", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "@${contract.seller.username}",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                        )
                        IconButton(modifier = Modifier.size(35.dp),
                            onClick = {
                                clipboardManager.setText(AnnotatedString("@${contract.seller.username}"))
                            }) {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",

                                )

                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Telegram ID получателя", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${contract.seller.telegramId}",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                        )
                        IconButton(modifier = Modifier.size(35.dp),
                            onClick = {
                                clipboardManager.setText(AnnotatedString(contract.seller.telegramId.toString()))
                            }) {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Адрес отправителя", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${contract.buyer.address.take(5)}..." +
                                    "${contract.buyer.address.takeLast(5)} ",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                        )
                        IconButton(modifier = Modifier.size(35.dp),
                            onClick = {
                                clipboardManager.setText(AnnotatedString(contract.buyer.address))
                            }) {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",

                                )

                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Username отправителя", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "@${contract.buyer.username}",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                        )
                        IconButton(modifier = Modifier.size(35.dp),
                            onClick = {
                                clipboardManager.setText(AnnotatedString("@${contract.buyer.username}"))
                            }) {
                            Icon(
                                modifier = Modifier.padding(4.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",

                                )

                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 0.dp, start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(modifier = Modifier.weight(0.5f)) {
                        Text("Telegram ID отправителя", fontSize = 16.sp)
                    }
                    Row(
                        modifier = Modifier.weight(0.5f),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${contract.buyer.telegramId}",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                        )
                        IconButton(modifier = Modifier.size(35.dp),
                            onClick = {
                                clipboardManager.setText(AnnotatedString(contract.buyer.telegramId.toString()))
                            }) {
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
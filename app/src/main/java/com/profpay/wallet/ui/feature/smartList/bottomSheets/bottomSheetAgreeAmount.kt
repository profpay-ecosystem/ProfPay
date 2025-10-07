package com.profpay.wallet.ui.feature.smartList.bottomSheets

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.bridge.view_model.smart_contract.GetSmartContractViewModel
import com.profpay.wallet.data.utils.toBigInteger
import com.profpay.wallet.data.utils.toSunAmount
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.BackgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.app.theme.PubAddressDark
import com.profpay.wallet.ui.app.theme.RedColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.example.protobuf.smart.SmartContractProto
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetAgreeAmount(
    contract: SmartContractProto.ContractDealListResponse,
    viewModel: GetSmartContractViewModel,
): Pair<Boolean, (Boolean) -> Unit> {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true },
        )
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }

    var amountSeller by remember { mutableStateOf("") }
    var amountBuyer by remember { mutableStateOf("") }

    if (isOpenSheet) {
        ModalBottomSheet(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { Box(modifier = Modifier) },
            modifier = Modifier.height(IntrinsicSize.Min),
            onDismissRequest = {
                keyboardController?.hide()
                coroutineScope.launch {
                    sheetState.hide()
                    delay(400)
                    setIsOpenSheet(false)
                }
            },
            sheetState = sheetState,
//            windowInsets = WindowInsets(bottom = 110.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
//              Row 1
                Row(
                    modifier = Modifier.padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(fraction = 0.85f),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Spacer(modifier = Modifier.size(40.dp))
                        Text(
                            text = "Введите сумму",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.Top,
                    ) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                delay(400)
                                setIsOpenSheet(false)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "",
                                modifier = Modifier.size(27.dp),
                            )
                        }
                    }
                }
//              Row 2
                Row(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Сумма продавцу",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
//              Row 3
                Row(
                    modifier =
                        Modifier
                            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier,
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        TextField(
                            value = amountSeller,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier =
                                Modifier
                                    .height(50.dp)
                                    .fillMaxWidth(),
                            placeholder = { Text(text = "0") },
                            shape = MaterialTheme.shapes.small.copy(),
                            onValueChange = { amount ->
                                val sellerValue = amount.toBigDecimalOrNull()?.toSunAmount() ?: BigInteger.ZERO
                                val buyerValue = amountBuyer.toBigDecimalOrNull()?.toSunAmount() ?: BigInteger.ZERO

                                if ((sellerValue + buyerValue > contract.amount.toBigInteger()) && amount.isNotEmpty()) {
                                    return@TextField
                                }
                                amountSeller = amount
                            },
                            trailingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    Text(text = "USDT", fontWeight = FontWeight.SemiBold)
                                    IconButton(onClick = {
                                        amountSeller =
                                            contract.amount
                                                .toBigInteger()
                                                .toTokenAmount()
                                                .toString()
                                    }) {
                                        Text(
                                            text = "MAX",
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }
                                }
                            },
                            colors =
                                TextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = PubAddressDark,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.onBackground,
                                    selectionColors =
                                        TextSelectionColors(
                                            handleColor = MaterialTheme.colorScheme.onBackground,
                                            backgroundColor = Color.Transparent,
                                        ),
                                ),
                        )
                    }
                }
//              Row 4
                Row(
                    modifier = Modifier.padding(top = 24.dp, start = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Сумма покупателю",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
//              Row 5
                Row(
                    modifier =
                        Modifier
                            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier,
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        TextField(
                            value = amountBuyer,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier =
                                Modifier
                                    .height(60.dp)
                                    .fillMaxWidth(),
                            placeholder = { Text(text = "0") },
                            shape = MaterialTheme.shapes.small.copy(),
                            onValueChange = { amount ->
                                val sellerValue =
                                    amountSeller.toBigDecimalOrNull()?.toSunAmount() ?: BigInteger.ZERO
                                val buyerValue = amount.toBigDecimalOrNull()?.toSunAmount() ?: BigInteger.ZERO

                                if ((sellerValue + buyerValue > contract.amount.toBigInteger()) && amount.isNotEmpty()) {
                                    return@TextField
                                }
                                amountBuyer = amount
                            },
                            trailingIcon = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    Text(text = "USDT", fontWeight = FontWeight.SemiBold)
                                    IconButton(onClick = {
                                        amountBuyer =
                                            contract.amount
                                                .toBigInteger()
                                                .toTokenAmount()
                                                .toString()
                                    }) {
                                        Text(
                                            text = "MAX",
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }
                                }
                            },
                            colors =
                                TextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = PubAddressDark,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.onBackground,
                                    selectionColors =
                                        TextSelectionColors(
                                            handleColor = MaterialTheme.colorScheme.onBackground,
                                            backgroundColor = Color.Transparent,
                                        ),
                                ),
                        )
                    }
                }
//              Row 6
                Row(
                    modifier =
                        Modifier
                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RedColor),
                    ) {
                        Text(
                            modifier =
                                Modifier
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                            text =
                                "После отправки введенных Вами значений, \n" +
                                    "будет необходимо подтверждение от участников, экспертов.",
                            color = RedColor,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.viewModelScope.launch {
                            val sellerValue = amountSeller.toBigDecimalOrNull()?.toSunAmount() ?: BigInteger.ZERO
                            val buyerValue = amountBuyer.toBigDecimalOrNull()?.toSunAmount() ?: BigInteger.ZERO

                            if ((sellerValue + buyerValue) == contract.amount.toBigInteger()) {
//                                viewModel.expertSetDecision(
//                                    deal = contract,
//                                    sellerValue = sellerValue,
//                                    buyerValue = buyerValue,
//                                )
                            }
                        }
                    },
                    modifier =
                        Modifier
                            .padding(vertical = 4.dp, horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = GreenColor,
                            contentColor = BackgroundContainerButtonLight,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Отправить на рассмотрение",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
    return isOpenSheet to { setIsOpenSheet(it) }
}

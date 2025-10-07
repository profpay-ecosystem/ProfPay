package com.profpay.wallet.ui.feature.wallet.tx_details.bottomSheet

import StackedSnakbarHostState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.bridge.view_model.dto.transfer.TransferResult
import com.profpay.wallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.flow_db.repo.EstimateCommissionResult
import com.profpay.wallet.data.utils.toBigInteger
import com.profpay.wallet.data.utils.toSunAmount
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.BackgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.app.theme.PubAddressDark
import com.profpay.wallet.ui.feature.wallet.send.bottomsheet.ContentBottomSheetTransferProcessing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.protobuf.transfer.TransferProto
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetRejectReceipt(
    viewModel: WalletAddressViewModel,
    addressWithTokens: AddressWithTokens?,
    snackbar: StackedSnakbarHostState,
    tokenName: String,
): Pair<Boolean, (Boolean) -> Unit> {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true },
        )

    val clipboardManager = LocalClipboardManager.current

    val keyboardController = LocalSoftwareKeyboardController.current

    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }
    val (isConfirmTransaction, setIsConfirmTransaction) = remember { mutableStateOf(false) }

    var valueAmount by remember { mutableStateOf("0.0") }
    val (commissionOnTransaction, setCommissionOnTransaction) = remember { mutableStateOf(BigDecimal.ZERO) }
    val commissionState by viewModel.stateCommission.collectAsStateWithLifecycle()

    val (commissionResult, setCommissionResult) = remember { mutableStateOf(TransferProto.EstimateCommissionResponse.getDefaultInstance()) }

    var isButtonEnabled by remember { mutableStateOf(false) }

    if (isOpenSheet) {
        var addressSending by remember { mutableStateOf(addressWithTokens!!.addressEntity.address) }
        val tokenEntity =
            addressWithTokens!!
                .tokens
                .stream()
                .filter { it.token.tokenName == tokenName }
                ?.findFirst()
                ?.orElse(null)

        LaunchedEffect(Unit, valueAmount, addressSending) {
            viewModel.requestCommission(addressWithTokens, tokenName, valueAmount, addressSending)
        }

        LaunchedEffect(commissionState) {
            when (commissionState) {
                is EstimateCommissionResult.Loading -> {}
                is EstimateCommissionResult.Success -> {
                    val commission = (commissionState as EstimateCommissionResult.Success).response.commission
                    val commissionResult = (commissionState as EstimateCommissionResult.Success).response
                    isButtonEnabled = true

                    if (valueAmount == "0.0") {
                        valueAmount = tokenEntity?.balanceWithoutFrozen?.toTokenAmount().toString()
                    }
                    setCommissionOnTransaction(commission.toBigInteger().toTokenAmount())
                    setCommissionResult(commissionResult)
                }
                is EstimateCommissionResult.Error -> {}
                is EstimateCommissionResult.Empty -> {}
            }
        }

        ModalBottomSheet(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { Box(modifier = Modifier) },
            modifier = Modifier.height(IntrinsicSize.Min),
            onDismissRequest = {
                keyboardController?.hide()
                setIsOpenSheet(false)
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                if (!isConfirmTransaction) {
                    Row(
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Отправить",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }

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
                                value = addressSending,
                                modifier =
                                    Modifier
                                        .fillMaxWidth(),
                                placeholder = { Text(text = "Введите адрес") },
                                shape = MaterialTheme.shapes.small.copy(),
                                onValueChange = {
                                    addressSending = it
                                },
                                trailingIcon = {
                                    Card(
                                        shape = RoundedCornerShape(5.dp),
                                        modifier = Modifier.padding(end = 8.dp),
                                        elevation = CardDefaults.cardElevation(7.dp),
                                        onClick = {
                                            val clipData = clipboardManager.getText()
                                            if (clipData != null) {
                                                addressSending = clipData.toString()
                                            }
                                        },
                                    ) {
                                        Text(
                                            "Paste",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier =
                                                Modifier.padding(
                                                    horizontal = 12.dp,
                                                    vertical = 8.dp,
                                                ),
                                        )
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
                                value = valueAmount,
                                modifier =
                                    Modifier
                                        .fillMaxWidth(),
                                placeholder = { Text(text = "Введите сумму") },
                                shape = MaterialTheme.shapes.small.copy(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                onValueChange = {
                                    valueAmount = it
                                },
                                trailingIcon = {},
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

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp, horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .padding(18.dp)
                                    .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = "Комиссия:", fontWeight = FontWeight.SemiBold)
                            Row {
                                Text(text = "$commissionOnTransaction ")
                                Text(text = "TRX", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier =
                            Modifier
                                .padding(horizontal = 16.dp),
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors =
                            CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                            ),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .padding(vertical = 18.dp, horizontal = 16.dp)
                                    .fillMaxWidth(),
                        ) {
                            Text(
                                text =
                                    "Мы взымаем комиссию в TRX, которая рассчитывается исходя из количества полученных AML отчетов и числа оплаченных услуг, " +
                                        "связанных с проверкой по AML. " +
                                        "Размер комиссии напрямую зависит от объема предоставленных отчетов и оплаченных проверок на соответствие требованиям по борьбе с отмыванием денег (AML).",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }

                    Button(
                        enabled = isButtonEnabled,
                        onClick = {
                            if (viewModel.tron.addressUtilities.isValidTronAddress(addressSending)) {
                                isButtonEnabled = false // Отключаем кнопку
                                setIsConfirmTransaction(true)
                                viewModel.viewModelScope.launch {
                                    val result =
                                        withContext(Dispatchers.IO) {
                                            viewModel.rejectTransaction(
                                                toAddress = addressSending,
                                                addressWithTokens = addressWithTokens,
                                                amount = valueAmount.toBigDecimal().toSunAmount(),
                                                commission = commissionOnTransaction.toSunAmount(),
                                                tokenEntity = tokenEntity,
                                                commissionResult = commissionResult,
                                            )
                                        }

                                    when (result) {
                                        is TransferResult.Success ->
                                            snackbar.showSuccessSnackbar(
                                                "Успешное действие",
                                                "Успешно отправлено ${valueAmount.toBigInteger()} $tokenName",
                                                "Закрыть",
                                            )
                                        is TransferResult.Failure ->
                                            snackbar.showErrorSnackbar(
                                                "Перевод валюты невозможен",
                                                result.error.message,
                                                "Закрыть",
                                            )
                                    }

                                    setIsOpenSheet(false)
                                    isButtonEnabled = true
                                    setIsConfirmTransaction(false)
                                }
                            }
                        },
                        modifier =
                            Modifier
                                .padding(vertical = 24.dp, horizontal = 16.dp)
                                .fillMaxWidth()
                                .height(50.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = GreenColor,
                                contentColor = BackgroundContainerButtonLight,
                            ),
                        shape = RoundedCornerShape(12.dp),
                    ) {
                        Text(
                            text = "Отправить",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                } else {
                    ContentBottomSheetTransferProcessing(onClick = {
                    })
                }
            }
        }
    }
    return isOpenSheet to { setIsOpenSheet(it) }
}

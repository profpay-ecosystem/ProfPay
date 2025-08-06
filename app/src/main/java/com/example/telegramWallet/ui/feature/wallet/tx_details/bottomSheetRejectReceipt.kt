package com.example.telegramWallet.ui.feature.wallet.tx_details

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
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.telegramWallet.bridge.view_model.dto.transfer.TransferResult
import com.example.telegramWallet.bridge.view_model.wallet.walletSot.WalletAddressViewModel
import com.example.telegramWallet.data.database.models.AddressWithTokens
import com.example.telegramWallet.data.flow_db.repo.EstimateCommissionResult
import com.example.telegramWallet.data.utils.toSunAmount
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.ui.app.theme.BackgroundContainerButtonLight
import com.example.telegramWallet.ui.app.theme.GreenColor
import com.example.telegramWallet.ui.app.theme.PubAddressDark
import com.example.telegramWallet.ui.screens.wallet.ContentBottomSheetTransferProcessing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetRejectReceipt(
    viewModel: WalletAddressViewModel,
    addressWithTokens: AddressWithTokens?,
    snackbar: StackedSnakbarHostState,
    tokenName: String
): Pair<Boolean, (Boolean) -> Unit> {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { true }
    )

    val clipboardManager = LocalClipboardManager.current

    val keyboardController = LocalSoftwareKeyboardController.current

    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }
    val (isConfirmTransaction, setIsConfirmTransaction) = remember { mutableStateOf(false) }

    var valueAmount by remember { mutableStateOf("0.0") }
    val (commissionOnTransaction, setCommissionOnTransaction) = remember { mutableStateOf(BigDecimal.ZERO) }
    val commissionState by viewModel.stateCommission.collectAsStateWithLifecycle()

    var isButtonEnabled by remember { mutableStateOf(false) }

    if (isOpenSheet) {
        var addressSending by remember { mutableStateOf(addressWithTokens!!.addressEntity.address) }
        val tokenEntity =
            addressWithTokens!!.tokens.stream().filter { it.token.tokenName == tokenName }?.findFirst()
                ?.orElse(null)

        LaunchedEffect(Unit, valueAmount, addressSending) {
            val addressEntity = withContext(Dispatchers.IO) {
                viewModel.addressRepo.getAddressEntityByAddress(addressWithTokens.addressEntity.address)
            }

            if (addressEntity == null || valueAmount.isEmpty() || !viewModel.tron.addressUtilities.isValidTronAddress(addressSending)) return@LaunchedEffect

            try {
                val requiredEnergy = withContext(Dispatchers.IO) {
                    viewModel.tron.transactions.estimateEnergy(
                        fromAddress = addressEntity.address,
                        toAddress = addressSending,
                        privateKey = addressEntity.privateKey,
                        amount = valueAmount.toBigDecimal().toSunAmount()
                    )
                }
                val requiredBandwidth = withContext(Dispatchers.IO) {
                    viewModel.tron.transactions.estimateBandwidth(
                        fromAddress = addressEntity.address,
                        toAddress = addressSending,
                        privateKey = addressEntity.privateKey,
                        amount = valueAmount.toBigDecimal().toSunAmount()
                    )
                }

                withContext(Dispatchers.IO) {
                    val hasEnoughBandwidth =
                        viewModel.tron.accounts.hasEnoughBandwidth(
                            addressEntity.address,
                            requiredBandwidth.bandwidth
                        )
                    viewModel.estimateCommission(
                        address = addressWithTokens.addressEntity.address,
                        bandwidth = if (hasEnoughBandwidth) 0 else requiredBandwidth.bandwidth,
                        energy = if (tokenName == "TRX") 0 else requiredEnergy.energy
                    )
                }
            } catch (e: NumberFormatException) { return@LaunchedEffect }
        }

        LaunchedEffect(commissionState) {
            when (commissionState) {
                is EstimateCommissionResult.Loading -> {}
                is EstimateCommissionResult.Success -> {
                    val commission = (commissionState as EstimateCommissionResult.Success).response.commission
                    isButtonEnabled = true

                    if (valueAmount == "0.0") {
                        valueAmount = tokenEntity?.balanceWithoutFrozen?.toTokenAmount().toString()
                    }
                    setCommissionOnTransaction(commission.toBigDecimal())
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Отправить",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier,
                            elevation = CardDefaults.cardElevation(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            TextField(
                                value = addressSending,
                                modifier = Modifier
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
                                        }
                                    ) {
                                        Text(
                                            "Paste",
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 8.dp
                                            ),
                                        )
                                    }},
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = PubAddressDark,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.onBackground,
                                    selectionColors = TextSelectionColors(
                                        handleColor = MaterialTheme.colorScheme.onBackground,
                                        backgroundColor = Color.Transparent
                                    )
                                )
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier,
                            elevation = CardDefaults.cardElevation(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            TextField(
                                value = valueAmount,
                                modifier = Modifier
                                    .fillMaxWidth(),
                                placeholder = { Text(text = "Введите сумму") },
                                shape = MaterialTheme.shapes.small.copy(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                onValueChange = {
                                    valueAmount = it
                                },
                                trailingIcon = {},
                                colors = TextFieldDefaults.colors(
                                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                                    unfocusedTextColor = PubAddressDark,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    cursorColor = MaterialTheme.colorScheme.onBackground,
                                    selectionColors = TextSelectionColors(
                                        handleColor = MaterialTheme.colorScheme.onBackground,
                                        backgroundColor = Color.Transparent
                                    )
                                )
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding( vertical = 8.dp, horizontal = 16.dp,),
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(18.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
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
                        modifier = Modifier
                            .padding( horizontal = 16.dp,),
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 18.dp, horizontal = 16.dp)
                                .fillMaxWidth(),
                        ) {
                            Text(
                                text = "Мы взымаем комиссию в TRX, которая рассчитывается исходя из количества полученных AML отчетов и числа оплаченных услуг, " +
                                        "связанных с проверкой по AML. " +
                                        "Размер комиссии напрямую зависит от объема предоставленных отчетов и оплаченных проверок на соответствие требованиям по борьбе с отмыванием денег (AML).",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Button(
                        enabled = isButtonEnabled,
                        onClick = {
                            if (viewModel.tron.addressUtilities.isValidTronAddress(addressSending)) {
                                isButtonEnabled = false // Отключаем кнопку
                                viewModel.viewModelScope.launch {
                                    val result = withContext(Dispatchers.IO) {
                                        viewModel.rejectTransaction(
                                            toAddress = addressSending,
                                            addressWithTokens = addressWithTokens,
                                            amount = valueAmount.toBigDecimal().toSunAmount(),
                                            commission = commissionOnTransaction.toSunAmount(),
                                            tokenName = tokenName,
                                            tokenEntity = tokenEntity
                                        )
                                    }

                                    when (result) {
                                        is TransferResult.Success -> snackbar.showSuccessSnackbar(
                                            "Успешное действие",
                                            "Успешно отправлено ${valueAmount.toBigInteger().toTokenAmount()} $tokenName",
                                            "Закрыть",
                                        )
                                        is TransferResult.Failure -> snackbar.showErrorSnackbar(
                                            "Перевод валюты невозможен",
                                            result.error.message,
                                            "Закрыть",
                                        )
                                    }

                                    setIsOpenSheet(false)
                                    isButtonEnabled = true
                                }
                            }
                        },
                        modifier = Modifier
                            .padding(vertical = 24.dp, horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(50.dp), colors = ButtonDefaults.buttonColors(
                            containerColor = GreenColor,
                            contentColor = BackgroundContainerButtonLight
                        ), shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Отправить",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
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
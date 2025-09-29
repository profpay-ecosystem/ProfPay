package com.profpay.wallet.ui.feature.wallet.tx_details

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
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
import com.profpay.wallet.ui.screens.wallet.ContentBottomSheetTransferProcessing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.protobuf.transfer.TransferProto
import java.math.BigDecimal
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetTransOnGeneralReceipt(
    viewModel: WalletAddressViewModel,
    addressWithTokens: AddressWithTokens?,
    snackbar: StackedSnakbarHostState,
    tokenName: String,
    walletId: Long,
    balance: BigInteger? = null,
): Pair<Boolean, (Boolean) -> Unit> {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true },
        )

    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }
    val (isConfirmTransaction, setIsConfirmTransaction) = remember { mutableStateOf(false) }

    var isButtonEnabled by remember { mutableStateOf(false) }
    val commissionState by viewModel.stateCommission.collectAsStateWithLifecycle()

    val (commissionOnTransaction, setCommissionOnTransaction) = remember { mutableStateOf(BigDecimal.ZERO) }
    val (commissionResult, setCommissionResult) = remember { mutableStateOf(TransferProto.EstimateCommissionResponse.getDefaultInstance()) }
    val (generalAddressActivatedCommission, setGeneralAddressActivatedCommission) = remember { mutableStateOf(BigInteger.ZERO) }
    val (isGeneralAddressNotActivatedVisible, setIsGeneralAddressNotActivatedVisible) = remember { mutableStateOf(false) }

    if (isOpenSheet) {
        val tokenEntity =
            addressWithTokens!!
                .tokens
                .stream()
                .filter { it.token.tokenName == tokenName }
                ?.findFirst()
                ?.orElse(null)

        LaunchedEffect(Unit) {
            val address =
                withContext(Dispatchers.IO) {
                    viewModel.addressRepo.getAddressEntityByAddress(addressWithTokens.addressEntity.address)
                }
            val generalAddress =
                withContext(Dispatchers.IO) {
                    viewModel.addressRepo.getGeneralAddressByWalletId(walletId)
                }

            if (address == null) return@LaunchedEffect

            val requiredEnergy =
                withContext(Dispatchers.IO) {
                    viewModel.tron.transactions.estimateEnergy(
                        fromAddress = address.address,
                        toAddress = generalAddress,
                        privateKey = address.privateKey,
                        amount = balance ?: tokenEntity?.balanceWithoutFrozen!!,
                    )
                }
            val requiredBandwidth =
                withContext(Dispatchers.IO) {
                    viewModel.tron.transactions.estimateBandwidth(
                        fromAddress = address.address,
                        toAddress = generalAddress,
                        privateKey = address.privateKey,
                        amount = balance ?: tokenEntity?.balanceWithoutFrozen!!,
                    )
                }

            withContext(Dispatchers.IO) {
                if (!viewModel.tron.addressUtilities.isAddressActivated(generalAddress)) {
                    val commission = viewModel.tron.addressUtilities.getCreateNewAccountFeeInSystemContract()
                    setIsGeneralAddressNotActivatedVisible(true)
                    setGeneralAddressActivatedCommission(commission)
                }

                viewModel.estimateCommission(
                    address = addressWithTokens.addressEntity.address,
                    bandwidth = requiredBandwidth.bandwidth,
                    energy = requiredEnergy.energy,
                )
            }
        }

        LaunchedEffect(commissionState) {
            when (commissionState) {
                is EstimateCommissionResult.Loading -> {}
                is EstimateCommissionResult.Success -> {
                    val commission = (commissionState as EstimateCommissionResult.Success).response.commission
                    val commissionResult = (commissionState as EstimateCommissionResult.Success).response

                    isButtonEnabled = true
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
                setIsOpenSheet(false)
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {
                if (!isConfirmTransaction) {
                    Row(
                        modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 16.dp),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Перевод на Главный",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
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

                    if (isGeneralAddressNotActivatedVisible) {
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
                                Text(text = "Активация адреса:", fontWeight = FontWeight.SemiBold, color = Color.Red)
                                Row {
                                    Text(text = "${generalAddressActivatedCommission.toTokenAmount()} ")
                                    Text(text = "TRX", fontWeight = FontWeight.SemiBold)
                                }
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
                                    "Мы списываем комиссию в TRX, которая рассчитывается исходя из количества полученных AML отчетов и числа оплаченных услуг, " +
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
                            isButtonEnabled = false // Отключаем кнопку
                            setIsConfirmTransaction(true)
                            viewModel.viewModelScope.launch {
                                val result =
                                    withContext(Dispatchers.IO) {
                                        viewModel.acceptTransaction(
                                            addressWithTokens = addressWithTokens,
                                            commission = commissionOnTransaction.toSunAmount(),
                                            walletId = walletId,
                                            tokenEntity = tokenEntity,
                                            amount = balance ?: tokenEntity?.balanceWithoutFrozen!!,
                                            commissionResult = commissionResult,
                                        )
                                    }

                                when (result) {
                                    is TransferResult.Success ->
                                        snackbar.showSuccessSnackbar(
                                            "Успешное действие",
                                            "Успешно отправлено ${(balance ?: tokenEntity?.balanceWithoutFrozen!!).toTokenAmount()} $tokenName",
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
                            text = "Подтвердить",
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

package com.profpay.wallet.ui.feature.wallet.tx_details.bottomSheet

import StackedSnakbarHostState
import android.app.Activity
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.bridge.view_model.wallet.walletSot.GeneralTransactionViewModel
import com.profpay.wallet.bridge.view_model.wallet.walletSot.TransferUiEvent
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.repository.flow.EstimateCommissionResult
import com.profpay.wallet.data.utils.toBigInteger
import com.profpay.wallet.data.utils.toSunAmount
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.BackgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.feature.wallet.send.bottomsheet.ContentBottomSheetTransferProcessing
import org.example.protobuf.transfer.TransferProto
import java.math.BigDecimal
import java.math.BigInteger

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetTransOnGeneralReceipt(
    viewModel: GeneralTransactionViewModel = hiltViewModel(),
    addressWithTokens: AddressWithTokens?,
    snackbar: StackedSnakbarHostState,
    tokenName: String,
    walletId: Long,
    balance: BigInteger? = null,
): Pair<Boolean, (Boolean) -> Unit> {
    val context = LocalContext.current

    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true },
        )

    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }
    val (isConfirmTransaction, setIsConfirmTransaction) = remember { mutableStateOf(false) }

    var isButtonEnabled by remember { mutableStateOf(false) }
    val commissionState by viewModel.stateCommission.collectAsStateWithLifecycle()
    val uiEvent by viewModel.uiEventTransfer.collectAsStateWithLifecycle(null)

    val (commissionOnTransaction, setCommissionOnTransaction) = remember { mutableStateOf(BigDecimal.ZERO) }
    val (commissionResult, setCommissionResult) = remember { mutableStateOf(TransferProto.EstimateCommissionResponse.getDefaultInstance()) }

    fun resetUI() {
        setIsOpenSheet(false)
        setIsConfirmTransaction(false)
        isButtonEnabled = true
    }

    if (isOpenSheet) {
        val tokenEntity =
            addressWithTokens!!
                .tokens
                .stream()
                .filter { it.token.tokenName == tokenName }
                ?.findFirst()
                ?.orElse(null)

        LaunchedEffect(Unit) {
            viewModel.prepareTransaction(
                walletId = walletId,
                addressWithTokens = addressWithTokens,
                tokenEntity = tokenEntity,
                balance = balance
            )
        }

        LaunchedEffect(uiEvent) {
            when (uiEvent) {
                is TransferUiEvent.Success -> {
                    snackbar.showSuccessSnackbar(
                        "Успешное действие",
                        "Успешно отправлено ${(balance ?: tokenEntity?.balanceWithoutFrozen!!).toTokenAmount()} $tokenName",
                        "Закрыть",
                    )
                    resetUI()
                }
                is TransferUiEvent.Error -> {
                    val e = uiEvent as TransferUiEvent.Error

                    snackbar.showErrorSnackbar(
                        "Перевод валюты невозможен",
                        e.message,
                        "Закрыть",
                    )
                    resetUI()
                }
                is TransferUiEvent.Idle, null -> Unit
            }
        }

        val isGeneralAddressNotActivatedVisible by viewModel.isGeneralAddressNotActivatedVisible.collectAsState()
        val generalAddressActivatedCommission by viewModel.generalAddressActivatedCommission.collectAsState()

        LaunchedEffect(commissionState) {
            when (commissionState) {
                is EstimateCommissionResult.Loading -> Unit
                is EstimateCommissionResult.Success -> {
                    val commission = (commissionState as EstimateCommissionResult.Success).response.commission
                    val commissionResult = (commissionState as EstimateCommissionResult.Success).response

                    isButtonEnabled = true
                    setCommissionOnTransaction(commission.toBigInteger().toTokenAmount())
                    setCommissionResult(commissionResult)
                }
                is EstimateCommissionResult.Error -> Unit
                is EstimateCommissionResult.Empty -> Unit
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
                                    Text(text = "${generalAddressActivatedCommission?.toTokenAmount()} ")
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

                            viewModel.onConfirmTransaction(
                                addressWithTokens = addressWithTokens,
                                commission = commissionOnTransaction.toSunAmount(),
                                walletId = walletId,
                                tokenEntity = tokenEntity,
                                amount = balance ?: tokenEntity?.balanceWithoutFrozen!!,
                                commissionResult = commissionResult,
                            )
                        },
                        modifier =
                            Modifier
                                // Защищаемся от Tapjacking/Clickjacking
                                .onGloballyPositioned {
                                    (context as? Activity)?.window?.decorView?.filterTouchesWhenObscured = true
                                }
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

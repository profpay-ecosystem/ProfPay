package com.profpay.wallet.ui.feature.smartList.bottomSheets

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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.AppConstants
import com.profpay.wallet.bridge.viewmodel.smartcontract.GetSmartContractViewModel
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.BackgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.GreenColor
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetCreateContract(viewModel: GetSmartContractViewModel): Pair<Boolean, (Boolean) -> Unit> {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true },
        )

    var isButtonEnabled by remember { mutableStateOf(false) }

    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }
    val deployEstimateCommission by viewModel.stateEstimateResourcePrice.collectAsStateWithLifecycle()
    val (commission, setCommission) =
        remember {
            mutableStateOf(BigDecimal(0.0))
        }

    if (isOpenSheet) {
        LaunchedEffect(Unit) {
            viewModel.getResourceQuote(
                energy = AppConstants.SmartContract.PUBLISH_ENERGY_REQUIRED,
                bandwidth = AppConstants.SmartContract.PUBLISH_BANDWIDTH_REQUIRED,
            )
        }

        LaunchedEffect(deployEstimateCommission) {
            if (deployEstimateCommission.commission == 0L) return@LaunchedEffect

            isButtonEnabled = true
            setCommission(deployEstimateCommission.commission.toBigInteger().toTokenAmount())
        }

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
                modifier = Modifier.fillMaxSize(),
            ) {
                Row(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Создание контракта",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
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
                                "Мы взымаем комиссию в TRX за выпуск смарт-контракта, " +
                                    "чтобы компенсировать затраты, связанные с использованием ресурсов сети Tron, " +
                                    "таких как Bandwidth и Energy. Эти ресурсы необходимы для выполнения операций, " +
                                    "связанных с развертыванием и поддержкой смарт-контрактов. " +
                                    "Взымая комиссию в TRX, мы обеспечиваем возможность поддерживать " +
                                    "стабильную работу сети и покрывать затраты на её использование.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(10.dp),
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 8.dp),
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
                            Text(text = "$commission ")
                            Text(text = "TRX", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                Button(
                    enabled = isButtonEnabled,
                    onClick = {
                        isButtonEnabled = false
                        viewModel.deploySmartContract(
                            commission = commission,
                            energy = AppConstants.SmartContract.PUBLISH_ENERGY_REQUIRED,
                            bandwidth = AppConstants.SmartContract.PUBLISH_BANDWIDTH_REQUIRED,
                            context = context,
                        )
                        coroutineScope.launch {
                            sheetState.hide()
                            delay(400)
                            setIsOpenSheet(false)
                        }
                        isButtonEnabled = true
                    },
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
                            .height(IntrinsicSize.Max)
                            .fillMaxWidth(),
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
            }
        }
    }
    return isOpenSheet to { setIsOpenSheet(it) }
}

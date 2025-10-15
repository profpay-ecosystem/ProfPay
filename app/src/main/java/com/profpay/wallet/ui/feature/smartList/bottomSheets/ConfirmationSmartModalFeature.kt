package com.profpay.wallet.ui.feature.smartList.bottomSheets
import StackedSnakbarHostState
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.bridge.viewmodel.smartcontract.CompleteReturnData
import com.profpay.wallet.bridge.viewmodel.smartcontract.CompleteStatusesEnum
import com.profpay.wallet.bridge.viewmodel.smartcontract.GetSmartContractViewModel
import com.profpay.wallet.data.repository.flow.SmartContractButtonType
import com.profpay.wallet.data.repository.flow.SmartContractModalData
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.BackgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.app.theme.RedColor
import kotlinx.coroutines.launch
import java.math.BigDecimal

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun confirmationSmartModalFeature(
    smartModalState: SmartContractModalData,
    snackbar: StackedSnakbarHostState,
    viewModel: GetSmartContractViewModel = hiltViewModel(),
): Boolean {
    val (confirm, setConfirm) = remember { mutableStateOf(false) }
    if (smartModalState.deal != null) {
        val (commission, setCommission) = remember { mutableStateOf(BigDecimal(0.0)) }
        val (functionMessage, setFunctionMessage) = remember { mutableStateOf("") }
        var isButtonEnabled by remember { mutableStateOf(false) }

        val (_, setIsOpenAgreedSheet) = bottomSheetAgreeAmount(smartModalState.deal, viewModel)
        val estimateResourcePrice by viewModel.stateEstimateResourcePrice.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
//            var transactionEstimatorResult =
//                if (smartModalState.buttonType == SmartContractButtonType.ACCEPT) {
//                    viewModel.estimateCompleteContract(smartModalState.deal)
//                } else if (smartModalState.buttonType == SmartContractButtonType.REJECT) {
//                    viewModel.estimateRejectContract(smartModalState.deal)
//                } else {
//                    return@LaunchedEffect
//                }
//
//            if (transactionEstimatorResult != null) {
//                viewModel.getResourceQuote(
//                    address = transactionEstimatorResult.executorAddress!!,
//                    energy = transactionEstimatorResult.requiredEnergy!!,
//                    bandwidth = transactionEstimatorResult.requiredBandwidth!!,
//                )
//
//                if (transactionEstimatorResult.estimateType == EstimateType.APPROVE) {
//                    setFunctionMessage(
//                        "\n\nДля работы данной функции необходим approve, сначала выполнится он, после повторите вызов еще раз.",
//                    )
//                }
//            }
        }

        LaunchedEffect(estimateResourcePrice) {
            if (estimateResourcePrice.commission == 0L) return@LaunchedEffect

            isButtonEnabled = true
            setCommission(estimateResourcePrice.commission.toBigInteger().toTokenAmount())
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.05f))

            Row(
                modifier =
                    Modifier
                        .padding()
                        .fillMaxWidth(0.9f),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Комиссия",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(modifier = Modifier.fillMaxHeight(0.02f))

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
                            "Мы взымаем комиссию в TRX за выполнение функций контракта, " +
                                "чтобы компенсировать затраты, связанные с использованием ресурсов сети Tron, " +
                                "таких как Bandwidth и Energy. Эти ресурсы необходимы для выполнения операций, " +
                                "связанных с развертыванием и поддержкой смарт-контрактов." +
                                functionMessage,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
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
                        Text(text = commission.toString())
                        Text(text = " TRX", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            Row(
                modifier =
                    Modifier
                        .height(IntrinsicSize.Min)
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        viewModel.setSmartContractModalActive(false, null, null)
                    },
                    modifier =
                        Modifier
                            .padding(start = 16.dp, end = 4.dp)
                            .height(IntrinsicSize.Max)
                            .weight(0.5f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = RedColor,
                            contentColor = BackgroundContainerButtonLight,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Закрыть",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Button(
                    enabled = isButtonEnabled,
                    onClick = {
                        isButtonEnabled = false
                        viewModel.viewModelScope.launch {
                            setConfirm(true)
                            if (smartModalState.buttonType == SmartContractButtonType.ACCEPT) {
                                val result: CompleteReturnData = viewModel.completeContract(commission, smartModalState.deal)
                                if (result.result != null && result.result.amountRequired != null) {
                                    snackbar.showErrorSnackbar(
                                        "Ошибка",
                                        "Баланс TRX недостаточен, необходимо ${result.result.amountRequired!!.toTokenAmount()}",
                                        "Закрыть",
                                    )
                                }
                                if (result.status == CompleteStatusesEnum.CALL_EXPERT_AMOUNT_SHEET) {
                                    setIsOpenAgreedSheet(true)
                                }
                            } else if (smartModalState.buttonType == SmartContractButtonType.REJECT) {
                                viewModel.rejectContract(commission, smartModalState.deal)
                            }
                            isButtonEnabled = true
                        }
                    },
                    modifier =
                        Modifier
                            .padding(start = 4.dp, end = 16.dp)
                            .height(IntrinsicSize.Max)
                            .weight(0.5f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = GreenColor,
                            contentColor = BackgroundContainerButtonLight,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = "Продолжить",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    return confirm
}

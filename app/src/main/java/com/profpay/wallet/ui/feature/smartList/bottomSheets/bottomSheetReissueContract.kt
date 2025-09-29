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
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.AppConstants
import com.profpay.wallet.bridge.view_model.smart_contract.GetSmartContractViewModel
import com.profpay.wallet.ui.app.theme.BackgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.GreenColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetReissueContract(
    commission: BigDecimal,
    viewModel: GetSmartContractViewModel,
): Pair<Boolean, (Boolean) -> Unit> {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true },
        )

    var isButtonEnabled by remember { mutableStateOf(true) }

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
                modifier = Modifier.fillMaxSize(),
            ) {
                Row(
                    modifier = Modifier.padding(top = 16.dp, start = 16.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "Перевыпуск контракта",
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
                                "Вы можете перевыпустить смарт-контракт в случае если он был загрязнен " +
                                    "криптовалютой или по иной причине.\n" +
                                    "Для перевыпуска Вам необходимо завершить все активные сделки и не открывать новые.",
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
                            .padding(bottom = 4.dp, top = 8.dp),
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
                        if (commission != BigDecimal.ZERO) {
                            viewModel.viewModelScope.launch {
                                withContext(Dispatchers.IO) {
                                    isButtonEnabled = false
                                    viewModel.deploySmartContract(
                                        commission = commission,
                                        energy = AppConstants.SmartContract.PUBLISH_ENERGY_REQUIRED,
                                        bandwidth = AppConstants.SmartContract.PUBLISH_BANDWIDTH_REQUIRED,
                                        context = context,
                                    )
                                    isButtonEnabled = true
                                }
                            }
                        }
                    },
                    modifier =
                        Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 8.dp)
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

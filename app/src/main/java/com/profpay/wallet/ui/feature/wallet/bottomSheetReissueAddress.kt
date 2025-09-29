package com.profpay.wallet.ui.feature.wallet

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.bridge.view_model.wallet.ReissueCentralAddressViewModel
import com.profpay.wallet.ui.app.theme.BackgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.GreenColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetReissueAddress(viewModel: ReissueCentralAddressViewModel = hiltViewModel()): Pair<Boolean, (Boolean) -> Unit> {
    val coroutineScope = rememberCoroutineScope()
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true },
        )

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
                        text = "Замена общего адреса",
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
                                "Убедитесь, что на текущем общем адресе нет токенов.\n" +
                                    "При перевыпуске адреса Вы потеряете средства, находящиеся на текущем адресе.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }

                Button(
                    onClick = {
                        viewModel.viewModelScope.launch {
                            withContext(Dispatchers.IO) {
                                viewModel.reissueCentralAddress()
                                coroutineScope.launch {
                                    sheetState.hide()
                                    delay(400)
                                    setIsOpenSheet(false)
                                }
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
                        text = "Продолжить",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
    return isOpenSheet to { setIsOpenSheet(it) }
}

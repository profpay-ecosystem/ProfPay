package com.profpay.wallet.ui.feature.smartList

import StackedSnakbarHostState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.profpay.wallet.R
import com.profpay.wallet.bridge.view_model.smart_contract.GetSmartContractViewModel
import com.profpay.wallet.ui.feature.smartList.bottomSheets.bottomSheetCreateContract

@Composable
fun SmartHeaderCreateContractInListFeature(
    viewModel: GetSmartContractViewModel,
    snackbar: StackedSnakbarHostState,
    goToSystemTRX: () -> Unit,
) {
    val (_, setIsOpenCreateContractSheet) = bottomSheetCreateContract(viewModel = viewModel)
    val isActivated by viewModel.isActivated.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        val generalAddress = viewModel.addressRepo.getGeneralAddressByWalletId(1)
        viewModel.checkActivation(generalAddress)
    }

    Column(modifier = Modifier.padding(8.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 8.dp, end = 0.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "У вас нет контракта",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Card(
                modifier =
                    Modifier
                        .padding(start = 6.dp, end = 6.dp, top = 12.dp)
                        .fillMaxHeight(0.3f)
                        .fillMaxWidth()
                        .shadow(7.dp, RoundedCornerShape(10.dp))
                        .clickable {
                            if (isActivated) {
                                setIsOpenCreateContractSheet(true)
                            } else {
                                snackbar.showErrorSnackbar(
                                    "Создание контракта невозможно",
                                    "У Вас не активирован центральный адрес",
                                    "Перейти",
                                    action = goToSystemTRX,
                                )
                            }
                        },
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_create),
                        contentDescription = "",
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        text = "Создать контракт",
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }

    Spacer(modifier = Modifier.fillMaxHeight(0.1f))
}

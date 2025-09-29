package com.profpay.wallet.ui.feature.smartList

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.R
import com.profpay.wallet.bridge.view_model.smart_contract.ContractButtonVisibleType
import com.profpay.wallet.bridge.view_model.smart_contract.GetSmartContractViewModel
import com.profpay.wallet.bridge.view_model.smart_contract.StatusData
import com.profpay.wallet.bridge.view_model.smart_contract.usecases.isBuyerNotDeposited
import com.profpay.wallet.bridge.view_model.smart_contract.usecases.isSellerNotPayedExpertFee
import com.profpay.wallet.data.flow_db.repo.SmartContractButtonType
import com.profpay.wallet.data.utils.toBigInteger
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.BackgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.app.theme.LocalFontSize
import com.profpay.wallet.ui.app.theme.RedColor
import com.profpay.wallet.ui.feature.smartList.bottomSheets.bottomSheetDetails
import com.profpay.wallet.ui.feature.wallet.HexagonShape
import kotlinx.coroutines.launch
import org.example.protobuf.smart.SmartContractProto
import java.math.BigInteger

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun SmartCardFeature(
    index: Int,
    item: SmartContractProto.ContractDealListResponse,
    viewModel: GetSmartContractViewModel,
) {
    val scope = rememberCoroutineScope()

    val (status, setStatus) = remember { mutableStateOf<StatusData?>(null) }
    val (oppositeUsername, setOppositeUsername) = remember { mutableStateOf<String?>(null) }
    val (oppositeUserId, setOppositeUserId) = remember { mutableStateOf<Long?>(null) }
    val (isButtonVisible, setIsButtonVisible) =
        remember {
            mutableStateOf<ContractButtonVisibleType>(
                ContractButtonVisibleType(false, false),
            )
        }
    val (isBuyerNotDeposited, setIsBuyerNotDeposited) = remember { mutableStateOf(false) }
    val (isSellerNotPayedExpertFee, setIsSellerNotPayedExpertFee) = remember { mutableStateOf(false) }
    val (_, setIsOpenDetailsSheet) = bottomSheetDetails(item, viewModel)

    LaunchedEffect(item) {
        scope.launch {
            setStatus(viewModel.smartContractStatus(deal = item))
            setOppositeUsername(viewModel.getOppositeUsername(deal = item))
            setOppositeUserId(viewModel.getOppositeTelegramId(deal = item))
            setIsButtonVisible(viewModel.isButtonVisible(deal = item))
            setIsBuyerNotDeposited(
                isBuyerNotDeposited(
                    item,
                    viewModel.profileRepo.getProfileUserId(),
                ),
            )
            setIsSellerNotPayedExpertFee(
                isSellerNotPayedExpertFee(
                    item,
                    viewModel.profileRepo.getProfileUserId(),
                ),
            )
        }
    }

    SmartCardWidget(
        indexToString = index.toString(),
        status = status,
        oppositeUsername = oppositeUsername,
        oppositeUserId = oppositeUserId,
        clickableDetails = { setIsOpenDetailsSheet(true) },
        item = item,
        isBuyerNotDeposited = isBuyerNotDeposited,
        isSellerNotPayedExpertFee = isSellerNotPayedExpertFee,
        isButtonVisible = isButtonVisible,
        onClickButtonCancel = {
            viewModel.viewModelScope.launch {
                viewModel.setSmartContractModalActive(true, SmartContractButtonType.REJECT, item)
            }
        },
        onClickButtonAgree = {
            viewModel.viewModelScope.launch {
                viewModel.setSmartContractModalActive(true, SmartContractButtonType.ACCEPT, item)
            }
        },
    )
}

@Composable
fun SmartCardWidget(
    indexToString: String,
    status: StatusData?,
    oppositeUsername: String?,
    oppositeUserId: Long?,
    clickableDetails: () -> Unit,
    item: SmartContractProto.ContractDealListResponse,
    isBuyerNotDeposited: Boolean,
    isSellerNotPayedExpertFee: Boolean,
    isButtonVisible: ContractButtonVisibleType,
    onClickButtonCancel: () -> Unit,
    onClickButtonAgree: () -> Unit,
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var expandedDropdownMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        elevation = CardDefaults.cardElevation(10.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .clip(HexagonShape(true))
                            .border(2.dp, Color(0xFF6A0E8D), HexagonShape(true))
                            .size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = indexToString,
                        fontSize = LocalFontSize.ExtraLarge.fS,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Column(
                    modifier =
                        Modifier
                            .padding(horizontal = 12.dp, 8.dp)
                            .weight(1f),
                ) {
                    Text(
                        text = status?.status ?: "Загрузка...",
                        fontSize = LocalFontSize.Medium.fS,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = "$oppositeUsername, ID №$oppositeUserId",
                        fontSize = LocalFontSize.ExtraSmall.fS,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .clickable { clickableDetails() },
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    "Детали",
                                    fontSize = LocalFontSize.ExtraSmall.fS,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                }
            }

            Row(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$${item.amount.toBigInteger().toTokenAmount()} (+$${(
                        item.dealData.totalExpertCommissions.toBigInteger() /
                            BigInteger.valueOf(
                                2,
                            )
                    ).toTokenAmount()})",
                    fontSize = LocalFontSize.Huge.fS,
                    color = RedColor,
                )
            }
            Row(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier =
                        Modifier
                            .weight(0.5f),
                    text = "Адрес контракта",
                    fontSize = LocalFontSize.Small.fS,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.clickable {
                            clipboardManager.setText(AnnotatedString(item.smartContractAddress))
                        },
                ) {
                    Text(
                        text =
                            "${item.smartContractAddress.take(5)}..." +
                                "${item.smartContractAddress.takeLast(5)} ",
                        fontSize = LocalFontSize.Small.fS,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Icon(
                        modifier = Modifier.padding(4.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    DropdownMenu(
                        expanded = expandedDropdownMenu,
                        onDismissRequest = { expandedDropdownMenu = false },
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(item.smartContractAddress))
                            },
                            text = { Text("Скопировать") },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            onClick = {
                                val intent =
                                    Intent(Intent.ACTION_VIEW).apply {
                                        data =
                                            "https://tronscan.org/#/address/${item.smartContractAddress}".toUri()
                                    }
                                context.startActivity(intent)
                            },
                            text = { Text("Перейти в Tron Scan", fontWeight = FontWeight.SemiBold) },
                        )
                    }
                }
            }
            Row(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier =
                        Modifier
                            .weight(0.5f),
                    text = "Получатель:",
                    fontSize = LocalFontSize.Small.fS,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.clickable {
                            clipboardManager.setText(
                                AnnotatedString(
                                    item.seller.address,
                                ),
                            )
                        },
                ) {
                    Text(
                        text =
                            "${item.seller.address.take(5)}..." +
                                "${item.seller.address.takeLast(5)} ",
                        fontSize = LocalFontSize.Small.fS,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Icon(
                        modifier = Modifier.padding(4.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Row(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier =
                        Modifier
                            .weight(0.5f),
                    text = "Отправитель:",
                    fontSize = LocalFontSize.Small.fS,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.clickable {
                            clipboardManager.setText(
                                AnnotatedString(
                                    item.buyer.address,
                                ),
                            )
                        },
                ) {
                    Text(
                        text =
                            "${item.buyer.address.take(5)}..." +
                                "${item.buyer.address.takeLast(5)} ",
                        fontSize = LocalFontSize.Small.fS,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Icon(
                        modifier = Modifier.padding(4.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            if (isBuyerNotDeposited || isSellerNotPayedExpertFee) {
                Row(
                    modifier =
                        Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RedColor),
                    ) {
                        Text(
                            modifier =
                                Modifier
                                    .padding(8.dp),
                            text =
                                "Будет списана половина комиссии на адрес смарт-контракта\n" +
                                    "в размере $${((item.dealData.totalExpertCommissions / 2).toBigInteger()).toTokenAmount()}, " +
                                    "вторая часть будет списана у контрагента",
                            fontSize = LocalFontSize.Medium.fS,
                            color = RedColor,
                        )
                    }
                }
            }
            Row(
                modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        onClickButtonCancel()
                    },
                    enabled = isButtonVisible.cancelVisible,
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .weight(0.5f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = RedColor,
                            contentColor = BackgroundContainerButtonLight,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = status?.rejectButtonName ?: "Загрузка...",
                        fontSize = LocalFontSize.Medium.fS,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Button(
                    onClick = {
                        onClickButtonAgree()
                    },
                    enabled = isButtonVisible.agreeVisible,
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .weight(0.5f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = GreenColor,
                            contentColor = BackgroundContainerButtonLight,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = status?.completeButtonName ?: "Загрузка...",
                        fontSize = LocalFontSize.Medium.fS,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

@Composable
fun SmartCardWidget2(
    indexToString: String,
    status: StatusData?,
    oppositeUsername: String?,
    oppositeUserId: Long?,
    clickableDetails: () -> Unit,
    item: SmartContractProto.ContractDealListResponse?,
    isBuyerNotDeposited: Boolean,
    isSellerNotPayedExpertFee: Boolean,
    isButtonVisible: Boolean,
    onClickButtonCancel: () -> Unit,
    onClickButtonAgree: () -> Unit,
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var expandedDropdownMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
        elevation = CardDefaults.cardElevation(10.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .clip(HexagonShape(true))
                            .border(2.dp, Color(0xFF6A0E8D), HexagonShape(true))
                            .size(40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = indexToString,
                        fontSize = LocalFontSize.Huge.fS,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                Column(
                    modifier =
                        Modifier
                            .padding(horizontal = 12.dp, 8.dp)
                            .weight(1f),
                ) {
                    Text(
                        text = status?.status ?: "Загрузка...",
                        fontSize = LocalFontSize.Medium.fS,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = "$oppositeUsername, ID №$oppositeUserId",
                        fontSize = LocalFontSize.ExtraSmall.fS,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .clickable { clickableDetails() },
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Text(
                                    "Детали",
                                    fontSize = LocalFontSize.ExtraSmall.fS,
                                    fontWeight = FontWeight.SemiBold,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                }
            }

            Row(
                modifier =
                    Modifier
                        .padding(8.dp)
                        .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$${item?.amount?.toBigInteger()?.toTokenAmount()} (+$${
                        item?.dealData?.totalExpertCommissions?.toBigInteger()?.toTokenAmount()
                    })",
                    fontSize = LocalFontSize.Huge.fS,
                    color = RedColor,
                )
            }
            Row(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp, vertical = 0.dp)
                        .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier =
                        Modifier
                            .weight(0.5f),
                    text = "Адрес контракта",
                    fontSize = LocalFontSize.Small.fS,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.clickable {
                            expandedDropdownMenu = !expandedDropdownMenu
                        },
                ) {
                    Text(
                        text =
                            "TRYDU..." +
                                "9kfYd ",
                        fontSize = LocalFontSize.Small.fS,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Icon(
                        modifier = Modifier.padding(4.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )

                    DropdownMenu(
                        expanded = expandedDropdownMenu,
                        onDismissRequest = { expandedDropdownMenu = false },
                    ) {
                        DropdownMenuItem(
                            onClick = {
                            },
                            text = { Text("Скопировать") },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            onClick = {
                                val intent =
                                    Intent(Intent.ACTION_VIEW).apply {
                                        data =
                                            "https://tronscan.org/#/address/${""}".toUri()
                                    }
                                context.startActivity(intent)
                            },
                            text = { Text("Перейти в Tron Scan", fontWeight = FontWeight.SemiBold) },
                        )
                    }
                }
            }
            Row(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier =
                        Modifier
                            .weight(0.5f),
                    text = "Получатель:",
                    fontSize = LocalFontSize.Small.fS,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.clickable {
                        },
                ) {
                    Text(
                        text =
                            "IJDKs..." +
                                "o9iKd ",
                        fontSize = LocalFontSize.Small.fS,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Icon(
                        modifier = Modifier.padding(4.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            Row(
                modifier =
                    Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier =
                        Modifier
                            .weight(0.5f),
                    text = "Отправитель:",
                    fontSize = LocalFontSize.Small.fS,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier =
                        Modifier.clickable {
                        },
                ) {
                    Text(
                        text =
                            "iOKCx..." +
                                "O9dk7 ",
                        fontSize = LocalFontSize.Small.fS,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                    Icon(
                        modifier = Modifier.padding(4.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
            if (isBuyerNotDeposited || isSellerNotPayedExpertFee) {
                Row(
                    modifier =
                        Modifier
                            .padding(start = 8.dp, end = 8.dp)
                            .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Card(
                        modifier =
                            Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxSize(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, RedColor),
                    ) {
                        Text(
                            modifier =
                                Modifier
                                    .padding(8.dp),
                            text =
                                "Будет списана половина комиссии на адрес смарт-контракта\n" +
                                    "в размере $$, " +
                                    "вторая часть будет списана у контрагента",
                            fontSize = LocalFontSize.Medium.fS,
                            color = RedColor,
                        )
                    }
                }
            }
            Row(
                modifier =
                    Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        onClickButtonCancel()
                    },
                    enabled = false,
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .weight(0.5f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = RedColor,
                            contentColor = BackgroundContainerButtonLight,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = status?.rejectButtonName ?: "Загрузка...",
                        fontSize = LocalFontSize.Medium.fS,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                Button(
                    onClick = {
                        onClickButtonAgree()
                    },
                    enabled = false,
                    modifier =
                        Modifier
                            .padding(horizontal = 8.dp)
                            .weight(0.5f),
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = GreenColor,
                            contentColor = BackgroundContainerButtonLight,
                        ),
                    shape = RoundedCornerShape(12.dp),
                ) {
                    Text(
                        text = status?.completeButtonName ?: "Загрузка...",
                        fontSize = LocalFontSize.Medium.fS,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}

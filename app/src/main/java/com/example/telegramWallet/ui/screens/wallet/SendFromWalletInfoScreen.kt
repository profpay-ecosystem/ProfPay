package com.example.telegramWallet.ui.screens.wallet

import StackedSnackbarHost
import StackedSnakbarHostState
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.dto.TokenName
import com.example.telegramWallet.bridge.view_model.dto.transfer.TransferResult
import com.example.telegramWallet.bridge.view_model.wallet.send.SendFromWalletViewModel
import com.example.telegramWallet.data.database.models.AddressWithTokens
import com.example.telegramWallet.data.utils.toSunAmount
import com.example.telegramWallet.data.utils.toTokenAmount
import com.example.telegramWallet.ui.app.theme.BackgroundContainerButtonLight
import com.example.telegramWallet.ui.app.theme.BackgroundLight
import com.example.telegramWallet.ui.app.theme.GreenColor
import com.example.telegramWallet.ui.app.theme.ProgressIndicator
import com.example.telegramWallet.ui.app.theme.PubAddressDark
import com.example.telegramWallet.ui.app.theme.RedColor
import com.example.telegramWallet.ui.app.theme.WalletNavigationBottomBarTheme
import com.example.telegramWallet.ui.shared.sharedPref
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.example.protobuf.transfer.TransferProto.TransferToken
import rememberStackedSnackbarHostState
import java.math.BigDecimal
import java.math.BigInteger
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun SendFromWalletInfoScreen(
    addressId: Long,
    tokenName: String,
    viewModel: SendFromWalletViewModel = hiltViewModel(),
    goToBack: () -> Unit,
    goToSystemTRX: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val tokenNameModel = TokenName.valueOf(tokenName)
    val currentTokenName = TokenName.entries.find { it.tokenName == tokenName } ?: TokenName.USDT

    var addressSending by rememberSaveable { mutableStateOf("") }
    var sumSending by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadAddressWithTokens(
            addressId,
            tokenNameModel.blockchainName,
            currentTokenName.tokenName
        )
    }

    LaunchedEffect(addressSending, sumSending) {
        viewModel.updateInputs(addressSending, sumSending, currentTokenName)
    }

    LaunchedEffect(viewModel.stateCommission.collectAsStateWithLifecycle().value) {
        viewModel.onCommissionResult(viewModel.stateCommission.value)
    }

    val stackedSnackbarHostState = rememberStackedSnackbarHostState()
    val (_, setIsOpenTransferProcessingSheet) = bottomSheetTransferConfirmation(
        modelTransferFromBS = ModelTransferFromBS(
            sumSending = sumSending.takeIf { it.isNotBlank() }?.toBigDecimalOrNull()
                ?: BigDecimal.ZERO,
            tokenName = tokenNameModel,
            addressSending = addressSending,
            addressSender = uiState.addressWithTokens?.addressEntity?.address ?: "",
            commissionOnTransaction = uiState.commission,
            addressWithTokens = uiState.addressWithTokens
        ),
        snackbar = stackedSnackbarHostState
    )

    val bottomPadding = sharedPref().getFloat("bottomPadding", 54f)

    Scaffold(
        modifier = Modifier,
        snackbarHost = {
            StackedSnackbarHost(
                hostState = stackedSnackbarHostState,
                modifier = Modifier
                    .padding(8.dp, 90.dp)
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clickable {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }) {}
        Column(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painterResource(id = R.drawable.wallet_background),
                    contentScale = ContentScale.FillBounds
                )
                .clickable {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }, verticalArrangement = Arrangement.Bottom
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Transfer",
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color.White)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    run {
                        IconButton(onClick = { goToBack() }) {
                            Icon(
                                modifier = Modifier.size(34.dp),
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                },
                actions = {
                    run {
                        IconButton(onClick = { /*goToBack()*/ }) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_alert),
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .weight(0.8f)
                    .shadow(7.dp, RoundedCornerShape(16.dp))
                    .verticalScroll(rememberScrollState()),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomPadding.dp)
                        .padding(vertical = 8.dp, horizontal = 16.dp),
                ) {
                    CardWithAddressForSendFromWallet(
                        title = "Адрес получения",
                        addressSending = addressSending,
                        onAddressChange = { addressSending = it },
                        warningAddress = true // Todo
                    )
                    Row(
                        modifier = Modifier
                            .padding(
                                top = 20.dp
                            )
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = 8.dp),
                            text = "Сумма",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                modifier = Modifier.padding(end = 4.dp),
                                text = uiState.tokenBalance.toTokenAmount().toString(),
                                style = MaterialTheme.typography.bodyLarge
//                                color = PubAddressDark
                            )
                            Text(
                                text = currentTokenName.shortName,
                                style = MaterialTheme.typography.bodySmall,
//                                color = PubAddressDark,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        TextField(
                            value = sumSending,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth(),
                            placeholder = {
                                Text(
                                    text = "Введите кол-во ${currentTokenName.shortName}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = PubAddressDark
                                )
                            },
                            shape = MaterialTheme.shapes.small.copy(),
                            onValueChange = { sumSending = it.replace(",", ".") },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = currentTokenName.shortName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = PubAddressDark,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )

                                    Card(
                                        shape = RoundedCornerShape(5.dp),
                                        modifier = Modifier.padding(end = 8.dp),
                                        elevation = CardDefaults.cardElevation(7.dp),
                                        onClick = {
                                            sumSending =
                                                uiState.tokenBalance.toTokenAmount().toString()
                                        }
                                    ) {
                                        Text(
                                            "MAX",
                                            style = MaterialTheme.typography.bodyLarge,
                                            modifier = Modifier.padding(
                                                horizontal = 12.dp,
                                                vertical = 8.dp
                                            ),
                                        )
                                    }
                                }
                            },
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

                    if (uiState.warning != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    modifier = Modifier,
                                    text = uiState.warning!!,
                                    color = RedColor
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(bottom = 20.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Button(
                            onClick = {
                                if (viewModel.tron.addressUtilities.isValidTronAddress(
                                        addressSending
                                    )
                                ) {
                                    setIsOpenTransferProcessingSheet(true)
                                }
                            },
                            enabled = uiState.isButtonEnabled,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenColor,
                                contentColor = BackgroundContainerButtonLight
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Перевести",
                                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                            )
                        }
                    }
                }
            }
        }
    }
}

class ModelTransferFromBS(
    val sumSending: BigDecimal,
    val tokenName: TokenName,
    val addressSending: String,
    val addressSender: String,
    val commissionOnTransaction: BigDecimal,
    val addressWithTokens: AddressWithTokens?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetTransferConfirmation(
    viewModel: SendFromWalletViewModel = hiltViewModel(),
    modelTransferFromBS: ModelTransferFromBS,
    snackbar: StackedSnakbarHostState
): Pair<Boolean, (Boolean) -> Unit> {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { false }
    )
    val coroutineScope = rememberCoroutineScope()
    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }

    val (isConfirmTransaction, setIsConfirmTransaction) = remember { mutableStateOf(false) }
    val (isDetailsTransaction, setIsDetailsTransaction) = remember { mutableStateOf(false) }

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

            Column {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(text = "Перевод", fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                sheetState.hide()
                                delay(400)
                                setIsOpenSheet(false)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "",
                                modifier = Modifier.size(22.dp)
                            )
                        }

                    }
                }

                if (!isConfirmTransaction) {
                    ContentBottomSheetTransferConfirmation(
                        isDetails = isDetailsTransaction,
                        modelTransferFromBS = modelTransferFromBS,
                        confirmTransaction = {
                            viewModel.viewModelScope.launch {
                                val tokenName = modelTransferFromBS.tokenName.tokenName
                                val tokenEntity = modelTransferFromBS.addressWithTokens
                                    ?.tokens
                                    ?.firstOrNull { it.token.tokenName == tokenName }

                                if (tokenEntity == null) {
                                    snackbar.showErrorSnackbar(
                                        title = "Ошибка перевода",
                                        description = "Не удалось найти токен",
                                        actionTitle = "Закрыть"
                                    )

                                    coroutineScope.launch {
                                        sheetState.hide()
                                        delay(400)
                                        setIsOpenSheet(false)
                                    }
                                    return@launch
                                }

                                val result = viewModel.transferProcess(
                                    senderAddress = modelTransferFromBS.addressSender,
                                    receiverAddress = modelTransferFromBS.addressSending,
                                    amount = modelTransferFromBS.sumSending.toSunAmount(),
                                    commission = modelTransferFromBS.commissionOnTransaction.toSunAmount(),
                                    tokenEntity = tokenEntity
                                )

                                when (result) {
                                    is TransferResult.Success -> {
                                        setIsConfirmTransaction(true)
                                    }

                                    is TransferResult.Failure -> {
                                        snackbar.showErrorSnackbar(
                                            title = "Ошибка перевода",
                                            description = result.error.message!!,
                                            actionTitle = "Закрыть"
                                        )

                                        coroutineScope.launch {
                                            sheetState.hide()
                                            delay(400)
                                            setIsOpenSheet(false)
                                        }
                                    }
                                }
                            }
                        }
                    )
                } else {
                    ContentBottomSheetTransferProcessing(onClick = {
                        setIsConfirmTransaction(false)
                        setIsDetailsTransaction(true)
                    })
                }
            }
        }
    }
    return isOpenSheet to { setIsOpenSheet(it) }
}

@Composable
fun CardWithAddressForSendFromWallet(
    title: String,
    addressSending: String,
    onAddressChange: (String) -> Unit,
    warningAddress: Boolean,
) {
    val colorContainer = if (warningAddress) {
        RedColor.copy(alpha = 0.3f)
    } else {
        MaterialTheme.colorScheme.primary
    }
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp),
    )
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        border = if (warningAddress) {
            BorderStroke(2.dp, color = RedColor)
        } else {
            null
        }
    ) {
        TextField(
            value = addressSending,
//                            textStyle = TextStyle(fontSize = 14.sp),
            modifier = Modifier
                .fillMaxWidth(),
            placeholder = {
                Text(
                    text = "Введите адрес",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PubAddressDark
                )
            },
            shape = MaterialTheme.shapes.small.copy(),
            onValueChange = { onAddressChange(it) },
            trailingIcon = {},
            colors =
                TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = PubAddressDark,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedContainerColor = colorContainer,
                    unfocusedContainerColor = colorContainer,
                    cursorColor = MaterialTheme.colorScheme.onBackground,
                    selectionColors = TextSelectionColors(
                        handleColor = MaterialTheme.colorScheme.onBackground,
                        backgroundColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
                    )
                )

        )
    }
}

@Composable
fun ContentBottomSheetTransferConfirmation(
    viewModel: SendFromWalletViewModel = hiltViewModel(),
    isDetails: Boolean,
    modelTransferFromBS: ModelTransferFromBS,
    confirmTransaction: () -> Unit
) {
    val decimalFormat = DecimalFormat("#.###")
    val tokenNameModel = modelTransferFromBS.tokenName

    val (trxToUsdtRate, setTrxToUsdtRate) = remember { mutableStateOf(BigDecimal.valueOf(1.0)) }
    val isConfirmButtonEnabled = remember { mutableStateOf(true) }
    val (isNeedActivationAddress, setIsNeedActivationAddress) = remember { mutableStateOf(false) }
    val (createNewAccountFeeInSystemContract, setCreateNewAccountFeeInSystemContract) = remember {
        mutableStateOf(
            BigInteger.ZERO
        )
    }

    LaunchedEffect(Unit) {
        val isAddressActivated = withContext(Dispatchers.IO) {
            viewModel.tron.addressUtilities.isAddressActivated(modelTransferFromBS.addressSending)
        }
        if (!isAddressActivated) {
            setCreateNewAccountFeeInSystemContract(
                withContext(Dispatchers.IO) {
                    viewModel.tron.addressUtilities.getCreateNewAccountFeeInSystemContract()
                }
            )
            setIsNeedActivationAddress(true)
        }

        setTrxToUsdtRate(viewModel.trxToUsdtRate())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 0.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "-${modelTransferFromBS.sumSending} ${tokenNameModel.shortName}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            if (tokenNameModel.shortName == "TRX") {
                Text(
                    text = "≈ ${decimalFormat.format(modelTransferFromBS.sumSending * trxToUsdtRate)} $",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PubAddressDark
                )
            } else {
                Text(
                    text = "≈ ${decimalFormat.format(modelTransferFromBS.sumSending)} $",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PubAddressDark
                )
            }
        }
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Сеть",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PubAddressDark,
                        modifier = Modifier
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(20.dp)
                                .paint(
                                    painterResource(id = tokenNameModel.paintIconId),
                                    contentScale = ContentScale.FillBounds
                                )
                        )
                        Text(
                            text = "${tokenNameModel.blockchainName} (${tokenNameModel.shortName})",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Откуда",
                        color = PubAddressDark,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                    )
                    Text(
                        text = "${modelTransferFromBS.addressSender.take(7)}...${
                            modelTransferFromBS.addressSender.takeLast(
                                7
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                    )

                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Куда",
                        color = PubAddressDark,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                    )
                    Text(
                        text = "${modelTransferFromBS.addressSending.take(7)}...${
                            modelTransferFromBS.addressSending.takeLast(
                                7
                            )
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                    )
                }

            }
        }
        Card(
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            elevation = CardDefaults.cardElevation(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Комиссия",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PubAddressDark,
                        modifier = Modifier
                    )
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${modelTransferFromBS.commissionOnTransaction} TRX",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        Text(
                            text = "≈ ${decimalFormat.format(modelTransferFromBS.commissionOnTransaction * trxToUsdtRate)} $",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PubAddressDark
                        )
                    }
                }
                if (isNeedActivationAddress) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {

                        Text(
                            text = "Активация адреса",
                            color = PubAddressDark,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier
                        )
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${createNewAccountFeeInSystemContract.toTokenAmount()} TRX",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            Text(
                                text = "≈ ${
                                    decimalFormat.format(
                                        createNewAccountFeeInSystemContract.toTokenAmount() * trxToUsdtRate
                                    )
                                } $",
                                style = MaterialTheme.typography.bodyMedium,
                                color = PubAddressDark
                            )
                        }

                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {

                    Text(
                        text = "Итого",
                        style = MaterialTheme.typography.bodySmall,
                        color = PubAddressDark,
                        modifier = Modifier
                    )
                    Text(
                        text = "${decimalFormat.format((createNewAccountFeeInSystemContract.toTokenAmount() + modelTransferFromBS.commissionOnTransaction) * trxToUsdtRate)} $",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                    )

                }
            }
        }
        if (!isDetails) {
            Spacer(modifier = Modifier.height(50.dp))
            Button(
                enabled = isConfirmButtonEnabled.value,
                onClick = {
                    if (!isConfirmButtonEnabled.value) return@Button
                    isConfirmButtonEnabled.value = false
                    confirmTransaction()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 4.dp, vertical = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GreenColor,
                    contentColor = BackgroundLight
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Подтвердить",
                    style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                )
            }
        }
    }
}

@Composable
fun ContentBottomSheetTransferProcessing(onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(top = 40.dp, bottom = 20.dp)
                .size(50.dp),
            color = ProgressIndicator
        )

        Text(
            "Перевод обрабатывается",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 40.dp)
                .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Button(
                onClick = {
                    onClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(horizontal = 4.dp), colors = ButtonDefaults.buttonColors(
                    containerColor = GreenColor,
                    contentColor = BackgroundLight
                ), shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Детали Перевода",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

    }

}
package com.profpay.wallet.ui.feature.wallet.send.bottomsheet

import StackedSnakbarHostState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.bridge.view_model.dto.transfer.TransferResult
import com.profpay.wallet.bridge.view_model.wallet.send.SendFromWalletViewModel
import com.profpay.wallet.data.utils.toSunAmount
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetTransferConfirmation(
    viewModel: SendFromWalletViewModel = hiltViewModel(),
    modelTransferFromBS: ModelTransferFromBS,
    snackbar: StackedSnakbarHostState,
): Pair<Boolean, (Boolean) -> Unit> {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { false },
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
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Text(text = "Перевод", fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier =
                            Modifier
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
                                modifier = Modifier.size(22.dp),
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
                                val tokenEntity =
                                    modelTransferFromBS.addressWithTokens
                                        ?.tokens
                                        ?.firstOrNull { it.token.tokenName == tokenName }

                                if (tokenEntity == null) {
                                    snackbar.showErrorSnackbar(
                                        title = "Ошибка перевода",
                                        description = "Не удалось найти токен",
                                        actionTitle = "Закрыть",
                                    )

                                    coroutineScope.launch {
                                        sheetState.hide()
                                        delay(400)
                                        setIsOpenSheet(false)
                                    }
                                    return@launch
                                }

                                val result =
                                    viewModel.transferProcess(
                                        senderAddress = modelTransferFromBS.addressSender,
                                        receiverAddress = modelTransferFromBS.addressReceiver,
                                        amount = modelTransferFromBS.amount.toSunAmount(),
                                        commission = modelTransferFromBS.commission.toSunAmount(),
                                        tokenEntity = tokenEntity,
                                        commissionResult = modelTransferFromBS.commissionResult,
                                    )

                                when (result) {
                                    is TransferResult.Success -> {
                                        setIsConfirmTransaction(true)
                                    }

                                    is TransferResult.Failure -> {
                                        snackbar.showErrorSnackbar(
                                            title = "Ошибка перевода",
                                            description = result.error.message!!,
                                            actionTitle = "Закрыть",
                                        )

                                        coroutineScope.launch {
                                            sheetState.hide()
                                            delay(400)
                                            setIsOpenSheet(false)
                                        }
                                    }
                                }
                            }
                        },
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

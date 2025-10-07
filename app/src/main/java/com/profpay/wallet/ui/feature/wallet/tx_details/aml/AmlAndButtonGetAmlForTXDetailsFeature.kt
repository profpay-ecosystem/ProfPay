package com.profpay.wallet.ui.feature.wallet.tx_details.aml

import StackedSnakbarHostState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.bridge.view_model.wallet.TXDetailsViewModel
import com.profpay.wallet.data.database.entities.wallet.TransactionEntity
import com.profpay.wallet.data.flow_db.repo.AmlResult
import com.profpay.wallet.exceptions.aml.ServerAmlException
import com.profpay.wallet.ui.app.theme.BackgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.backgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.greenColor
import com.profpay.wallet.ui.widgets.dialog.AlertDialogWidget
import com.profpay.wallet.utils.aml.toAmlType
import io.sentry.Sentry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun AmlAndButtonGetAmlForTXDetailsFeature(
    amlState: AmlResult,
    viewModel: TXDetailsViewModel,
    transactionEntity: TransactionEntity,
    stackedSnackbarHostState: StackedSnakbarHostState,
    amlReleaseDialog: Boolean,
    setAmlReleaseDialog: (Boolean) -> Unit,
    amlButtonIsEnabled: Boolean,
    setAmlButtonIsEnabled: (Boolean) -> Unit,
    amlFeeResultText: String
) {
    when (val result = amlState) {
        is AmlResult.Success -> {
            if (result.response.amlId.isNotEmpty()) {
                KnowAMLFeature(
                    viewModel = viewModel,
                    amlType = result.response.toAmlType(),
                    amlState = result.response,
                    transactionEntity = transactionEntity,
                    stackedSnackbarHostState = stackedSnackbarHostState,
                )
            } else {
                UnknownAMLFeature()
            }
        }

        is AmlResult.Error -> {
            LaunchedEffect(stackedSnackbarHostState) {
                when (result.throwable.message) {
                    "ABORTED: Time has not yet passed since the last request" -> {
                        stackedSnackbarHostState.showErrorSnackbar(
                            "Ошибка запроса",
                            "Запрос на перевыпуск AML разрешен раз в день.",
                            "Закрыть",
                        )
                        viewModel.getAmlFromTransactionId(
                            transactionEntity.receiverAddress,
                            transactionEntity.txId,
                            transactionEntity.tokenName,
                        )
                    }

                    "FAILED_PRECONDITION: This AML was not paid by the client" -> {
                        stackedSnackbarHostState.showErrorSnackbar(
                            "Запрос отклонен",
                            "Для получения данного AML его необходимо оплатить.",
                            "Закрыть",
                        )
                    }

                    else -> {
                        stackedSnackbarHostState.showErrorSnackbar(
                            "Ошибка запроса",
                            "Сервер вернул ошибку, пожалуйста, сообщите поддержке и повторите через минуту.",
                            "Закрыть",
                        )
                        Sentry.captureException(
                            ServerAmlException(
                                result.throwable.message
                                    ?: "Пустое сообщение, анализируйте сервер.",
                                result.throwable,
                            ),
                        )
                    }
                }
            }
            UnknownAMLFeature()
        }

        is AmlResult.Loading, AmlResult.Empty -> {
            UnknownAMLFeature()
        }
    }
    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .padding(bottom = 10.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Button(
            onClick = {
                setAmlReleaseDialog(true)
            },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.greenColor,
                    contentColor = MaterialTheme.colorScheme.backgroundContainerButtonLight,
                ),
            shape = RoundedCornerShape(12.dp),
            enabled = amlButtonIsEnabled,
        ) {
            Text(
                text = "Получить AML",
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
    if (amlReleaseDialog) {
        AlertDialogWidget(
            onConfirmation = {
                viewModel.viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        setAmlReleaseDialog(false)
                        setAmlButtonIsEnabled(false)

                        val (status, message) =
                            viewModel.processedAmlReport(
                                receiverAddress = transactionEntity.receiverAddress,
                                txId = transactionEntity.txId,
                            )

                        if (status) {
                            stackedSnackbarHostState.showSuccessSnackbar(
                                "Успешное действие",
                                message,
                                "Закрыть",
                            )
                        } else {
                            setAmlButtonIsEnabled(true)
                            stackedSnackbarHostState.showErrorSnackbar(
                                "Ошибка запроса",
                                message,
                                "Закрыть",
                            )
                        }
                    }
                }
            },
            onDismissRequest = {
                setAmlReleaseDialog(false)
            },
            dialogTitle = "Выпуск AML",
            dialogText =
                "Для получения AML необходимо внести плату за его выпуск или перевыпуск в размере $amlFeeResultText TRX.\n\n" +
                        "Это обязательная процедура, которая обеспечивает актуализацию и соответствие AML требованиям текущего законодательства и стандартов.\n\n" +
                        "Сумма будет списана с центрального адреса которому принадлежит данный адрес!",
            textConfirmButton = "Оплатить и получить",
            textDismissButton = "Закрыть",
        )
    }
}

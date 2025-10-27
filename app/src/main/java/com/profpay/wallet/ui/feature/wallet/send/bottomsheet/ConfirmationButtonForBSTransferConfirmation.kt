package com.profpay.wallet.ui.feature.wallet.send.bottomsheet

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profpay.wallet.ui.app.theme.backgroundLight
import com.profpay.wallet.ui.app.theme.greenColor
import com.profpay.wallet.ui.widgets.dialog.AlertDialogWidget


@Composable
fun ConfirmationButtonForBSTransferConfirmation(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    showContractWarning: Boolean,
    onConfirm: () -> Unit,
    onShowContractWarning: (Boolean) -> Unit,
    onClose: () -> Unit
) {
    Spacer(modifier = Modifier.height(50.dp))

    ConfirmationButton(
        modifier = modifier,
        isEnabled = isEnabled,
        onConfirm = onConfirm,
    )

    if (showContractWarning) {
        AlertDialogTransferConfirmationIfIsContractAddress(
            onConfirmation = onConfirm,
            onDismissRequest = {
                onShowContractWarning(false)
                onClose()
            }
        )
    }
}

@Composable
private fun ConfirmationButton(
    modifier: Modifier = Modifier,
    isEnabled: Boolean,
    onConfirm: () -> Unit,
) {
    Button(
        enabled = isEnabled,
        onClick = {
            onConfirm()
        },
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 4.dp, vertical = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.greenColor,
            contentColor = MaterialTheme.colorScheme.backgroundLight,
        ),
        shape = RoundedCornerShape(12.dp),
    ) {
        Text(
            text = "Подтвердить",
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp)
        )
    }
}
@Composable
private fun AlertDialogTransferConfirmationIfIsContractAddress(
    onConfirmation: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialogWidget(
        onConfirmation = { onConfirmation() },
        onDismissRequest = { onDismissRequest() },
        dialogTitle = "Осторожно!",
        dialogText =
            """
                Вы отправляете средства на адрес смарт-контракта.
                Если этот контракт не предназначен для приёма переводов,
                ваши средства могут быть безвозвратно утеряны.
                """.trimIndent(),
        textConfirmButton = "Продолжить",
        textDismissButton = "Закрыть",
        icon = Icons.Default.Warning
    )
}

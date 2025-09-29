package com.profpay.wallet.ui.widgets.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
fun AlertDialogWidget(
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
    confirmEnabled: Boolean = true,
    dialogTitle: String,
    isSmallDialogTitle: Boolean = false,
    dialogText: String,
    icon: ImageVector? = null,
    iconSize: Int = 25,
    colorIcon: Color? = null,
    textDismissButton: String,
    textConfirmButton: String,
    content: @Composable () -> Unit = {},
) {
    AlertDialog(
        icon = {
            if (colorIcon != null && icon != null) {
                Icon(
                    icon,
                    contentDescription = "Example Icon",
                    tint = colorIcon,
                    modifier = Modifier.size(iconSize.dp),
                )
            } else if (icon != null) {
                Icon(
                    icon,
                    contentDescription = "Example Icon",
                    modifier = Modifier.size(iconSize.dp),
                )
            }
        },
        title = {
            if (isSmallDialogTitle) {
                Text(text = dialogTitle, style = MaterialTheme.typography.bodyLarge)
            } else {
                Text(text = dialogTitle)
            }
        },
        text = {
            Column {
                Text(text = dialogText)
                content()
            }
        },
        onDismissRequest = {
            onDismissRequest()
        },
        dismissButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
                onClick = {
                    onDismissRequest()
                },
            ) {
                Text(textDismissButton)
            }
        },
        confirmButton = {
            TextButton(
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
                onClick = {
                    onConfirmation()
                },
                enabled = confirmEnabled,
            ) {
                Text(textConfirmButton)
            }
        },
    )
}

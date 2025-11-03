package com.profpay.wallet.ui.components.feature.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.profpay.wallet.ui.app.theme.BackgroundIcon
import com.profpay.wallet.ui.app.theme.DarkBlue
import com.profpay.wallet.ui.app.theme.SwitchColor


@Composable
fun switchForSettings(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
): Boolean {
    var checkIs by remember { mutableStateOf(checked) }
    Switch(
        modifier = Modifier.padding(end = 8.dp),
        checked = checkIs,
        onCheckedChange = {
            checkIs = it
            onCheckedChange(it)
        },
        colors =
            SwitchDefaults.colors(
                checkedThumbColor = DarkBlue,
                checkedTrackColor = SwitchColor,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = BackgroundIcon,
                uncheckedBorderColor = MaterialTheme.colorScheme.primary,
            ),
    )
    return checkIs
}

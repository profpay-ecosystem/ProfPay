package com.profpay.wallet.ui.feature.settings.settings
import android.content.SharedPreferences
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.profpay.wallet.bridge.viewmodel.settings.ThemeViewModel
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.shared.getTextValueTheme


@Composable
fun ContentCardForSettingsTheme(
    openThemeChoice: Boolean,
    onOpenThemeChoiceChange: (Boolean) -> Unit,
    themeSharedInt: Int,
    onThemeSelected: (Int) -> Unit,
    themesNames: List<String>,
    shared: SharedPreferences,
    themeVM: ThemeViewModel,
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onOpenThemeChoiceChange(!openThemeChoice) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            text = getTextValueTheme(themeSharedInt = themeSharedInt),
            style = MaterialTheme.typography.bodySmall,
        )
        Icon(
            imageVector = if (openThemeChoice) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
            contentDescription = null,
        )

        DropdownMenu(
            expanded = openThemeChoice,
            onDismissRequest = { onOpenThemeChoiceChange(false) },
        ) {
            themesNames.forEachIndexed { index, title ->
                DropdownMenuItem(
                    onClick = {
                        shared.edit { putInt("valueTheme", index) }
                        themeVM.getThemeApp(shared)
                        onThemeSelected(index)
                    },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    },
                    trailingIcon = {
                        if (themeSharedInt == index) {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                imageVector = Icons.Filled.Check,
                                tint = GreenColor,
                                contentDescription = null,
                            )
                        }
                    },
                )

                if (index != themesNames.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

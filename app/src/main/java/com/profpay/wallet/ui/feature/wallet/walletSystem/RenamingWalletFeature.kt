package com.profpay.wallet.ui.feature.wallet.walletSystem

import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.profpay.wallet.ui.app.theme.PubAddressDark
import com.profpay.wallet.ui.app.theme.transparent

@Composable
fun RenamingWalletFeature(
    walletName: String,
    onClick: (newName: String) -> Unit,
) {
    val maxNameLength = 20
    val disallowedChars = Regex("[\"'\\\\<>;{}()]")

    var newName by remember { mutableStateOf(walletName) }

    Card(
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .shadow(7.dp, RoundedCornerShape(10.dp)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = newName,
                onValueChange = { input ->
                    // Убираем запрещённые символы и ограничиваем длину
                    val cleaned =
                        input
                            .replace(disallowedChars, "")
                            .take(maxNameLength)
                    newName = cleaned
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                placeholder = {
                    Text(
                        text = "Введите новое имя",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PubAddressDark,
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                trailingIcon = {
                    Row(
                        modifier =
                            Modifier
                                .padding(end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            modifier = Modifier.size(25.dp),
                            colors =
                                IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                            onClick = {
                                newName = ""
                            },
                        ) {
                            Icon(
                                modifier = Modifier.fillMaxSize(0.6f),
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "",
                            )
                        }
                        if (newName != walletName && newName != "") {
                            Spacer(modifier = Modifier.size(6.dp))
                            IconButton(
                                modifier = Modifier.size(25.dp),
                                colors =
                                    IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.onPrimary,
                                        contentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                onClick = {
                                    onClick(newName)
                                },
                            ) {
                                Icon(
                                    modifier = Modifier.fillMaxSize(0.6f),
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "",
                                )
                            }
                        }
                    }
                },
                colors =
                    TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = PubAddressDark,
                        focusedIndicatorColor = MaterialTheme.colorScheme.transparent,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.transparent,
                        disabledIndicatorColor = MaterialTheme.colorScheme.transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.transparent,
                        unfocusedContainerColor = MaterialTheme.colorScheme.transparent,
                        cursorColor = MaterialTheme.colorScheme.onBackground,
                        selectionColors =
                            TextSelectionColors(
                                handleColor = MaterialTheme.colorScheme.onBackground,
                                backgroundColor = MaterialTheme.colorScheme.transparent,
                            ),
                    ),
            )
        }
    }
}

package com.profpay.wallet.ui.feature.wallet.tx_details
import StackedSnakbarHostState
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.profpay.wallet.R


@Composable
fun CardTextForTxDetailsFeature(
    modifier: Modifier = Modifier,
    title: String,
    title2: String,
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .padding(top = 4.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
        ),
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 10.dp, horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(
                text = title2,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}


@Composable
fun CardTextForTxDetailsFeature(
    title: String,
    contentText: String?,
    stackedSnackbarHostState: StackedSnakbarHostState,
    isHashTransaction: Boolean = false,
    isDropdownMenu: Boolean = true,
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    var expandedDropdownMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
        modifier = Modifier.padding(bottom = 4.dp, top = 12.dp),
    )
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(vertical = 10.dp)
                    .padding(start = 16.dp, end = 8.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier.weight(0.9f),
                text = contentText ?: "...",
                style = MaterialTheme.typography.bodySmall,
            )

            if (isDropdownMenu) {
                IconButton(
                    modifier = Modifier.size(30.dp),
                    onClick = {
                        expandedDropdownMenu = !expandedDropdownMenu
                    },
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_more_vert),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )

                    DropdownMenu(
                        expanded = expandedDropdownMenu,
                        onDismissRequest = { expandedDropdownMenu = false },
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                clipboardManager.setText(
                                    AnnotatedString(contentText ?: ""),
                                )
                                stackedSnackbarHostState.showSuccessSnackbar(
                                    "Успешное действие",
                                    "Копирование выполнено успешно",
                                    "Закрыть",
                                )
                                expandedDropdownMenu = false
                            },
                            text = { Text("Скопировать") },
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            onClick = {
                                val intent =
                                    Intent(Intent.ACTION_VIEW).apply {
                                        data =
                                            if (isHashTransaction) {
                                                "https://tronscan.org/#/transaction/$contentText".toUri()
                                            } else {
                                                "https://tronscan.org/#/address/$contentText".toUri()
                                            }
                                    }
                                context.startActivity(intent)
                                expandedDropdownMenu = false
                            },
                            text = {
                                Text(
                                    "Перейти в Tron Scan",
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                        )
                    }
                }
            }
        }
    }
}
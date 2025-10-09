package com.profpay.wallet.ui.feature.wallet.walletSystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profpay.wallet.R
import com.profpay.wallet.data.database.dao.wallet.WalletProfileModel
import com.profpay.wallet.ui.app.theme.BackgroundIcon2

@Composable
fun CardForWalletSystemFeature(
    wallet: WalletProfileModel,
    onClick: () -> Unit = {},
    selected: Boolean = false,
) {
    val color =
        if (selected) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.primary

    var expandedDropdownMenu by remember { mutableStateOf(false) }
    val (openControlWallet, setOpenControlWallet) = bottomSheetControlOfTheWallet(wallet = wallet)

    Card(
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .shadow(7.dp, RoundedCornerShape(10.dp)),
        onClick = { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(vertical = 6.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = wallet.name,
                Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
            Column {
                IconButton(onClick = {
                    expandedDropdownMenu = !expandedDropdownMenu
                }) {
                    Icon(
                        modifier = Modifier.size(25.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_more_vert),
                        contentDescription = "",
                        tint = BackgroundIcon2,
                    )
                }
                DropdownMenu(
                    expanded = expandedDropdownMenu,
                    onDismissRequest = { expandedDropdownMenu = false },
                ) {
                    DropdownMenuItem(
                        onClick = {
                            expandedDropdownMenu = false
                            onClick()
                        },
                        text = {
                            Text(
                                "Перейти",
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    DropdownMenuItem(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        onClick = {
                            expandedDropdownMenu = false
                            setOpenControlWallet(true)
                        },
                        text = {
                            Text(
                                "Управлять",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                    )
                }
            }
        }
    }
}

package com.profpay.wallet.ui.feature.wallet.walletSots

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.profpay.wallet.R
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.data.utils.toTokenAmount
import com.profpay.wallet.ui.app.theme.BackgroundIcon2
import java.math.BigInteger


@Composable
internal fun AddressCardForWalletSotsFeature(
    index: Int,
    color: Color,
    addressWithTokens: AddressWithTokens,
    tokenName: String,
    onAddressClick: () -> Unit,
    onReplaceAddressClick: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    val tokenEntity = addressWithTokens.tokens.find {
        it.token.tokenName == tokenName
    }
    val balance = (tokenEntity?.balanceWithoutFrozen ?: BigInteger.ZERO).toTokenAmount()
    val formattedBalance = if (tokenName == "USDT") "$$balance" else "$balance TRX"
    val isAddressCanReplace = addressWithTokens.addressEntity.sotDerivationIndex != 0

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(10.dp),
        onClick = onAddressClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                HexagonIndex(index = index, color = color)
                AddressInfo(
                    address = addressWithTokens.addressEntity.address,
                    balance = formattedBalance
                )
            }

            IconButton(onClick = { expandedMenu = !expandedMenu }) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.icon_more_vert),
                    contentDescription = null,
                    tint = BackgroundIcon2,
                    modifier = Modifier.size(25.dp)
                )
            }

            AddressMenu(
                expanded = expandedMenu,
                onDismiss = { expandedMenu = false },
                onReplaceClick = {
                    onReplaceAddressClick()
                    expandedMenu = false
                },
                canReplace = isAddressCanReplace
            )
        }
    }
}

@Composable
private fun HexagonIndex(index: Int, color: Color) {
    Box(
        modifier = Modifier
            .clip(HexagonShape(true))
            .border(2.dp, color, HexagonShape(true))
            .size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${index + 1}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Composable
private fun AddressInfo(address: String, balance: String) {
    Column(modifier = Modifier.padding(start = 8.dp)) {
        Text(
            text = "${address.take(7)}...${address.takeLast(7)}",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = balance,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun AddressMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onReplaceClick: () -> Unit,
    canReplace: Boolean
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        DropdownMenuItem(
            onClick = {},
            text = { Text("Получить AML", fontWeight = FontWeight.SemiBold) }
        )

        if (canReplace) {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
            DropdownMenuItem(
                onClick = onReplaceClick,
                text = { Text("Заменить адрес", style = MaterialTheme.typography.labelLarge) }
            )
        }
    }
}

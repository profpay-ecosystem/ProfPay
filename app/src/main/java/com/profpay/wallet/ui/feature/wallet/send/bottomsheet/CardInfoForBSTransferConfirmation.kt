package com.profpay.wallet.ui.feature.wallet.send.bottomsheet

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.profpay.wallet.bridge.viewmodel.dto.TokenName
import com.profpay.wallet.ui.app.theme.PubAddressDark
import com.profpay.wallet.ui.shared.utils.formatAddress


@Composable
fun CardInfoForBSTransferConfirmation(
    tokenNameModel: TokenName,
    addressSender: String,
    addressReceiver: String
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)) {
            InfoRow(
                label = "Сеть",
                valueContent = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .padding(end = 4.dp)
                                .size(20.dp)
                                .paint(
                                    painterResource(id = tokenNameModel.paintIconId),
                                    contentScale = ContentScale.FillBounds,
                                ),
                        )
                        Text(text = "${tokenNameModel.blockchainName} (${tokenNameModel.shortName})")
                    }
                }
            )
            InfoRow(
                label = "Откуда",
                value = formatAddress(addressSender)
            )
            InfoRow(
                label = "Куда",
                value = formatAddress(addressReceiver)
            )
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String? = null,
    valueContent: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = PubAddressDark
        )
        if (value != null) {
            Text(text = value, style = MaterialTheme.typography.bodyMedium)
        } else {
            valueContent()
        }
    }
}

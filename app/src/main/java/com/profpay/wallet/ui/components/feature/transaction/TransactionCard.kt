package com.profpay.wallet.ui.components.feature.transaction

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp


@Composable
fun TransactionCard(
    title: String,
    details: String,
    amount: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    extraContent: @Composable (() -> Unit) = {},
) {
    Card(
        modifier = modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
            .shadow(7.dp, RoundedCornerShape(10.dp)),
        onClick = { onClick() },
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.padding(start = 10.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .paint(
                                painterResource(id = iconRes),
                                contentScale = ContentScale.FillBounds,
                            )
                    )
                    Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                        Text(text = title, style = MaterialTheme.typography.bodyLarge)
                        Text(text = details, style = MaterialTheme.typography.labelLarge)
                    }
                }
                Text(
                    text = amount,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(end = 12.dp),
                )
            }

            extraContent()
        }
    }
}

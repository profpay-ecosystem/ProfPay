package com.profpay.wallet.ui.feature.settings.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp


@Composable
fun CardForSettings(
    onClick: () -> Unit = {},
    iconID: Int,
    label: String,
    smallLabel: Boolean = false,
    noClick: Boolean = false,
    iconSize: Int = 40,
    content: @Composable () -> Unit = {},
) {
    @Composable
    fun contentThis() {
        Row(
            modifier =
                Modifier
                    .padding(vertical = 6.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier =
                    Modifier
                        .padding(start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        modifier =
                            Modifier
                                .size(iconSize.dp),
                        imageVector = ImageVector.vectorResource(id = iconID),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = label,
                    style = if (smallLabel) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
                )
            }
            content()
        }
    }

    if (noClick) {
        Card(
            modifier =
                Modifier
                    .padding(top = 4.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .shadow(7.dp, RoundedCornerShape(10.dp)),
        ) {
            contentThis()
        }
    } else {
        Card(
            modifier =
                Modifier
                    .padding(top = 4.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .shadow(7.dp, RoundedCornerShape(10.dp)),
            onClick = { onClick() },
        ) {
            contentThis()
        }
    }
}

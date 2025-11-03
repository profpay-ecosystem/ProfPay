package com.profpay.wallet.ui.components.feature.settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp


@Composable
fun CardWithTextForSettings(
    label: String,
    noClick: Boolean = true,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    @Composable
    fun contentThis() {
        Row(
            modifier =
                Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
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

package com.profpay.wallet.ui.feature.settings.account

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp


@Composable
fun DescriptionCardSettingsAccountFeature() {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp, bottom = 24.dp)
            .fillMaxWidth()
            .shadow(7.dp, RoundedCornerShape(10.dp)),
    ) {
        Text(
            text = "Данный раздел настроек необходим для привязки Telegram " +
                "Account и получения информации о привязанных аккаунтах",
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

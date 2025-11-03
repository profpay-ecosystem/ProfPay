package com.profpay.wallet.ui.feature.settings.account

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp


@Composable
fun TelegramCardSettingsAccountFeature(
    tgId: Long?,
    tgUsername: String?,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .shadow(7.dp, RoundedCornerShape(10.dp)),
    ) {
        Column(Modifier.padding(16.dp)) {
            if (tgId != null && tgId != 0L) {
                RowSettingsAccountFeature(label = "Telegram ID:", info = "$tgId")
                RowSettingsAccountFeature(label = "Username:", info = "@$tgUsername")
                UnlinkTelegramAccountFeature()
            } else {
                LinkTelegramAccountFeature()
            }
        }
    }
}

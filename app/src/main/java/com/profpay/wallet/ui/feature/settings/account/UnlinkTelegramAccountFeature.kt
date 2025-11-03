package com.profpay.wallet.ui.feature.settings.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun UnlinkTelegramAccountFeature() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 6.dp),
    ) {
        Row(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable {
                        /*TODO: отвязка Тг*/
                    },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Отвязать Telegram аккаунт",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp, end = 2.dp),
            )
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "",
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

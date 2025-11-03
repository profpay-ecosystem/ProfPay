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
fun ProfPayCardSettingsAccountFeature(
    userId: Long?,
    appId: String?,
    status: String,
) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(top = 4.dp,bottom = 14.dp)
            .fillMaxWidth()
            .shadow(7.dp, RoundedCornerShape(10.dp)),
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp),) {
            RowSettingsAccountFeature(label = "UNID:", info = userId?.toString() ?: "-")
            RowSettingsAccountFeature(label = "APP ID:", info = appId ?: "", isInfoShorted = true)
            RowSettingsAccountFeature(label = "Status:", info = status)
        }
    }
}

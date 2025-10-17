package com.profpay.wallet.ui.feature.wallet.send

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.profpay.wallet.ui.app.theme.BackgroundContainerButtonLight
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.extensions.protectFromTapjacking

@Composable
fun ButtonSendFeature(
    onClick: () -> Unit,
    isButtonEnabled: Boolean,
) {
    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.Bottom,
    ) {
        Button(
            onClick = {
                onClick()
            },
            enabled = isButtonEnabled,
            modifier =
                Modifier
                    .protectFromTapjacking()
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(vertical = 8.dp, horizontal = 4.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = GreenColor,
                    contentColor = BackgroundContainerButtonLight,
                ),
            shape = RoundedCornerShape(12.dp),
        ) {
            Text(
                text = "Перевести",
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            )
        }
    }
}

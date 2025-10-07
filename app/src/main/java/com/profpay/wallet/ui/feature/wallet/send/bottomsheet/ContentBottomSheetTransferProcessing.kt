package com.profpay.wallet.ui.feature.wallet.send.bottomsheet
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.profpay.wallet.ui.app.theme.BackgroundLight
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.app.theme.ProgressIndicator


@Composable
fun ContentBottomSheetTransferProcessing(onClick: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(vertical = 0.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator(
            modifier =
                Modifier
                    .padding(top = 40.dp, bottom = 20.dp)
                    .size(50.dp),
            color = ProgressIndicator,
        )

        Text(
            "Перевод обрабатывается",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )

        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .padding(top = 40.dp)
                    .padding(vertical = 8.dp, horizontal = 8.dp),
            verticalArrangement = Arrangement.Bottom,
        ) {
            Button(
                onClick = {
                    onClick()
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(horizontal = 4.dp),
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = GreenColor,
                        contentColor = BackgroundLight,
                    ),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = "Детали Перевода",
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

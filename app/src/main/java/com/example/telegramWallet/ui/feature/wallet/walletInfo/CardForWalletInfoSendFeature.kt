package com.example.telegramWallet.ui.feature.wallet.walletInfo
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun CardForWalletInfoSendFeature(
    onClick: () -> Unit,
    paintIconId: Int,
    label: String,
) {
    Card(
        modifier =
            Modifier
                .padding(vertical = 0.dp)
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        onClick = { onClick() },
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp)
                        .weight(0.5f)
                        .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier =
                            Modifier
                                .size(40.dp)
                                .paint(
                                    painterResource(id = paintIconId),
                                    contentScale = ContentScale.FillBounds,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {}
                    Column(modifier = Modifier.padding(horizontal = 12.dp, 4.dp)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                }
            }
//            Column(
//                modifier = Modifier
//                    .padding(horizontal = 16.dp)
//                    .weight(0.5f)
//                    .fillMaxHeight(),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.End
//            ) {
//                Text(text = "${balance}", fontWeight = FontWeight.SemiBold)
//            }
        }
    }
}

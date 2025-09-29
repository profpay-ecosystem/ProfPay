package com.example.telegramWallet.ui.feature.smartList.bottomSheets
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegramWallet.ui.app.theme.ProgressIndicator

@Composable
fun ProgressIndicatorSmartModalFeature(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(0.05f))
        CircularProgressIndicator(
            modifier = Modifier.weight(0.1f),
            color = ProgressIndicator,
        )
        Spacer(modifier = Modifier.weight(0.05f))
        Text(
            text = text,
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.height(IntrinsicSize.Min).padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.weight(0.05f))
    }
}

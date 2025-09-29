package com.profpay.wallet.ui.feature.smartList

import android.content.Intent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.R
import com.profpay.wallet.bridge.view_model.smart_contract.GetSmartContractViewModel
import kotlinx.coroutines.launch

@Composable
fun IsEmptyListSmartContract(viewModel: GetSmartContractViewModel = hiltViewModel()) {
    var scale by remember { mutableFloatStateOf(1f) }
    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        while (true) {
            animate(
                initialValue = 1f,
                targetValue = 1.15f,
                animationSpec =
                    infiniteRepeatable(
                        animation = tween(durationMillis = 1000),
                        repeatMode = RepeatMode.Reverse,
                    ),
            ) { value, _ ->
                scale = value
            }
        }
    }

    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Card(
            shape = CircleShape,
            modifier = Modifier.size(230.dp),
            elevation = CardDefaults.cardElevation(10.dp),
            onClick = {
                coroutineScope.launch {
                    val appId = viewModel.profileRepo.getProfileAppId()
                    val intent =
                        Intent(Intent.ACTION_VIEW).apply {
                            data = "https://t.me/bb_list_bot?start=wallet_$appId".toUri()
                        }
                    context.startActivity(intent)
                }
            },
        ) {
            Box(
                modifier =
                    Modifier
                        .padding(end = 30.dp)
                        .clip(CircleShape)
                        .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.icon_telegram),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier =
                        Modifier
                            .size(150.dp)
                            .scale(scale),
                )
            }
        }
        Text(
            text =
                "Сделок пока нет.\n" +
                    "Их можно создать в телеграм-боте",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(30.dp),
        )
    }
}

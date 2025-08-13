package com.example.telegramWallet.ui.screens.createOrRecoveryWallet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.telegramWallet.PrefKeys
import com.example.telegramWallet.R
import com.example.telegramWallet.ui.app.theme.BackgroundDark
import com.example.telegramWallet.ui.app.theme.BackgroundLight
import com.example.telegramWallet.ui.shared.sharedPref

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrRecoverWalletScreen(
    goToCreateNewWallet: () -> Unit,
    goToRecoverWallet: () -> Unit,
    goToBack: () -> Unit
) {
    var title by remember { mutableStateOf("Добавить кошелёк") }
    val isFirstStart = sharedPref().getBoolean(PrefKeys.FIRST_STARTED, true)
    val isGrpcStatus = sharedPref().getBoolean("IS_GRPC_ERROR", false)

    if (isFirstStart) {
        title = "Добро пожаловать в Wallet"
    }
    Scaffold(topBar = {
        TopAppBar(
            title = {},
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            ),
            navigationIcon = {
                run {
                    if (!isFirstStart)
                        IconButton(onClick = { goToBack() }) {
                            Icon(
                                modifier = Modifier.size(35.dp),
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                }
            }
        )
    }
    ) { padding ->
        padding
        TitleCreateOrRecoveryWalletFeature(title = title, bottomContent = {}) {
            Column(
                modifier = Modifier.fillMaxHeight(0.7f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(
                    modifier = Modifier
                        .fillMaxHeight(0.1f)
                )
                CardCreateOrRecoveryWalletFeature(
                    goTo = { goToCreateNewWallet() },
                    title = "Создать новый кошелёк",
                    description = "если не пользовались кошельками TRC-20",
                    iconId = R.drawable.create_wallet_img
                )
                CardCreateOrRecoveryWalletFeature(
                    goTo = { goToRecoverWallet() },
                    title = "Восстановить кошелёк",
                    description = "если есть seed-фраза или приватный ключ",
                    iconId = R.drawable.recovery_wallet_img
                )
            }
        }
    }
}

@Composable
fun TitleCreateOrRecoveryWalletFeature(
    title: String,
    bottomContent: @Composable () -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painterResource(id = R.drawable.create_recovery_bg),
                contentScale = ContentScale.FillBounds
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(
                modifier = Modifier
                    .weight(0.5f)
                    .fillMaxHeight(0.1f)
            )
            Icon(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxSize(0.5f),
                imageVector = ImageVector.vectorResource(id = R.drawable.icon_smart),
                contentDescription = "", tint = BackgroundLight
            )
            Spacer(
                modifier = Modifier
                    .weight(0.1f)
                    .fillMaxHeight(0.1f)
            )
            Text(
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(0.8f)
                    .height(IntrinsicSize.Max),
                text = title,
                style = MaterialTheme.typography.headlineSmall, color = BackgroundLight
            )

            Column(modifier = Modifier.weight(5.5f)) {
                content()
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.97f),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.height(IntrinsicSize.Min)
        ) {
            bottomContent()
            Text(
                text = "ProfPay",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.ExtraBold),
                color = BackgroundDark
            )
            Text(
                text = "ProfPay IO, 2024",
                style = MaterialTheme.typography.titleSmall,
                color = BackgroundDark
            )
        }
    }
}

@Composable
fun CardCreateOrRecoveryWalletFeature(
    goTo: () -> Unit,
    title: String,
    description: String,
    iconId: Int
) {
    val isGrpcStatus = sharedPref().getBoolean("IS_GRPC_ERROR", false)

    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .height(70.dp),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        onClick = { goTo() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .padding(18.dp)
                        .paint(painterResource(id = iconId)),
                    contentAlignment = Alignment.Center
                ) {}
                Column(modifier = Modifier) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.DarkGray
                    )
                }
            }
        }
    }
}
package com.example.telegramWallet.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.example.telegramWallet.R
import com.example.telegramWallet.ui.app.theme.BackgroundIcon
import com.example.telegramWallet.ui.app.theme.DarkBlue
import com.example.telegramWallet.ui.app.theme.SwitchColor
import com.example.telegramWallet.ui.shared.sharedPref

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsNotificationsScreen(goToBack: () -> Unit) {
    var checkIsTest by remember { mutableStateOf(false) }

    val (checkNotificationsInWallet, setCheckNotificationsInWallet) =
        remember {
            mutableStateOf(
                false,
            )
        }
    val bottomPadding = sharedPref().getFloat("bottomPadding", 54f)

    Scaffold(modifier = Modifier) { padding ->
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .paint(
                        painterResource(id = R.drawable.wallet_background),
                        contentScale = ContentScale.FillBounds,
                    ),
            verticalArrangement = Arrangement.Bottom,
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings notifications",
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                navigationIcon = {
                    run {
                        IconButton(onClick = { goToBack() }) {
                            Icon(
                                modifier = Modifier.size(34.dp),
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = Color.White,
                            )
                        }
                    }
                },
                actions = {
                    run {
                        IconButton(onClick = { /*goToBack()*/ }) {
                            Icon(
                                modifier = Modifier.size(24.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_alert),
                                contentDescription = "",
                                tint = Color.White,
                            )
                        }
                    }
                },
            )

            Card(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(
                            RoundedCornerShape(
                                topStart = 20.dp,
                                topEnd = 20.dp,
                                bottomEnd = 0.dp,
                                bottomStart = 0.dp,
                            ),
                        ).weight(0.8f),
            ) {
                Column(
                    modifier =
                        Modifier
                            .padding(bottom = bottomPadding.dp)
                            .padding(vertical = 0.dp, horizontal = 0.dp)
                            .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Spacer(modifier = Modifier.size(16.dp))
                    CardWithText(label = "Уведомления в Telegram") {
                        setCheckNotificationsInWallet(
                            switchForSettings(checkNotificationsInWallet) {
                                // Todo: переменные для изменеия
                                checkIsTest = it
                            },
                        )
                    }
                    CardWithText(label = "Уведомления в приложении") {
                        setCheckNotificationsInWallet(
                            switchForSettings(checkNotificationsInWallet) {
                                // Todo: переменные для изменеия
                                checkIsTest = it
                            },
                        )
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
fun switchForSettings(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
): Boolean {
    var checkIs by remember { mutableStateOf(checked) }
    Switch(
        modifier = Modifier.padding(end = 8.dp),
        checked = checkIs,
        onCheckedChange = {
            checkIs = it
            onCheckedChange(it)
        },
        colors =
            SwitchDefaults.colors(
                checkedThumbColor = DarkBlue,
                checkedTrackColor = SwitchColor,
                checkedBorderColor = MaterialTheme.colorScheme.primary,
                uncheckedThumbColor = MaterialTheme.colorScheme.primary,
                uncheckedTrackColor = BackgroundIcon,
                uncheckedBorderColor = MaterialTheme.colorScheme.primary,
            ),
    )
    return checkIs
}

@Composable
fun CardWithText(
    label: String,
    noClick: Boolean = true,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    @Composable
    fun contentThis() {
        Row(
            modifier =
                Modifier
                    .padding(vertical = 4.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = label,
                style = MaterialTheme.typography.bodyLarge,
            )
            content()
        }
    }

    if (noClick) {
        Card(
            modifier =
                Modifier
                    .padding(top = 4.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .shadow(7.dp, RoundedCornerShape(10.dp)),
        ) {
            contentThis()
        }
    } else {
        Card(
            modifier =
                Modifier
                    .padding(top = 4.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth()
                    .shadow(7.dp, RoundedCornerShape(10.dp)),
            onClick = { onClick() },
        ) {
            contentThis()
        }
    }
}

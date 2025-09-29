package com.example.telegramWallet.ui.screens.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.example.telegramWallet.R
import com.example.telegramWallet.bridge.view_model.settings.SettingsAccountViewModel
import com.example.telegramWallet.ui.shared.sharedPref
import com.example.telegramWallet.ui.widgets.SettingsBotWidget
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAccountScreen(
    goToBack: () -> Unit,
    viewModel: SettingsAccountViewModel = hiltViewModel(),
) {
    val tgId by viewModel.profileTelegramId.observeAsState()
    val tgUsername by viewModel.profileTelegramUsername.observeAsState()

    val userId by produceState<Long?>(initialValue = null) {
        value = viewModel.getProfileUserId()
    }

    val appId by produceState<String?>(initialValue = null) {
        value = viewModel.getProfileAppId()
    }

    LaunchedEffect(Unit) {
        viewModel.viewModelScope.launch {
            viewModel.getUserTelegramData()
        }
    }

    val bottomPadding = sharedPref().getFloat("bottomPadding", 54f)
    Scaffold { padding ->
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
                        text = "Account",
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
                        .padding(bottom = 8.dp)
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
                    Card(
                        modifier =
                            Modifier
                                .padding(top = 4.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
                                .fillMaxWidth()
                                .shadow(7.dp, RoundedCornerShape(10.dp)),
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .padding(vertical = 8.dp)
                                    .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text =
                                    "Данный раздел настроек необходим для привязки Telegram " +
                                        "Account и получения информации о привязанных аккаунтах",
                                modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 18.dp),
                    ) {
                        Text(text = "Telegram", style = MaterialTheme.typography.titleMedium)
                    }
                    Card(
                        modifier =
                            Modifier
                                .padding(top = 4.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
                                .fillMaxWidth()
                                .shadow(7.dp, RoundedCornerShape(10.dp)),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                        ) {
                            if (tgId != null && tgId != 0L) {
                                RowSettingsAccountFeature(
                                    label = "Telegram ID:",
                                    info = "$tgId",
                                )
                                RowSettingsAccountFeature(
                                    label = "Username:",
                                    info = "@$tgUsername",
                                )
                            } else {
                                Text(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(start = 0.dp, top = 10.dp),
                                    text = "Вы не авторизовались через Telegram",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Row(
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .padding(start = 0.dp, top = 12.dp, bottom = 6.dp),
                                ) {
                                    SettingsBotWidget()
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                    Row(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp, horizontal = 18.dp),
                    ) {
                        Text(text = "ProfPay", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Card(
                        modifier =
                            Modifier
                                .padding(top = 4.dp, bottom = 4.dp, start = 16.dp, end = 16.dp)
                                .fillMaxWidth()
                                .shadow(7.dp, RoundedCornerShape(10.dp)),
                    ) {
                        Column(
                            modifier =
                                Modifier
                                    .padding(top = 8.dp, bottom = 8.dp, start = 16.dp, end = 16.dp),
                        ) {
                            RowSettingsAccountFeature(label = "UNID:", info = "$userId")
                            RowSettingsAccountFeature(
                                label = "APP ID:",
                                info = appId ?: "",
                                byInfoShorted = true,
                            )
                            RowSettingsAccountFeature(label = "Status:", info = "User")
                        }
                    }
                    Spacer(modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}

@Composable
fun RowSettingsAccountFeature(
    label: String,
    info: String,
    byInfoShorted: Boolean = false,
) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val infoForUI =
        if (byInfoShorted) {
            if (info.length > 12) {
                "${info.take(6)}...${info.takeLast(6)}"
            } else {
                info
            }
        } else {
            info
        }

    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(0.45f),
            text = label,
            style = MaterialTheme.typography.bodyLarge,
        )

        Row(
            modifier =
                Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .clickable { clipboardManager.setText(AnnotatedString(info)) },
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier =
                    Modifier
                        .padding(horizontal = 4.dp, vertical = 8.dp)
                        .weight(0.4f),
                text = infoForUI,
                style = MaterialTheme.typography.bodySmall,
            )
            Icon(
                modifier =
                    Modifier
                        .padding(4.dp)
                        .weight(0.05f),
                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                contentDescription = "",
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

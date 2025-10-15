package com.profpay.wallet.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.R
import com.profpay.wallet.bridge.viewmodel.settings.SettingsViewModel
import com.profpay.wallet.bridge.viewmodel.settings.ThemeViewModel
import com.profpay.wallet.ui.app.theme.GreenColor
import com.profpay.wallet.ui.shared.getTextValueTheme
import com.profpay.wallet.ui.shared.sharedPref
import rememberStackedSnackbarHostState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SettingsScreen(
    themeVM: ThemeViewModel = hiltViewModel(),
    viewModel: SettingsViewModel = hiltViewModel(),
    goToLockGraph: () -> Unit,
    goToTheme: () -> Unit,
    goToSettingsNotifications: () -> Unit,
    goToSettingsAccount: () -> Unit,
    goToSettingsSecurity: () -> Unit,
    goToSettingsAml: () -> Unit,
) {
    val snackbarHostState = rememberStackedSnackbarHostState()

    var openThemeChoice by remember { mutableStateOf(false) }

    val shared = sharedPref()
    var themeSharedInt by remember { mutableIntStateOf(shared.getInt("valueTheme", 2)) }

    val isHiddenB: Boolean = shared.getBoolean(stringResource(R.string.IS_HIDDEN_BALANCES), false)
    val checkHiddenAllBalances = remember { mutableStateOf(isHiddenB) }

    val isTestNet: Boolean = shared.getBoolean(stringResource(R.string.IS_TEST_NETWORK), false)
    val checkIsTestNet = remember { mutableStateOf(isTestNet) }

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
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                navigationIcon = {
//                    run {
//                        IconButton(onClick = { goToBack() }) {
//                            Icon(
//                                modifier = Modifier.size(34.dp),
//                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
//                                contentDescription = "Back",
//                                tint = Color.White
//                            )
//                        }
//                    }
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

                    CardForSettings(
                        iconID = R.drawable.icon_settings_account,
                        label = "Аккаунт",
                        iconSize = 24,
                        onClick = { goToSettingsAccount() },
                    )
                    CardForSettings(
                        iconID = R.drawable.icon_settings_aml,
                        label = "AML",
                        iconSize = 24,
                        onClick = { goToSettingsAml() },
                    )
                    CardForSettings(
                        iconID = R.drawable.icon_settings_theme,
                        label = "Тема",
                        noClick = true,
                    ) {
                        Row(
                            modifier =
                                Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { openThemeChoice = !openThemeChoice }
                                    .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Text(
                                text = getTextValueTheme(themeSharedInt = themeSharedInt),
                                style = MaterialTheme.typography.bodySmall,
                            )
                            if (openThemeChoice) {
                                Icon(
                                    Icons.Filled.KeyboardArrowUp,
                                    contentDescription = "Вниз",
                                )
                            } else {
                                Icon(
                                    Icons.Filled.KeyboardArrowDown,
                                    contentDescription = "Вверх",
                                )
                            }
                            DropdownMenu(
                                expanded = openThemeChoice,
                                onDismissRequest = { openThemeChoice = false },
                            ) {
                                DropdownMenuItem(
                                    onClick = {
                                        shared.edit { putInt("valueTheme", 0) }
                                        themeVM.getThemeApp(shared)
                                        themeSharedInt = 0
                                    },
                                    text = {
                                        Text(
                                            "Светлая",
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    },
                                    trailingIcon = {
                                        if (themeSharedInt == 0) {
                                            Icon(
                                                modifier = Modifier.size(20.dp),
                                                imageVector = Icons.Filled.Check,
                                                tint = GreenColor,
                                                contentDescription = "",
                                            )
                                        }
                                    },
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        shared.edit { putInt("valueTheme", 1) }
                                        themeVM.getThemeApp(shared)
                                        themeSharedInt = 1
                                    },
                                    text = {
                                        Text(
                                            "Тёмная",
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    },
                                    trailingIcon = {
                                        if (themeSharedInt == 1) {
                                            Icon(
                                                modifier = Modifier.size(20.dp),
                                                imageVector = Icons.Filled.Check,
                                                tint = GreenColor,
                                                contentDescription = "",
                                            )
                                        }
                                    },
                                )
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 8.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                                DropdownMenuItem(
                                    onClick = {
                                        shared.edit { putInt("valueTheme", 2) }
                                        themeVM.getThemeApp(shared)
                                        themeSharedInt = 2
                                    },
                                    text = {
                                        Text(
                                            "Системная",
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    },
                                    trailingIcon = {
                                        if (themeSharedInt == 2) {
                                            Icon(
                                                modifier = Modifier.size(20.dp),
                                                imageVector = Icons.Filled.Check,
                                                tint = GreenColor,
                                                contentDescription = "",
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }

                    CardForSettings(
                        onClick = { goToSettingsNotifications() },
                        iconID = R.drawable.icon_settings_alert,
                        label = "Уведомления",
                    )
                    CardForSettings(
                        onClick = {},
                        iconID = R.drawable.icon_settings_dollar,
                        label = "Валюта",
                    ) {
                        Text(
                            "USD",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                    CardForSettings(
                        onClick = {},
                        iconID = R.drawable.icon_settings_support,
                        label = "Поддержка",
                    )
                    CardForSettings(
                        onClick = { goToSettingsSecurity() },
                        iconID = R.drawable.icon_settings_security,
                        label = "Безопасность",
                    )
                    CardForSettings(
                        onClick = {},
                        iconID = R.drawable.icon_settings_faq,
                        label = "FAQ",
                    )
                    CardForSettings(
                        onClick = {},
                        iconID = R.drawable.icon_settings_privacy_policy,
                        label = "Политика \nКонфиденциальности",
                        smallLabel = true,
                    )

//
//                        RowSettingsButtonHiddenWithoutDivider(
//                            textContent = "Скрыть все балансы",
//                            modifier = Modifier,
//                            funcRight = {
//                                Switch(
//                                    checked = checkHiddenAllBalances.value,
//                                    onCheckedChange = {
//                                        checkHiddenAllBalances.value = it
//                                        shared.edit().putBoolean("isHiddenBalances", it).apply()
//                                    },
//                                    colors = SwitchDefaults.colors(
//                                        checkedThumbColor = MaterialTheme.colorScheme.primaryContainer,
//                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
//                                        checkedBorderColor = MaterialTheme.colorScheme.background,
//                                        uncheckedThumbColor = MaterialTheme.colorScheme.onSecondary,
//                                        uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
//                                        uncheckedBorderColor = MaterialTheme.colorScheme.background
//                                    )
//                                )
//                            }
//                        )
//                        RowSettingsButtonHiddenWithoutDivider(
//                            textContent = "Тестовая Nile-сеть",
//                            modifier = Modifier,
//                            funcRight = {
//                                Switch(
//                                    checked = checkIsTestNet.value,
//                                    onCheckedChange = {
//                                        checkIsTestNet.value = it
//                                        shared.edit().putBoolean("isTestNetwork", it).apply()
//                                    },
//                                    colors = SwitchDefaults.colors(
//                                        checkedThumbColor = MaterialTheme.colorScheme.primaryContainer,
//                                        checkedTrackColor = MaterialTheme.colorScheme.primary,
//                                        checkedBorderColor = MaterialTheme.colorScheme.background,
//                                        uncheckedThumbColor = MaterialTheme.colorScheme.onSecondary,
//                                        uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
//                                        uncheckedBorderColor = MaterialTheme.colorScheme.background
//                                    )
//                                )
//                            }
//                        )
//
//
//                        RowSettingsButtonHiddenWithoutDivider(
//                            textContent = "Сменить пин-код",
//                            funcRight = {
//                                Icon(
//                                    Icons.Filled.KeyboardArrowRight,
//                                    contentDescription = "Вправо"
//                                )
//                            },
//                            modifier = Modifier.clickable(onClick = {
//                                goToLockGraph()
//                            })
//                        )
                    Spacer(modifier = Modifier.size(10.dp))
                }
            }
        }
    }
}

@Composable
fun CardForSettings(
    onClick: () -> Unit = {},
    iconID: Int,
    label: String,
    smallLabel: Boolean = false,
    noClick: Boolean = false,
    iconSize: Int = 40,
    content: @Composable () -> Unit = {},
) {
    @Composable
    fun contentThis() {
        Row(
            modifier =
                Modifier
                    .padding(vertical = 6.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier =
                    Modifier
                        .padding(start = 8.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimary),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        modifier =
                            Modifier
                                .size(iconSize.dp),
                        imageVector = ImageVector.vectorResource(id = iconID),
                        contentDescription = "",
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    text = label,
                    style = if (smallLabel) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyLarge,
                )
            }
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

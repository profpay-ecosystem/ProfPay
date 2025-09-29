package com.example.telegramWallet.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.telegramWallet.PrefKeys.AUTO_CHECK_AML
import com.example.telegramWallet.R
import com.example.telegramWallet.ui.shared.sharedPref

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAmlScreen(goToBack: () -> Unit) {
    val sharedPref = sharedPref()

    val (useAutoCheckAml, setUseAutoCheckAml) =
        remember {
            mutableStateOf(
                sharedPref.getBoolean(AUTO_CHECK_AML, true),
            )
        }

    val bottomPadding = sharedPref.getFloat("bottomPadding", 54f)

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
                        text = "AML",
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
                    CardWithText(label = "Авто. проверка AML") {
                        setUseAutoCheckAml(
                            switchForSettings(useAutoCheckAml) {
                                sharedPref.edit { putBoolean(AUTO_CHECK_AML, it) }
                            },
                        )
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

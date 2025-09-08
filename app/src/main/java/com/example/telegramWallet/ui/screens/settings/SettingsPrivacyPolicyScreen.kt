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
import com.example.telegramWallet.R
import com.example.telegramWallet.ui.shared.sharedPref

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPrivacyPolicyScreen(goToBack: () -> Unit) {

    val bottomPadding = sharedPref().getFloat("bottomPadding", 54f)
    val sectionsTextPrivacyPolicy = listOf(
        PolicySection(
            "1. Общие положения\n\n",
            "Настоящая Политика конфиденциальности определяет порядок обработки и защиты персональных данных пользователей Android-приложения [название приложения] (далее — «Приложение»).\n" +
                    "Используя Приложение, пользователь подтверждает согласие с условиями настоящей Политики.\n\n"
        ),
        PolicySection(
            "2. Обрабатываемые данные\n\n",
            "Мы обрабатываем и храним следующие данные:\n\n" +
                    "   Историю транзакций, совершённых пользователем через Приложение;\n\n" +
                    "   AML-отчёты, связанные с транзакциями;\n\n" +
                    "   Публичные ключи криптовалютных адресов (не приватные ключи);\n\n" +
                    "   Username и идентификатор Telegram-аккаунта;\n\n" +
                    "   Технические отчёты об ошибках (включая IP-адрес устройства и параметры системы), собираемые через self-hosted Sentry.\n\n"
        ),
        PolicySection(
            "3. Цели обработки данных\n\n",
            "Мы используем данные исключительно для:\n\n" +
                    "   Предоставления доступа к функционалу Приложения;\n\n" +
                    "   Технической поддержки пользователей и исправления ошибок.\n\n"
        ),
        PolicySection(
            "4. Хранение и защита данных\n\n",
            "Все данные хранятся на наших серверах и не передаются третьим лицам, за исключением случаев, прямо предусмотренных законодательством.\n\n" +
                    "Мы применяем технические и организационные меры для защиты данных от несанкционированного доступа, изменения, раскрытия или уничтожения.\n\n"
        ),
        PolicySection(
            "5. Права пользователя\n\n",
            "Пользователь имеет право:\n\n" +
                    "   Запросить копию своих данных;\n\n" +
                    "   Запросить удаление своих данных, если это не противоречит обязательным требованиям законодательства (например, в части AML).\n\n" +
                    "   Для реализации прав пользователь может связаться с нами через Telegram-бота или иные доступные каналы поддержки.\n\n"
        ),
        PolicySection(
            "6. Изменения политики\n\n",
            "Мы можем время от времени обновлять настоящую Политику. Обновлённая версия будет доступна внутри Приложения."
        ),
    )

    Scaffold(modifier = Modifier) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .paint(
                    painterResource(id = R.drawable.wallet_background),
                    contentScale = ContentScale.FillBounds
                ), verticalArrangement = Arrangement.Bottom
        ) {
            TopAppBar(
                title = {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color.White)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                navigationIcon = {
                    run {
                        IconButton(onClick = { goToBack() }) {
                            Icon(
                                modifier = Modifier.size(34.dp),
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = Color.White
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
                                tint = Color.White
                            )
                        }
                    }
                }
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 20.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 0.dp
                        )
                    )
                    .weight(0.8f),
            ) {
                Column(
                    modifier = Modifier
                        .padding(bottom = bottomPadding.dp)
                        .padding(vertical = 0.dp, horizontal = 16.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    sectionsTextPrivacyPolicy.forEach { section ->
                        TextForPrivacyPolicy(isTitle = true, text = section.title)
                        TextForPrivacyPolicy(text = section.text)
                    }
                    Spacer(modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

data class PolicySection(val title: String, val text: String)

@Composable
fun TextForPrivacyPolicy(isTitle: Boolean = false, text: String) {
    if (isTitle) {
        Text(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp)
//                .padding(start = 8.dp)
            ,
            text = text,
            style = MaterialTheme.typography.bodyLarge,
        )
    } else {
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = text,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}
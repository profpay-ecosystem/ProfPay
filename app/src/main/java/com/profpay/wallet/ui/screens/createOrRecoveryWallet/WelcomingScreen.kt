package com.profpay.wallet.ui.screens.createOrRecoveryWallet

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.R
import com.profpay.wallet.bridge.viewmodel.welcoming.WelcomingViewModel
import com.profpay.wallet.ui.app.navigation.graphs.Graph
import com.profpay.wallet.ui.app.navigation.graphs.navGraph.OnboardingScreen
import com.profpay.wallet.ui.app.theme.BackgroundDark
import com.profpay.wallet.ui.app.theme.BackgroundLight
import com.profpay.wallet.ui.shared.sharedPref

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomingScreen(
    toNavigate: (String) -> Unit,
    viewModel: WelcomingViewModel = hiltViewModel(),
) {
    val sharedPref = sharedPref() //
    val isFirstStart = sharedPref.getBoolean(PrefKeys.FIRST_STARTED, true)

    val text =
        "СОГЛАШЕНИЕ ДЛЯ ПОЛЬЗОВАТЕЛЕЙ " +
            "КРИПТОКОШЕЛЬКА \n\n" +
            "Настоящее соглашение (далее - «Соглашение») " +
            "регулирует условия использования криптокошелька " +
            "(далее - «Кошелёк»), предоставляемого пользователям " +
            "далее - «Пользователи»). \n\n" +
            "ОБЩИЕ ПОЛОЖЕНИЯ\n\n" +
            "1.1. Используя Кошелёк, Пользователь соглашается " +
            "соблюдать условия данного Соглашения. \n" +
            "1.2. Настоящее Соглашение может быть изменено " +
            "Администрацией в любое время. Пользователь " +
            "обязуется регулярно просматривать Соглашение на " +
            "наличие изменений и правок. Использование Кошелька " +
            "после обновления Соглашения рассматривается как " +
            "согласие с новыми условиями. \n" +
            "1.3. Пользователь подтверждает, что является " +
            "дееспособным и его намерения заключить сделку не " +
            "ограничены судебными решениями или предписаниями " +
            "государственных органов. \n\n" +
            "ОГРАНИЧЕНИЯ НА ИСПОЛЬЗОВАНИЕ \n\n" +
            "2.1. Использование Кошелька запрещено следующим " +
            "лицам: \n" +
            "Гражданам или резидентам Соединенных Штатов " +
            "Америки и их территорий, включая Американское " +
            "Самоа, Гуам, Северные Марианские острова, Пуэрто-" +
            "Рико и Виргинские острова США." +
            "Лицам, находящимся под санкциями или гражданам и резидентам стран, находящихся под " +
            "санкциями, включая Бурунди, " +
            "Центральноафриканскую республику, Кубу, Крым, " +
            "Иран, Ирак, Ливан, Ливию, Северную Корею, " +
            "С0мали, Южный Судан, Дарфур (Судан), Сирию, " +
            "Венесуэлу, Йемен, Конго, Зимбабве. " +
            "Лицам, проживающим на оккупированных " +
            "территориях Украины. \n" +
            "2.2. Пользователь подтверждает, что он не является " +
            "гражданином или резидентом указанных стран и " +
            "территорий и не действует в их интересах. \n" +
            " 2.3. Пользователь несет персональную ответственность " +
            "за любые последствия использования VРN или иных " +
            "средств для обхода указанных ограничений. \n\n" +
            "ОТВЕТСТВЕННОСТЬ ПОЛЬЗОВАТЕЛЯ \n\n" +
            "3.1. Пользователь несет ответственность за " +
            "сохранность личных данных, приватных ключей, " +
            "паролей и иных средств доступа к Кошельку. \n" +
            "3.2. Пользователь обязуется использовать Кошелёк " +
            "только для законных целей и подтверждает, что " +
            "использует средства, полученные законным путём. \n" +
            "3.3. Пользователь принимает на себя риски, связанные " +
            "с транзакциями с цифровыми активами, и осознает " +
            "возможность полной или частичной потери своих " +
            "средств. \n\n" +
            "ОТКАЗ ОТ ОТВЕТСТВЕННОСТИ\n\n" +
            "4.1. Кошелёк предоставляется «как есть». " +
            "Администрация не несет ответственности за убытки, " +
            "возникшие в результате технических сбоев, " +
            "вредоносного программного обеспечения, проблем с " +
            "интернетом или иных обстоятельств, не зависящих от " +
            "Администрации. \n" +
            "4.2. Пользователь осознает риски, связанные с " +
            "законодательными изменениями, которые могут " +
            "привести к запрету или ограничению операций с " +
            "цифровыми активами. \n\n" +
            "КОНФИДЕНЦИАЛЬНОСТЬ И КУС/АМL \n\n" +
            "5.1. Администрация обязуется обеспечить " +
            "конфиденциальность данных Пользователей согласно " +
            "действующему законодательству. \n" +
            "5.2. Пользователь обязуется соблюдать требования " +
            "международных стандартов АМL/КУС/СТА В случае " +
            "необходимости Администрация может запросить " +
            "дополнительную информацию для идентификации " +
            "Пользователя. \n" +
            "5.3. Система автоматической проверки AML включается по умолчанию " +
            "Пользователь всегда может отключить эту функцию в настройках. \n\n" +
            "ПРЕКРАЩЕНИЕ ДОСТУПА \n\n" +
            "6.1. Администрация имеет право приостановить или " +
            "прекратить доступ Пользователю в случае нарушения " +
            "условий данного Соглашения или при незаконности " +
            "использования Кошелька в юрисдикции Пользователя. \n\n" +
            "РАЗРЕШЕНИЕ СПОРОВ \n\n" +
            "7.1. Все споры должны решаться путем переговоров." +
            "При невозможности урегулирования спор подлежит рассмотрению в суде по месту регистрации Администрации. \n\n" +
            "ЗАКЛЮЧИТЕЛЬНЫЕ ПОЛОЖЕНИЯ \n\n" +
            "8.1. Соглашение регулируется законодательством страны регистрации Администрации. \n" +
            "8.2. Соглашение составлено на русском языке. При переводе на другие языки преимущественную силу имеет русская версия. " +
            "Используя Кошелёк, Пользователь подтверждает, что о ознакомился с настоящим Соглашением, понимает и " +
            "принимает его условия в полном объёме."

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                navigationIcon = {
                    run {
                    }
                },
            )
        },
    ) { padding ->
        padding
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .paint(
                        painterResource(id = R.drawable.create_recovery_bg),
                        contentScale = ContentScale.FillBounds,
                    ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.35f),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                Text(
                    text = "Добро пожаловать \n в ProfPay",
                    modifier =
                        Modifier
                            .padding(start = 16.dp)
                            .padding(vertical = 0.dp),
                    style = MaterialTheme.typography.headlineSmall,
                    color = BackgroundLight,
                )
            }

            Column(
                modifier =
                    Modifier
                        .fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    var isChecked by remember { mutableStateOf(false) }
                    AnimatedFadingTextList(splitTextIntoBlocks(text, 1)) {
                        Column(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 0.dp),
                        ) {
                            // Текст с галочкой
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                        ) { isChecked = !isChecked }
                                        .padding(top = 24.dp),
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { isChecked = it },
                                )
                                Text(
                                    text = "Я принимаю условия соглашения",
                                    color = BackgroundDark,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                            }
                        }
                        Row(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 32.dp, bottom = 32.dp),
                            horizontalArrangement = Arrangement.End,
                        ) {
                            Button(
                                onClick = {
                                    if (isFirstStart) {
                                        toNavigate(OnboardingScreen.CreateOrRecoverWalletFS.route)
                                    } else {
                                        toNavigate(Graph.Home.route)
                                    }

                                    viewModel.setUserLegalConsentsTrue()

                                    sharedPref.edit(commit = true) {
                                        putBoolean(PrefKeys.ACCEPTED_RULES, true)
                                    }
                                },
                                enabled = isChecked,
                                colors =
                                    ButtonDefaults.buttonColors(
                                        containerColor = Color.White,
                                        contentColor = BackgroundDark,
                                        disabledContainerColor = Color.LightGray,
                                        disabledContentColor = BackgroundDark.copy(alpha = 0.5f),
                                    ),
                                shape = RoundedCornerShape(10.dp),
                                elevation =
                                    ButtonDefaults.buttonElevation(
                                        defaultElevation = 7.dp,
                                    ),
                                modifier =
                                    Modifier
                                        .padding(horizontal = 8.dp)
                                        .fillMaxWidth(0.5f)
                                        .height(IntrinsicSize.Min),
                            ) {
                                Text(text = "Продолжить", style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimatedFadingTextList(
    textBlocks: List<String>,
    itemContent: @Composable () -> Unit = {},
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    LazyColumn(
        state = listState,
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
    ) {
        itemsIndexed(textBlocks) { index, block ->
            val itemInfo by remember {
                derivedStateOf {
                    listState.layoutInfo.visibleItemsInfo.find { it.index == index }
                }
            }

            // Затухание начинается, когда элемент начинает уезжать вверх
            val fadeDistancePx = with(density) { 10.dp.toPx() }

            val alpha =
                remember {
                    mutableFloatStateOf(1f)
                }

            LaunchedEffect(itemInfo?.offset) {
                alpha.floatValue = calculateAlpha(itemInfo?.offset?.toFloat(), fadeDistancePx)
            }

            val animatedAlpha by animateFloatAsState(
                targetValue = alpha.floatValue,
                animationSpec = tween(durationMillis = 400),
                label = "fadeAlpha",
            )

            Text(
                text = block,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .graphicsLayer { this.alpha = animatedAlpha }
                        .padding(vertical = 0.dp),
                //                style = MaterialTheme.typography.titleMedium,
                style = MaterialTheme.typography.bodyLarge,
                color = BackgroundDark,
            )
        }
        item {
            itemContent()
        }
    }
}

private fun calculateAlpha(
    offset: Float?,
    fadeDistancePx: Float,
): Float {
    if (offset == null) return 1f

    return when {
        offset >= 0 -> 1f
        offset <= -fadeDistancePx -> 0f
        else -> 1f + (offset / fadeDistancePx)
    }.coerceIn(0f, 1f)
}

fun splitTextIntoBlocks(
    text: String,
    linesPerBlock: Int,
): List<String> {
    val words = text.split(" ")
    val blocks = mutableListOf<String>()
    var current = StringBuilder()

    for (word in words) {
        current.append(word).append(" ")
        if (current.length >= 60 * linesPerBlock) { // эвристика для строк
            blocks.add(current.toString().trim())
            current = StringBuilder()
        }
    }
    if (current.isNotBlank()) {
        blocks.add(current.toString().trim())
    }
    return blocks
}

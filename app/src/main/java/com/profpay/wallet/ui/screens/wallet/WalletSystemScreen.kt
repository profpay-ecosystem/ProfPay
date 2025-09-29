package com.profpay.wallet.ui.screens.wallet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.core.content.edit
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import com.profpay.wallet.R
import com.profpay.wallet.bridge.view_model.wallet.WalletSystemViewModel
import com.profpay.wallet.data.database.dao.wallet.WalletProfileModel
import com.profpay.wallet.ui.app.theme.BackgroundIcon2
import com.profpay.wallet.ui.app.theme.PubAddressDark
import com.profpay.wallet.ui.app.theme.RedColor
import com.profpay.wallet.ui.shared.sharedPref
import com.profpay.wallet.ui.widgets.dialog.AlertDialogWidget
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletSystemScreen(
    goToBack: () -> Unit,
    goToWalletInfo: () -> Unit,
    goToCoRA: () -> Unit,
    viewModel: WalletSystemViewModel = hiltViewModel(),
) {
    val sharedPref = sharedPref()

    val walletList by viewModel.getListAllWallets().observeAsState(emptyList())

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
                        text = "Wallet System",
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
                            .padding(vertical = 8.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "Выберите кошелёк:",
                        modifier =
                            Modifier
                                .padding(top = 16.dp, bottom = 8.dp)
                                .fillMaxWidth(),
                        style = MaterialTheme.typography.titleMedium,
                    )

                    LazyColumn(
                        modifier = Modifier.padding(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        if (walletList.isNotEmpty()) {
                            items(walletList) { wallet ->
                                val currentWalletId = sharedPref.getLong("wallet_id", 1)
                                CardForWalletSystemFeature(
                                    wallet = wallet,
                                    onClick = {
                                        sharedPref.edit { putLong("wallet_id", wallet.id!!) }
                                        goToWalletInfo()
                                    },
                                    selected = wallet.id == currentWalletId,
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }

                    Row(
                        modifier =
                            Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .clickable { goToCoRA() },
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AddCircle,
                                contentDescription = "",
                                modifier = Modifier.padding(),
//                                tint =
                            )

                            Text(
                                modifier = Modifier.padding(),
                                text = "Добавить кошелёк",
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CardForWalletSystemFeature(
    wallet: WalletProfileModel,
    onClick: () -> Unit = {},
    selected: Boolean = false,
) {
    val color =
        if (selected) MaterialTheme.colorScheme.surfaceContainer else MaterialTheme.colorScheme.primary

    var expandedDropdownMenu by remember { mutableStateOf(false) }
    val (openControlWallet, setOpenControlWallet) = bottomSheetControlOfTheWallet(wallet = wallet)

    Card(
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .shadow(7.dp, RoundedCornerShape(10.dp)),
        onClick = { onClick() },
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(vertical = 6.dp)
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = wallet.name,
                Modifier.padding(start = 16.dp),
                style = MaterialTheme.typography.bodyLarge,
            )
            Column {
                IconButton(onClick = {
                    expandedDropdownMenu = !expandedDropdownMenu
                }) {
                    Icon(
                        modifier = Modifier.size(25.dp),
                        imageVector = ImageVector.vectorResource(id = R.drawable.icon_more_vert),
                        contentDescription = "",
                        tint = BackgroundIcon2,
                    )
                }
                DropdownMenu(
                    expanded = expandedDropdownMenu,
                    onDismissRequest = { expandedDropdownMenu = false },
                ) {
                    DropdownMenuItem(
                        onClick = {
                            expandedDropdownMenu = false
                            onClick()
                        },
                        text = {
                            Text(
                                "Перейти",
                                fontWeight = FontWeight.SemiBold,
                            )
                        },
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 8.dp))
                    DropdownMenuItem(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        onClick = {
                            expandedDropdownMenu = false
                            setOpenControlWallet(true)
                        },
                        text = {
                            Text(
                                "Управлять",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        },
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun bottomSheetControlOfTheWallet(
    viewModel: WalletSystemViewModel = hiltViewModel(),
    wallet: WalletProfileModel,
): Pair<Boolean, (Boolean) -> Unit> {
    val sheetState =
        rememberModalBottomSheetState(
            skipPartiallyExpanded = true,
            confirmValueChange = { true },
        )
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val (isOpenSheet, setIsOpenSheet) = remember { mutableStateOf(false) }

    val (isOpenSeedPhrase, setIsOpenSeedPhrase) = remember { mutableStateOf(false) }
    val (isOpenConfDeleteWallet, setIsOpenConfDeleteWallet) = remember { mutableStateOf(false) }

    val (seedPhr, setSeedPhr) = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.viewModelScope.launch {
            setSeedPhr(
                viewModel.getSeedPhrase(walletId = wallet.id!!) ?: "",
            )
        }
    }

    if (isOpenSheet) {
        ModalBottomSheet(
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            dragHandle = { Box(modifier = Modifier) },
            modifier = Modifier.fillMaxHeight(0.7f),
            onDismissRequest = {
                coroutineScope.launch {
                    sheetState.hide()
                    delay(400)
                    setIsOpenSheet(false)
                }
            },
            sheetState = sheetState,
        ) {
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize(),
            ) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Text(
                        modifier = Modifier.padding(vertical = 8.dp),
                        text = "Кошелёк",
                        style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
                    )
                }
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                ) {
                    Text(
                        modifier = Modifier.padding(top = 8.dp),
                        text = "Название",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                RenamingWalletFeature(
                    walletName = wallet.name,
                    onClick = { newName ->
                        viewModel.viewModelScope.launch {
                            viewModel.updateNameWalletById(wallet.id!!, newName)
                        }
                    },
                )

                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onPrimary),
                        onClick = { setIsOpenSeedPhrase(!isOpenSeedPhrase) },
                    ) {
                        Text(
                            text = "${if (isOpenSeedPhrase) "Скрыть" else "Посмотреть"} сид-фразу",
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                    if (isOpenSeedPhrase && seedPhr.isNotEmpty()) {
                        IconButton(onClick = { clipboardManager.setText(AnnotatedString(seedPhr)) }) {
                            Icon(
                                modifier =
                                    Modifier
                                        .size(18.dp),
                                imageVector = ImageVector.vectorResource(id = R.drawable.icon_copy),
                                contentDescription = "",
                                tint = MaterialTheme.colorScheme.onPrimary,
                            )
                        }
                    }
                }
                SeedPhraseCardAnimated(
                    seedPhrase = seedPhr,
                    isExpanded = isOpenSeedPhrase,
                )

                TextButton(
                    colors = ButtonDefaults.textButtonColors(contentColor = RedColor),
                    onClick = {
                        setIsOpenConfDeleteWallet(!isOpenConfDeleteWallet)
//                            viewModel.deleteWalletProfile(walletId = wallet.id!!)
                    },
                ) {
                    Text(text = "Удалить кошелёк", style = MaterialTheme.typography.titleSmall)
                }
                if (isOpenConfDeleteWallet) {
                    AlertDialogWidget(
                        onDismissRequest = {
                            setIsOpenConfDeleteWallet(!isOpenConfDeleteWallet)
                        },
                        onConfirmation = {
                            viewModel.viewModelScope.launch {
                                viewModel.deleteWalletProfile(walletId = wallet.id!!)
                                setIsOpenConfDeleteWallet(!isOpenConfDeleteWallet)
                            }
                        },
                        dialogTitle = "Удалить кошелёк",
                        isSmallDialogTitle = true,
                        dialogText =
                            "Вы действительно уверены, что хотите удалить кошелек?\n\n" +
                                "Данные и привязанные к нему смарт-контракты будут уничтожены.",
                        textDismissButton = "Назад",
                        textConfirmButton = "Подтвердить",
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
    return isOpenSheet to { setIsOpenSheet(it) }
}

@Composable
fun SeedPhraseCardAnimated(
    seedPhrase: String,
    isExpanded: Boolean,
) {
    val words = seedPhrase.trim().split(" ")

    Card(
        shape = RoundedCornerShape(12.dp),
        modifier =
            Modifier
                .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(8.dp),
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(tween(500)) + expandVertically(tween(500)),
            exit = fadeOut(tween(500)) + shrinkVertically(tween(500)),
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Column {
                    if (seedPhrase.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            modifier =
                                Modifier
                                    .fillMaxWidth(),
                            contentPadding = PaddingValues(4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            itemsIndexed(words) { index, word ->
                                Text(
                                    text = "${index + 1}. $word",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier =
                                        Modifier
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                                RoundedCornerShape(8.dp),
                                            ).padding(8.dp),
                                )
                            }
                        }
                    } else {
                        Text(
                            text = "Удалите и восстановите данный кошелёк, для работы этой функции",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier =
                                Modifier
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        RoundedCornerShape(8.dp),
                                    ).padding(8.dp),
                        )
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
fun RenamingWalletFeature(
    walletName: String,
    onClick: (newName: String) -> Unit,
) {
    val maxNameLength = 20
    val disallowedChars = Regex("[\"'\\\\<>;{}()]")

    var newName by remember { mutableStateOf(walletName) }

    Card(
        modifier =
            Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .shadow(7.dp, RoundedCornerShape(10.dp)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextField(
                value = newName,
                onValueChange = { input ->
                    // Убираем запрещённые символы и ограничиваем длину
                    val cleaned =
                        input
                            .replace(disallowedChars, "")
                            .take(maxNameLength)
                    newName = cleaned
                },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                placeholder = {
                    Text(
                        text = "Введите новое имя",
                        style = MaterialTheme.typography.bodyLarge,
                        color = PubAddressDark,
                    )
                },
                textStyle = MaterialTheme.typography.bodyLarge,
                trailingIcon = {
                    Row(
                        modifier =
                            Modifier
                                .padding(end = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(
                            modifier = Modifier.size(25.dp),
                            colors =
                                IconButtonDefaults.iconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.onPrimary,
                                    contentColor = MaterialTheme.colorScheme.primary,
                                ),
                            onClick = {
                                newName = ""
                            },
                        ) {
                            Icon(
                                modifier = Modifier.fillMaxSize(0.6f),
                                imageVector = Icons.Filled.Clear,
                                contentDescription = "",
                            )
                        }
                        if (newName != walletName && newName != "") {
                            Spacer(modifier = Modifier.size(6.dp))
                            IconButton(
                                modifier = Modifier.size(25.dp),
                                colors =
                                    IconButtonDefaults.iconButtonColors(
                                        containerColor = MaterialTheme.colorScheme.onPrimary,
                                        contentColor = MaterialTheme.colorScheme.primary,
                                    ),
                                onClick = {
                                    onClick(newName)
                                },
                            ) {
                                Icon(
                                    modifier = Modifier.fillMaxSize(0.6f),
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "",
                                )
                            }
                        }
                    }
                },
                colors =
                    TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = PubAddressDark,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.onBackground,
                        selectionColors =
                            TextSelectionColors(
                                handleColor = MaterialTheme.colorScheme.onBackground,
                                backgroundColor = Color.Transparent,
                            ),
                    ),
            )
        }
    }
}

@Deprecated("")
@Composable
fun CardForWalletSystemFeatureDeprecated(
    paintIconId: Int,
    label: String,
    balance: Double,
    onClick: () -> Unit = {},
) {
    Card(
        modifier =
            Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth()
                .height(65.dp)
                .shadow(7.dp, RoundedCornerShape(10.dp)),
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
                                .size(45.dp)
                                .paint(
                                    painterResource(id = paintIconId),
                                    contentScale = ContentScale.FillBounds,
                                ),
                        contentAlignment = Alignment.Center,
                    ) {}
                    Column(modifier = Modifier.padding(horizontal = 12.dp, 8.dp)) {
                        Text(
                            text = label,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Column(
                modifier =
                    Modifier
                        .padding(horizontal = 16.dp)
                        .weight(0.5f)
                        .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
            ) {
                Text(text = "$balance", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

package com.profpay.wallet.ui.feature.wallet

import android.graphics.Matrix
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.content.edit
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.profpay.wallet.PrefKeys
import com.profpay.wallet.data.database.models.AddressWithTokens
import com.profpay.wallet.ui.app.theme.HexagonColor2
import com.profpay.wallet.ui.app.theme.HexagonColor3
import com.profpay.wallet.ui.app.theme.HexagonColor4
import com.profpay.wallet.ui.app.theme.HexagonColor5
import com.profpay.wallet.ui.app.theme.HexagonColor6
import com.profpay.wallet.ui.app.theme.HexagonColor7
import com.profpay.wallet.ui.shared.sharedPref
import com.profpay.wallet.ui.widgets.dialog.AlertDialogWidget
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class HexagonWithCircleShape(
    private val rotate: Boolean = false,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path =
            RoundedPolygon(
                numVertices = 6,
                radius = size.minDimension / 2,
                centerX = size.width / 2,
                centerY = size.height / 2,
                rounding =
                    CornerRounding(
                        size.minDimension / 15f,
                        smoothing = 0.75f,
                    ),
            ).toPath()
        if (rotate) {
            val matrix = Matrix()
            matrix.postRotate(30f, size.width / 2, size.height / 2)
            path.transform(matrix)
        }

        return Outline.Generic(path.asComposePath())
    }
}

class HexagonShape(
    private val rotate: Boolean = false,
) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path =
            RoundedPolygon(
                numVertices = 6,
                radius = size.minDimension / 2,
                centerX = size.width / 2,
                centerY = size.height / 2,
                rounding =
                    CornerRounding(
                        size.minDimension / 15f,
                        smoothing = 0.75f,
                    ),
            ).toPath()
        if (rotate) {
            val matrix = Matrix()
            matrix.postRotate(30f, size.width / 2, size.height / 2)
            path.transform(matrix)
        }
        return Outline.Generic(path.asComposePath())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HexagonsFeature(
    goToBack: () -> Unit,
    goToReceive: () -> Unit,
    addressList: List<AddressWithTokens>,
    size: Dp,
) {
    val sharedPref = sharedPref()
    var openDialog by remember { mutableStateOf(false) }

    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val listColors: List<Color> =
        listOf(
            HexagonColor2,
            HexagonColor3,
            HexagonColor4,
            HexagonColor5,
            HexagonColor6,
            HexagonColor7,
        )
    val hexagonPoints = generateHexagonPoints()

    var clickSot1 by remember { mutableStateOf(false) }

    Box(modifier = Modifier) {
        TopAppBar(
            title = {
                Text(
                    text = "Wallet",
                    fontSize = if ((size.value / 16) >= 18) (size.value / 16).sp else 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    style = TextStyle(color = Color.White),
                )
            },
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
            navigationIcon = {
                run {
                    IconButton(
                        modifier = Modifier.size(32.dp),
                        onClick = { goToBack() },
                    ) {
                        Icon(
                            modifier = Modifier.size(30.dp),
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = Color.White,
                        )
                    }
                }
            },
        )
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .size(size),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .clip(HexagonWithCircleShape(clickSot1))
                        .size(size / 4)
//                        .background(Color(0xFF6A0E8D,))
                        .border(size / 80, Color(0xFF6A0E8D), HexagonWithCircleShape(clickSot1))
                        .clickable {
                            openDialog = !openDialog
                        }.animateContentSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = "1", fontSize = (size / 10).value.sp, fontWeight = FontWeight.Thin, color = Color.White)
            }
            hexagonPoints.forEachIndexed { index, point ->
                var clickSots by remember { mutableStateOf(false) }
                Box(
                    modifier =
                        Modifier
                            .size(size / 4)
                            .offset(x = point.first * size / 6.25f, y = point.second * size / 6.25f)
                            .clip(HexagonShape(clickSots))
                            .border(size / 80, listColors[index], HexagonShape(clickSots))
                            .clickable {
                                sharedPref
                                    .edit {
                                        putString(
                                            PrefKeys.ADDRESS_FOR_RECEIVE,
                                            addressList[index + 1].addressEntity.address,
                                        )
                                    }
                                goToReceive()
                            }.animateContentSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "${index + 2}",
                        fontSize = (size / 10).value.sp,
                        fontWeight = FontWeight.Thin,
                        color = Color.White,
                    )
                }
            }

            if (openDialog) {
                AlertDialogWidget(
                    onConfirmation = {
                        sharedPref
                            .edit {
                                putString(
                                    PrefKeys.ADDRESS_FOR_RECEIVE,
                                    addressList[0].addressEntity.address,
                                )
                            }
                        goToReceive()
                        openDialog = !openDialog
                    },
                    onDismissRequest = {
                        openDialog = !openDialog
                    },
                    dialogTitle = "Главный адрес",
                    dialogText =
                        "Пополнение главной соты не рекомендуется " +
                            "вместо этого откройте любую доп-соту и пополните ее.\nПосле AML проверки " +
                            "Вы сможете перевести валюту на центральную соту, " +
                            "так Ваш центральный адрес будет чист всегда.",
                    textConfirmButton = "Всё-равно открыть",
                    textDismissButton = "Закрыть",
                )
            }
        }
    }
}

fun generateHexagonPoints(): List<Pair<Float, Float>> {
    val angleStep = (2 * PI / 6).toFloat()
    val radius = 1.5f // Радиус круга, содержащего точки шестиугольника

    return (0 until 6).map { index ->
        val angle = angleStep * index
        Pair(radius * cos(angle - 32.99f), radius * sin(angle - 32.99f))
    }
}

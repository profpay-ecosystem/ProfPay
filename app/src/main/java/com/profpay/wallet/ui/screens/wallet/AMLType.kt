package com.profpay.wallet.ui.screens.wallet

import androidx.compose.ui.graphics.Color
import com.profpay.wallet.ui.app.theme.RedColor

enum class AMLType(
    val index: Int,
    val label: String,
    val description: String,
    val color: Color,
) {
    HIGH_RISC(
        index = 2,
        label = "Высокий риск",
        description = "Есть высокая вероятность, что преводы с этого адреса могут быть заблокированы...",
        color = RedColor,
    ),
    MEDIUM_RISC(
        index = 1,
        label = "Средний риск",
        description =
            "Есть вероятность, что преводы с этого адреса могут быть заблокированы " +
                "централизованными биржами криптовалют",
        color = Color(0xFFFFB218),
    ),
    LOW_RISC(
        index = 0,
        label = "Низкий риск",
        description = "Низкая вероятность, что преводы с этого адреса могут быть заблокированы...",
        color = Color(0xFF51B413),
    ),
}

package com.profpay.wallet.ui.extensions

import android.app.Activity
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext

@Composable
fun Modifier.protectFromTapjacking(): Modifier {
    val context = LocalContext.current
    return this.then(
        Modifier.onGloballyPositioned {
            (context as? Activity)?.window?.decorView?.filterTouchesWhenObscured = true
        }
    )
}

fun Modifier.protectFromTapjacking(context: Context): Modifier = this.then(
    Modifier.onGloballyPositioned {
        (context as? Activity)?.window?.decorView?.filterTouchesWhenObscured = true
    }
)

package com.example.telegramWallet.ui.feature.lockScreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegramWallet.ui.feature.biometricAuth.FaceIDAuthentication

@Preview(showBackground = true)
@Composable
fun NumberButton(
    modifier: Modifier = Modifier,
    number: String = "1",
    onClick: (number: String) -> Unit = {},
    onClickBiom: () -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier =
            Modifier
                .aspectRatio(2f)
                .clip(shape = RoundedCornerShape(200.dp))
                .clickable(onClick = { onClick(number) }),
    ) {
        when (number) {
            "-1" -> {
                FaceIDAuthentication(
                    toNavigate = {
                        onClickBiom()
                    },
                )
            }
            else -> {
                Text(
                    text = number,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
//    Button(
//        shape = RoundedCornerShape(200.dp),
//        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
//        modifier = modifier
//            .size(75.dp)
//            .padding(0.dp),
//        onClick = {
//            onClick(number)
//        },
//    ) {
//        Text(
//            text = number, color = MaterialTheme.colorScheme.onBackground, fontSize = 22.sp
//        )
//    }

package com.example.telegramWallet.ui.app.theme

// Set of Material typography styles to start with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.telegramWallet.R

// 1. Создаём провайдера
val provider =
    GoogleFont.Provider(
        providerAuthority = "com.google.android.gms.fonts",
        providerPackage = "com.google.android.gms",
        certificates = R.array.com_google_android_gms_fonts_certs,
    )

@Composable
fun rememberTypography(googleFontName: String): Typography {
    val font = remember(googleFontName) { GoogleFont(googleFontName) }

    val provider =
        remember {
            GoogleFont.Provider(
                providerAuthority = "com.google.android.gms.fonts",
                providerPackage = "com.google.android.gms",
                certificates = com.example.telegramWallet.R.array.com_google_android_gms_fonts_certs,
            )
        }

    val fontFamily =
        remember(font, provider) {
            FontFamily(
                Font(googleFont = font, fontProvider = provider, weight = FontWeight.Normal, style = FontStyle.Normal),
                Font(googleFont = font, fontProvider = provider, weight = FontWeight.Medium, style = FontStyle.Normal),
                Font(googleFont = font, fontProvider = provider, weight = FontWeight.Bold, style = FontStyle.Normal),
                Font(googleFont = font, fontProvider = provider, weight = FontWeight.Normal, style = FontStyle.Italic),
                Font(googleFont = font, fontProvider = provider, weight = FontWeight.Bold, style = FontStyle.Italic),
            )
        }

    return remember(fontFamily) {
        Typography(
            displayLarge =
                TextStyle( // 32.sp, Normal
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 32.sp,
                ),
            displayMedium =
                TextStyle( // 32.sp, SemiBold
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 32.sp,
                ),
            displaySmall =
                TextStyle( // 28.sp, Normal
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 28.sp,
                ),
            headlineLarge =
                TextStyle( // 28.sp, SemiBold
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 28.sp,
                ),
            headlineMedium =
                TextStyle( // 24.sp, Normal
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 24.sp,
                ),
            headlineSmall =
                TextStyle( // 24.sp, SemiBold
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 24.sp,
                ),
            titleLarge =
                TextStyle( // 20.sp, Normal
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 20.sp,
                ),
            titleMedium =
                TextStyle( // 20.sp, SemiBold
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 20.sp,
                ),
            titleSmall =
                TextStyle( // 16.sp, Normal
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                ),
            bodyLarge =
                TextStyle( // 16.sp, SemiBold
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                ),
            bodyMedium =
                TextStyle( // 14.sp, Normal
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                ),
            bodySmall =
                TextStyle( // 14.sp, SemiBold
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                ),
            labelLarge =
                TextStyle( // 12.sp, Normal
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                ),
            labelMedium =
                TextStyle( // 12.sp, SemiBold
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                ),
            labelSmall =
                TextStyle( // 11.sp, Normal
                    fontFamily = fontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 11.sp,
                ),
        )
    }
}

enum class LocalFontSize(
    val fS: TextUnit,
) {
    ExtraSmall(12.sp),
    Small(14.sp),
    Medium(16.sp),
    Large(20.sp),
    ExtraLarge(22.sp),
    Huge(24.sp),
    XLarge(32.sp),
    XXLarge(40.sp),
}

@Composable
@Preview(showBackground = true, name = "Typography Preview")
fun TypographyPreview() {
    val typography = rememberTypography("Roboto") // или любой другой шрифт

    Column(
        modifier =
            Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("displayLarge", style = typography.displayLarge)
        Text("displayMedium", style = typography.displayMedium)
        Text("displaySmall", style = typography.displaySmall)
        Text("headlineLarge", style = typography.headlineLarge)
        Text("headlineMedium", style = typography.headlineMedium)
        Text("headlineSmall", style = typography.headlineSmall)
        Text("titleLarge", style = typography.titleLarge)
        Text("titleMedium", style = typography.titleMedium)
        Text("titleSmall", style = typography.titleSmall)
        Text("bodyLarge", style = typography.bodyLarge)
        Text("bodyMedium", style = typography.bodyMedium)
        Text("bodySmall", style = typography.bodySmall)
        Text("labelLarge", style = typography.labelLarge)
        Text("labelMedium", style = typography.labelMedium)
        Text("labelSmall", style = typography.labelSmall)
    }
}

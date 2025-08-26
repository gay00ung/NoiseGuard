package net.lateinit.noiseguard.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import noiseguard.composeapp.generated.resources.Pretendard_Black
import noiseguard.composeapp.generated.resources.Pretendard_Bold
import noiseguard.composeapp.generated.resources.Pretendard_ExtraBold
import noiseguard.composeapp.generated.resources.Pretendard_ExtraLight
import noiseguard.composeapp.generated.resources.Pretendard_Light
import noiseguard.composeapp.generated.resources.Pretendard_Medium
import noiseguard.composeapp.generated.resources.Pretendard_Regular
import noiseguard.composeapp.generated.resources.Pretendard_SemiBold
import noiseguard.composeapp.generated.resources.Pretendard_Thin
import noiseguard.composeapp.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
fun getPretendardFontFamily() = FontFamily(
    Font(Res.font.Pretendard_Thin, FontWeight.Thin),
    Font(Res.font.Pretendard_ExtraLight, FontWeight.ExtraLight),
    Font(Res.font.Pretendard_Light, FontWeight.Light),
    Font(Res.font.Pretendard_Regular, FontWeight.Normal),
    Font(Res.font.Pretendard_Medium, FontWeight.Medium),
    Font(Res.font.Pretendard_SemiBold, FontWeight.SemiBold),
    Font(Res.font.Pretendard_Bold, FontWeight.Bold),
    Font(Res.font.Pretendard_ExtraBold, FontWeight.ExtraBold),
    Font(Res.font.Pretendard_Black, FontWeight.Black)
)

@Composable
fun getTypography(): Typography {
    val fontFamily = getPretendardFontFamily()

    return Typography(
        displayLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = 0.sp
        ),
        displayMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = fontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}
package app.dinar.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import app.dinar.core.designsystem.R

/** Vazirmatn (SIL Open Font License), a typeface designed for Persian UI text. */
val DinarFontFamily: FontFamily = FontFamily(
    Font(R.font.core_designsystem_vazirmatn_regular, FontWeight.Normal),
    Font(R.font.core_designsystem_vazirmatn_medium, FontWeight.Medium),
    Font(R.font.core_designsystem_vazirmatn_semibold, FontWeight.SemiBold),
    Font(R.font.core_designsystem_vazirmatn_bold, FontWeight.Bold),
    Font(R.font.core_designsystem_vazirmatn_extrabold, FontWeight.ExtraBold),
)

private fun style(size: Int, line: Int, weight: FontWeight, tracking: Double = 0.0) = TextStyle(
    fontFamily = DinarFontFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = line.sp,
    letterSpacing = tracking.sp,
)

val DinarTypography = Typography(
    displayLarge = style(52, 64, FontWeight.Bold),
    displayMedium = style(42, 52, FontWeight.Bold),
    displaySmall = style(34, 44, FontWeight.ExtraBold),
    headlineLarge = style(30, 40, FontWeight.Bold),
    headlineMedium = style(26, 36, FontWeight.Bold),
    headlineSmall = style(22, 32, FontWeight.Bold),
    titleLarge = style(20, 30, FontWeight.Bold),
    titleMedium = style(16, 26, FontWeight.SemiBold),
    titleSmall = style(14, 22, FontWeight.SemiBold),
    bodyLarge = style(16, 26, FontWeight.Normal),
    bodyMedium = style(14, 22, FontWeight.Normal),
    bodySmall = style(12, 18, FontWeight.Normal),
    labelLarge = style(14, 20, FontWeight.SemiBold),
    labelMedium = style(12, 18, FontWeight.Medium),
    labelSmall = style(11, 16, FontWeight.Medium),
)

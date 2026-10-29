package app.dinar.core.designsystem.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import app.dinar.core.designsystem.theme.DinarPalette as P

val LightColorScheme = lightColorScheme(
    primary = P.Ink80,
    onPrimary = P.GoldLight,
    primaryContainer = P.GoldContainer,
    onPrimaryContainer = P.GoldOnContainer,
    secondary = P.GoldDeep,
    onSecondary = Color.White,
    secondaryContainer = P.GoldContainer,
    onSecondaryContainer = P.GoldOnContainer,
    tertiary = P.IncomeLight,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFCFF3E6),
    onTertiaryContainer = Color(0xFF00382A),
    error = Color(0xFFBA1A1A),
    onError = Color.White,
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = P.Paper,
    onBackground = P.Ink,
    surface = P.Paper,
    onSurface = P.Ink,
    surfaceVariant = Color(0xFFECE8DF),
    onSurfaceVariant = Color(0xFF5B616C),
    surfaceTint = P.Ink80,
    outline = Color(0xFF8C9099),
    outlineVariant = Color(0xFFE3DFD6),
    inverseSurface = P.Ink70,
    inverseOnSurface = Color(0xFFF2F0EA),
    inversePrimary = P.Gold,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFFBF9F5),
    surfaceContainer = Color(0xFFF2EFE8),
    surfaceContainerHigh = Color(0xFFECE8E0),
    surfaceContainerHighest = Color(0xFFE6E2D9),
    surfaceBright = Color.White,
    surfaceDim = Color(0xFFDDD9D0),
    scrim = Color.Black,
)

val DarkColorScheme = darkColorScheme(
    primary = P.Gold,
    onPrimary = Color(0xFF17140D),
    primaryContainer = Color(0xFF5C4618),
    onPrimaryContainer = Color(0xFFFCE6BB),
    secondary = Color(0xFFD9C29A),
    onSecondary = Color(0xFF3A2A06),
    secondaryContainer = Color(0xFF2B2416),
    onSecondaryContainer = Color(0xFFF4E8CF),
    tertiary = P.IncomeDark,
    onTertiary = Color(0xFF003828),
    tertiaryContainer = Color(0xFF0F3B2F),
    onTertiaryContainer = Color(0xFFB4F1DA),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = P.Night,
    onBackground = Color(0xFFF2F0EA),
    surface = P.Night,
    onSurface = Color(0xFFF2F0EA),
    surfaceVariant = Color(0xFF2A2E36),
    onSurfaceVariant = Color(0xFF9BA1AC),
    surfaceTint = P.Gold,
    outline = Color(0xFF6E7480),
    outlineVariant = Color(0xFF262A33),
    inverseSurface = Color(0xFFF2F0EA),
    inverseOnSurface = P.Ink,
    inversePrimary = P.GoldDeep,
    surfaceContainerLowest = Color(0xFF08090C),
    surfaceContainerLow = Color(0xFF13161B),
    surfaceContainer = Color(0xFF16191F),
    surfaceContainerHigh = Color(0xFF1F232B),
    surfaceContainerHighest = Color(0xFF2A2E36),
    surfaceBright = Color(0xFF2F333C),
    surfaceDim = P.Night,
    scrim = Color.Black,
)

/** Colors Material 3 has no slot for. */
@Immutable
data class DinarExtendedColors(
    val income: Color,
    val expense: Color,
    val transfer: Color,
    val balanceCard: Color,
    val onBalanceCard: Color,
    val onBalanceCardMuted: Color,
    val balanceCardChip: Color,
    val warning: Color,
)

private val LightExtended = DinarExtendedColors(
    income = P.IncomeLight,
    expense = P.ExpenseLight,
    transfer = Color(0xFF3D5A80),
    balanceCard = P.Ink,
    onBalanceCard = Color(0xFFF5F3EE),
    onBalanceCardMuted = Color(0xFFA7ABB4),
    balanceCardChip = Color(0xFF262A32),
    warning = Color(0xFFB45309),
)

private val DarkExtended = DinarExtendedColors(
    income = P.IncomeDark,
    expense = P.ExpenseDark,
    transfer = Color(0xFF8FB3E8),
    balanceCard = P.Gold,
    onBalanceCard = Color(0xFF17140D),
    onBalanceCardMuted = Color(0xFF5C4618),
    balanceCardChip = Color(0xFFCFA14E),
    warning = Color(0xFFFBBF24),
)

val LocalDinarColors = staticCompositionLocalOf { LightExtended }

/**
 * The app theme. Dinar is a Persian app, so the whole tree is laid out right to left
 * regardless of the device language.
 */
@Composable
fun DinarTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme: ColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val extended = (if (darkTheme) DarkExtended else LightExtended).let {
        if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            it.copy(
                balanceCard = colorScheme.primary,
                onBalanceCard = colorScheme.onPrimary,
                onBalanceCardMuted = colorScheme.onPrimary.copy(alpha = 0.72f),
                balanceCardChip = colorScheme.onPrimary.copy(alpha = 0.14f),
            )
        } else {
            it
        }
    }

    CompositionLocalProvider(
        LocalDinarColors provides extended,
        LocalLayoutDirection provides LayoutDirection.Rtl,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DinarTypography,
            shapes = DinarShapes,
            content = content,
        )
    }
}

object DinarTheme {
    val extendedColors: DinarExtendedColors
        @Composable get() = LocalDinarColors.current
}

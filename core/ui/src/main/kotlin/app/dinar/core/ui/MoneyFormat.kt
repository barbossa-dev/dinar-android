package app.dinar.core.ui

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDirection
import app.dinar.core.common.format.DateFormatter
import app.dinar.core.common.format.MoneyFormatter
import app.dinar.core.common.format.localized
import app.dinar.core.common.time.JalaliDate
import app.dinar.core.model.CurrencyUnit
import app.dinar.core.model.UserPreferences

/** Display settings every screen needs to format numbers, provided once at the app root. */
@Immutable
data class DisplaySettings(
    val currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    val persianDigits: Boolean = true,
    val hideBalances: Boolean = false,
) {
    val unitLabel: String get() = MoneyFormatter.unitLabel(currencyUnit)

    fun amount(rial: Long): String = MoneyFormatter.amount(rial, currencyUnit, persianDigits)
    fun signed(rial: Long): String = MoneyFormatter.signed(rial, currencyUnit, persianDigits)
    fun compact(rial: Long): String = MoneyFormatter.compact(rial, currencyUnit, persianDigits)
    fun number(value: Int): String = value.localized(persianDigits)
    fun percent(fraction: Float): String = number(Math.round(fraction * 100)) + "٪"
    fun fullDate(date: JalaliDate): String = DateFormatter.full(date, persianDigits)
    fun monthYear(date: JalaliDate): String = DateFormatter.monthYear(date, persianDigits)
    fun dayMonth(date: JalaliDate): String = DateFormatter.dayMonth(date, persianDigits)

    /** Masks the value when the user chose to hide balances (e.g. in public places). */
    fun maybeHidden(text: String): String = if (hideBalances) "••••••" else text

    companion object {
        fun from(prefs: UserPreferences) = DisplaySettings(
            currencyUnit = prefs.currencyUnit,
            persianDigits = prefs.usePersianDigits,
            hideBalances = prefs.hideBalances,
        )
    }
}

val LocalDisplaySettings = staticCompositionLocalOf { DisplaySettings() }

/** Money text. Numbers keep left-to-right digit order inside right-to-left layouts. */
@Composable
fun AmountText(
    rial: Long,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = LocalContentColor.current,
    signed: Boolean = false,
    maskable: Boolean = false,
) {
    val settings = LocalDisplaySettings.current
    val text = if (signed) settings.signed(rial) else settings.amount(rial)
    Text(
        text = if (maskable) settings.maybeHidden(text) else text,
        modifier = modifier,
        style = style.copy(textDirection = TextDirection.Ltr),
        color = color,
        maxLines = 1,
    )
}

package app.dinar.core.common.format

import app.dinar.core.common.time.JalaliDate
import app.dinar.core.model.CurrencyUnit
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs

private const val PERSIAN_DIGITS = "۰۱۲۳۴۵۶۷۸۹"
private const val ARABIC_DIGITS = "٠١٢٣٤٥٦٧٨٩"
const val PERSIAN_THOUSANDS_SEPARATOR = '٬'
const val PERSIAN_DECIMAL_SEPARATOR = '٫'

/** Replaces Latin digits with Persian ones (and `.` with the Persian decimal mark). */
fun String.toPersianDigits(): String = buildString(length) {
    for (c in this@toPersianDigits) {
        append(
            when (c) {
                in '0'..'9' -> PERSIAN_DIGITS[c - '0']
                '.' -> PERSIAN_DECIMAL_SEPARATOR
                else -> c
            },
        )
    }
}

/** Normalizes Persian and Arabic-Indic digits to Latin ones; useful for parsing user input. */
fun String.toLatinDigits(): String = buildString(length) {
    for (c in this@toLatinDigits) {
        val p = PERSIAN_DIGITS.indexOf(c)
        val a = ARABIC_DIGITS.indexOf(c)
        append(
            when {
                p >= 0 -> '0' + p
                a >= 0 -> '0' + a
                else -> c
            },
        )
    }
}

fun Long.groupThousands(separator: Char = PERSIAN_THOUSANDS_SEPARATOR): String {
    val digits = abs(this).toString()
    val grouped = digits.reversed().chunked(3).joinToString(separator.toString()).reversed()
    return if (this < 0) "-$grouped" else grouped
}

fun Int.localized(persianDigits: Boolean): String =
    if (persianDigits) toString().toPersianDigits() else toString()

/**
 * Formats money for display. Values are stored in rial and converted to the
 * user's [CurrencyUnit] here.
 */
object MoneyFormatter {

    fun amount(rial: Long, unit: CurrencyUnit, persianDigits: Boolean = true): String {
        val value = rial / unit.rialFactor
        val text = value.groupThousands(if (persianDigits) PERSIAN_THOUSANDS_SEPARATOR else ',')
        return if (persianDigits) text.replace("-", "−").toPersianDigits() else text
    }

    /** `+۱۲٬۰۰۰` / `−۱۲٬۰۰۰`; the sign is explicit for income and expense rows. */
    fun signed(rial: Long, unit: CurrencyUnit, persianDigits: Boolean = true): String {
        val body = amount(abs(rial), unit, persianDigits)
        return when {
            rial > 0 -> "+$body"
            rial < 0 -> "−$body"
            else -> body
        }
    }

    /** Short form for charts and tight spaces: ۱۸٫۴ میلیون, ۸۵۰ هزار. */
    fun compact(rial: Long, unit: CurrencyUnit, persianDigits: Boolean = true): String {
        val value = abs(rial / unit.rialFactor)
        val sign = if (rial < 0) "−" else ""
        val text = when {
            value >= 1_000_000_000 -> trim(value / 1_000_000_000.0) + " میلیارد"
            value >= 1_000_000 -> trim(value / 1_000_000.0) + " میلیون"
            value >= 1_000 -> trim(value / 1_000.0) + " هزار"
            else -> value.toString()
        }
        return sign + if (persianDigits) text.toPersianDigits() else text
    }

    fun unitLabel(unit: CurrencyUnit): String = when (unit) {
        CurrencyUnit.TOMAN -> "تومان"
        CurrencyUnit.RIAL -> "ریال"
    }

    /** Converts a number the user typed in [unit] to rial for storage. */
    fun toRial(input: String, unit: CurrencyUnit): Long? =
        input.toLatinDigits().filter(Char::isDigit).takeIf { it.isNotEmpty() }
            ?.toLongOrNull()?.times(unit.rialFactor)

    private fun trim(value: Double): String {
        val rounded = Math.round(value * 10) / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toLong().toString() else rounded.toString()
    }
}

object DateFormatter {
    /** «۲۶ مهر ۱۴۰۵» */
    fun full(date: JalaliDate, persianDigits: Boolean = true): String =
        "${date.day.localized(persianDigits)} ${date.monthName} ${date.year.localized(persianDigits)}"

    /** «۲۶ مهر» */
    fun dayMonth(date: JalaliDate, persianDigits: Boolean = true): String =
        "${date.day.localized(persianDigits)} ${date.monthName}"

    /** «مهر ۱۴۰۵» */
    fun monthYear(date: JalaliDate, persianDigits: Boolean = true): String =
        "${date.monthName} ${date.year.localized(persianDigits)}"

    /** «امروز» / «دیروز» / «۲۳ مهر» */
    fun relativeDay(
        instant: Instant,
        today: JalaliDate,
        zone: ZoneId = ZoneId.systemDefault(),
        persianDigits: Boolean = true,
    ): String {
        val date = JalaliDate.fromInstant(instant, zone)
        return when (date) {
            today -> "امروز"
            today.plusDays(-1) -> "دیروز"
            else -> if (date.year == today.year) dayMonth(date, persianDigits) else full(date, persianDigits)
        }
    }

    fun time(instant: Instant, zone: ZoneId = ZoneId.systemDefault(), persianDigits: Boolean = true): String {
        val t = instant.atZone(zone).toLocalTime()
        val text = "%02d:%02d".format(t.hour, t.minute)
        return if (persianDigits) text.toPersianDigits() else text
    }
}

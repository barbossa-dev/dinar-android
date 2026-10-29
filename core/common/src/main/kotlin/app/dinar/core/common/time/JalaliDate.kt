package app.dinar.core.common.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * A date in the Solar Hijri (Jalali / Shamsi) calendar.
 *
 * Conversion uses the well-known arithmetic algorithm that is exact for
 * every year an app like this can reasonably meet (1 to 3177 SH).
 */
data class JalaliDate(val year: Int, val month: Int, val day: Int) : Comparable<JalaliDate> {

    init {
        require(month in 1..12) { "month must be in 1..12, was $month" }
        require(day in 1..31) { "day must be in 1..31, was $day" }
    }

    val monthName: String get() = MONTH_NAMES[month - 1]

    val lengthOfMonth: Int get() = monthLength(year, month)

    val isLeapYear: Boolean get() = isLeap(year)

    fun toLocalDate(): LocalDate {
        val (gy, gm, gd) = toGregorian(year, month, day)
        return LocalDate.of(gy, gm, gd)
    }

    fun firstDayOfMonth(): JalaliDate = copy(day = 1)

    fun plusMonths(months: Int): JalaliDate {
        val total = (year * 12 + (month - 1)) + months
        val newYear = Math.floorDiv(total, 12)
        val newMonth = Math.floorMod(total, 12) + 1
        return JalaliDate(newYear, newMonth, minOf(day, monthLength(newYear, newMonth)))
    }

    fun plusDays(days: Long): JalaliDate = fromLocalDate(toLocalDate().plusDays(days))

    /** Start of this day as an [Instant] in [zone]. */
    fun startOfDay(zone: ZoneId = ZoneId.systemDefault()): Instant =
        toLocalDate().atStartOfDay(zone).toInstant()

    override fun compareTo(other: JalaliDate): Int =
        compareValuesBy(this, other, { it.year }, { it.month }, { it.day })

    companion object {
        val MONTH_NAMES = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
        )

        val WEEKDAY_NAMES = listOf(
            "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه",
        )

        fun fromLocalDate(date: LocalDate): JalaliDate {
            val (jy, jm, jd) = toJalali(date.year, date.monthValue, date.dayOfMonth)
            return JalaliDate(jy, jm, jd)
        }

        fun fromInstant(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): JalaliDate =
            fromLocalDate(instant.atZone(zone).toLocalDate())

        fun today(zone: ZoneId = ZoneId.systemDefault()): JalaliDate =
            fromLocalDate(LocalDate.now(zone))

        fun isLeap(year: Int): Boolean {
            val start = JalaliDate(year, 1, 1).toLocalDate()
            val next = JalaliDate(year + 1, 1, 1).toLocalDate()
            return next.toEpochDay() - start.toEpochDay() == 366L
        }

        fun monthLength(year: Int, month: Int): Int = when {
            month <= 6 -> 31
            month <= 11 -> 30
            isLeap(year) -> 30
            else -> 29
        }

        internal fun toJalali(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
            val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
            val gy2 = if (gm > 2) gy + 1 else gy
            var days = 355666L + 365L * gy + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) +
                ((gy2 + 399) / 400) + gd + gDaysInMonth[gm - 1]
            var jy = -1595 + 33 * (days / 12053).toInt()
            days %= 12053
            jy += 4 * (days / 1461).toInt()
            days %= 1461
            if (days > 365) {
                jy += ((days - 1) / 365).toInt()
                days = (days - 1) % 365
            }
            val jm: Int
            val jd: Int
            if (days < 186) {
                jm = 1 + (days / 31).toInt()
                jd = 1 + (days % 31).toInt()
            } else {
                jm = 7 + ((days - 186) / 30).toInt()
                jd = 1 + ((days - 186) % 30).toInt()
            }
            return Triple(jy, jm, jd)
        }

        internal fun toGregorian(jYear: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
            val jy = jYear + 1595
            var days = -355668L + 365L * jy + (jy / 33) * 8 + ((jy % 33) + 3) / 4 + jd +
                if (jm < 7) (jm - 1) * 31 else (jm - 7) * 30 + 186
            var gy = 400 * (days / 146097).toInt()
            days %= 146097
            if (days > 36524) {
                days -= 1
                gy += 100 * (days / 36524).toInt()
                days %= 36524
                if (days >= 365) days += 1
            }
            gy += 4 * (days / 1461).toInt()
            days %= 1461
            if (days > 365) {
                gy += ((days - 1) / 365).toInt()
                days = (days - 1) % 365
            }
            var gd = (days + 1).toInt()
            val leap = (gy % 4 == 0 && gy % 100 != 0) || gy % 400 == 0
            val monthDays = intArrayOf(0, 31, if (leap) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
            var gm = 0
            while (gm < 13 && gd > monthDays[gm]) {
                gd -= monthDays[gm]
                gm++
            }
            return Triple(gy, gm, gd)
        }
    }
}

/** A half-open time range `[start, end)`. */
data class TimeRange(val start: Instant, val end: Instant)

/** The [TimeRange] covering this whole Jalali month. */
fun JalaliDate.monthRange(zone: ZoneId = ZoneId.systemDefault()): TimeRange =
    TimeRange(firstDayOfMonth().startOfDay(zone), firstDayOfMonth().plusMonths(1).startOfDay(zone))

/** The [TimeRange] covering this whole Jalali year. */
fun JalaliDate.yearRange(zone: ZoneId = ZoneId.systemDefault()): TimeRange =
    TimeRange(JalaliDate(year, 1, 1).startOfDay(zone), JalaliDate(year + 1, 1, 1).startOfDay(zone))

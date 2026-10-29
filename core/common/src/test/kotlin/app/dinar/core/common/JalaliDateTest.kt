package app.dinar.core.common

import app.dinar.core.common.time.JalaliDate
import java.time.LocalDate
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class JalaliDateTest {

    @Test
    fun knownDates_convertBothWays() {
        val cases = mapOf(
            LocalDate.of(2026, 3, 21) to JalaliDate(1405, 1, 1),
            LocalDate.of(2026, 10, 18) to JalaliDate(1405, 7, 26),
            LocalDate.of(2025, 3, 20) to JalaliDate(1403, 12, 30),
            LocalDate.of(2000, 1, 1) to JalaliDate(1378, 10, 11),
            LocalDate.of(2024, 2, 29) to JalaliDate(1402, 12, 10),
        )
        cases.forEach { (gregorian, jalali) ->
            assertEquals(jalali, JalaliDate.fromLocalDate(gregorian), "to jalali: $gregorian")
            assertEquals(gregorian, jalali.toLocalDate(), "to gregorian: $jalali")
        }
    }

    @Test
    fun roundTrip_everyDayForThirtyYears() {
        var date = LocalDate.of(2000, 1, 1)
        val end = LocalDate.of(2030, 12, 31)
        while (date <= end) {
            assertEquals(date, JalaliDate.fromLocalDate(date).toLocalDate())
            date = date.plusDays(1)
        }
    }

    @Test
    fun leapYears() {
        assertTrue(JalaliDate.isLeap(1403))
        assertFalse(JalaliDate.isLeap(1404))
        assertEquals(30, JalaliDate.monthLength(1403, 12))
        assertEquals(29, JalaliDate.monthLength(1404, 12))
    }

    @Test
    fun plusMonths_wrapsYearsAndClampsDays() {
        assertEquals(JalaliDate(1406, 1, 15), JalaliDate(1405, 12, 15).plusMonths(1))
        assertEquals(JalaliDate(1404, 12, 15), JalaliDate(1405, 1, 15).plusMonths(-1))
        assertEquals(JalaliDate(1405, 7, 30), JalaliDate(1405, 6, 31).plusMonths(1))
    }
}

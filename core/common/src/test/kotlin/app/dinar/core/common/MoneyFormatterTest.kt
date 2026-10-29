package app.dinar.core.common

import app.dinar.core.common.format.MoneyFormatter
import app.dinar.core.common.format.toLatinDigits
import app.dinar.core.model.CurrencyUnit
import org.junit.Test
import kotlin.test.assertEquals

class MoneyFormatterTest {

    @Test
    fun amount_inTomanWithPersianDigits() {
        assertEquals("۴۸٬۲۵۰٬۰۰۰", MoneyFormatter.amount(482_500_000, CurrencyUnit.TOMAN))
    }

    @Test
    fun amount_inRialWithLatinDigits() {
        assertEquals("1,250", MoneyFormatter.amount(1_250, CurrencyUnit.RIAL, persianDigits = false))
    }

    @Test
    fun signed_addsExplicitSign() {
        assertEquals("+۱۰٬۰۰۰", MoneyFormatter.signed(100_000, CurrencyUnit.TOMAN))
        assertEquals("−۱۰٬۰۰۰", MoneyFormatter.signed(-100_000, CurrencyUnit.TOMAN))
    }

    @Test
    fun compact_usesPersianUnits() {
        assertEquals("۱۸٫۴ میلیون", MoneyFormatter.compact(184_000_000, CurrencyUnit.TOMAN))
        assertEquals("۸۵۰ هزار", MoneyFormatter.compact(8_500_000, CurrencyUnit.TOMAN))
    }

    @Test
    fun toRial_parsesPersianInput() {
        assertEquals(8_500_000, MoneyFormatter.toRial("۸۵۰٬۰۰۰", CurrencyUnit.TOMAN))
        assertEquals(null, MoneyFormatter.toRial("", CurrencyUnit.TOMAN))
        assertEquals("123", "۱۲۳".toLatinDigits())
    }
}

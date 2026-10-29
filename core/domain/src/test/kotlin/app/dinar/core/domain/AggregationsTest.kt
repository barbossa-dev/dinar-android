package app.dinar.core.domain

import app.dinar.core.common.time.JalaliDate
import app.dinar.core.model.Account
import app.dinar.core.model.AccountType
import app.dinar.core.model.Budget
import app.dinar.core.model.Category
import app.dinar.core.model.CategoryIcon
import app.dinar.core.model.CategoryKind
import app.dinar.core.model.Transaction
import app.dinar.core.model.TransactionDetails
import app.dinar.core.model.TransactionType
import java.time.ZoneOffset
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AggregationsTest {
    private val zone = ZoneOffset.UTC
    private val account = Account(1, "نقدی", AccountType.CASH, 0, 0)
    private val food = Category(1, "خوراک", CategoryKind.EXPENSE, CategoryIcon.GROCERIES, 0)
    private val taxi = Category(2, "تاکسی", CategoryKind.EXPENSE, CategoryIcon.TRANSPORT, 0)
    private val salary = Category(3, "حقوق", CategoryKind.INCOME, CategoryIcon.SALARY, 0)

    private fun tx(type: TransactionType, amount: Long, category: Category?, date: JalaliDate) =
        TransactionDetails(
            Transaction(0, type, amount, category?.id, 1, null, "", date.startOfDay(zone)),
            category, account, null,
        )

    private val mehr = JalaliDate(1405, 7, 10)
    private val rows = listOf(
        tx(TransactionType.EXPENSE, 300, food, mehr),
        tx(TransactionType.EXPENSE, 100, taxi, mehr),
        tx(TransactionType.EXPENSE, 100, food, mehr),
        tx(TransactionType.INCOME, 1_000, salary, mehr),
        tx(TransactionType.TRANSFER, 5_000, null, mehr),
        tx(TransactionType.EXPENSE, 50, food, JalaliDate(1405, 5, 3)),
    )

    @Test
    fun totals_ignoreTransfers() {
        assertEquals(550, Aggregations.totalOf(rows, TransactionType.EXPENSE))
        assertEquals(1_000, Aggregations.totalOf(rows, TransactionType.INCOME))
    }

    @Test
    fun categoryTotals_sortedWithShares() {
        val totals = Aggregations.categoryTotals(rows.take(3), TransactionType.EXPENSE)
        assertEquals(listOf(food, taxi), totals.map { it.category })
        assertEquals(400, totals[0].total)
        assertEquals(0.8f, totals[0].share, 0.0001f)
    }

    @Test
    fun monthlyTotals_includeEmptyMonths() {
        val months = Aggregations.monthlyTotals(rows, mehr, 3, zone)
        assertEquals(listOf(5, 6, 7), months.map { it.month })
        assertEquals(listOf(50L, 0L, 500L), months.map { it.expense })
        assertEquals(1_000, months.last().income)
    }

    @Test
    fun budgetProgress_flagsOverspending() {
        val progress = Aggregations.budgetProgress(
            budgets = listOf(Budget(1, 300), Budget(2, 1_000)),
            categories = listOf(food, taxi),
            spentByCategory = mapOf(1L to 400L, 2L to 100L),
        )
        assertEquals(food, progress.first().category)
        assertTrue(progress.first().isOverBudget)
        assertEquals(-100, progress.first().remaining)
    }
}

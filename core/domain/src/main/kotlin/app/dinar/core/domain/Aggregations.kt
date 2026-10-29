package app.dinar.core.domain

import app.dinar.core.common.time.JalaliDate
import app.dinar.core.model.Budget
import app.dinar.core.model.BudgetProgress
import app.dinar.core.model.Category
import app.dinar.core.model.CategoryTotal
import app.dinar.core.model.MonthlyTotal
import app.dinar.core.model.TransactionDetails
import app.dinar.core.model.TransactionType
import java.time.ZoneId

/**
 * Pure aggregation functions. Keeping them free of Flow and Android makes the
 * business rules trivial to unit test.
 */
object Aggregations {

    fun totalOf(transactions: List<TransactionDetails>, type: TransactionType): Long =
        transactions.filter { it.transaction.type == type }.sumOf { it.transaction.amount }

    /** Category shares for one transaction type, largest first. Uncategorized rows are skipped. */
    fun categoryTotals(
        transactions: List<TransactionDetails>,
        type: TransactionType,
    ): List<CategoryTotal> {
        val relevant = transactions.filter { it.transaction.type == type && it.category != null }
        val grand = relevant.sumOf { it.transaction.amount }.takeIf { it > 0 } ?: return emptyList()
        return relevant
            .groupBy { it.category!! }
            .map { (category, rows) ->
                val total = rows.sumOf { it.transaction.amount }
                CategoryTotal(category, total, total.toFloat() / grand)
            }
            .sortedByDescending { it.total }
    }

    /**
     * Income and expense per Jalali month for the [count] months ending at [lastMonth],
     * oldest first. Months without activity are included with zeros.
     */
    fun monthlyTotals(
        transactions: List<TransactionDetails>,
        lastMonth: JalaliDate,
        count: Int,
        zone: ZoneId = ZoneId.systemDefault(),
    ): List<MonthlyTotal> {
        val byMonth = transactions.groupBy {
            val d = JalaliDate.fromInstant(it.transaction.occurredAt, zone)
            d.year to d.month
        }
        return (count - 1 downTo 0).map { back ->
            val month = lastMonth.firstDayOfMonth().plusMonths(-back)
            val rows = byMonth[month.year to month.month].orEmpty()
            MonthlyTotal(
                year = month.year,
                month = month.month,
                income = totalOf(rows, TransactionType.INCOME),
                expense = totalOf(rows, TransactionType.EXPENSE),
            )
        }
    }

    fun budgetProgress(
        budgets: List<Budget>,
        categories: List<Category>,
        spentByCategory: Map<Long, Long>,
    ): List<BudgetProgress> {
        val byId = categories.associateBy { it.id }
        return budgets.mapNotNull { budget ->
            val category = byId[budget.categoryId] ?: return@mapNotNull null
            BudgetProgress(category, budget.monthlyLimit, spentByCategory[budget.categoryId] ?: 0L)
        }.sortedByDescending { it.fraction }
    }
}

package app.dinar.core.domain

import app.dinar.core.common.time.JalaliDate
import app.dinar.core.common.time.TimeRange
import app.dinar.core.common.time.monthRange
import app.dinar.core.common.time.yearRange
import app.dinar.core.data.repository.TransactionsRepository
import app.dinar.core.model.CategoryTotal
import app.dinar.core.model.MonthlyTotal
import app.dinar.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Duration
import javax.inject.Inject

enum class ReportPeriod { MONTH, YEAR }

data class Report(
    val period: ReportPeriod,
    val anchor: JalaliDate,
    val income: Long,
    val expense: Long,
    val previousIncome: Long,
    val previousExpense: Long,
    val expenseByCategory: List<CategoryTotal>,
    val incomeByCategory: List<CategoryTotal>,
    val trend: List<MonthlyTotal>,
    val dailyAverageExpense: Long,
) {
    val net: Long get() = income - expense
    val savingsRate: Float get() = if (income <= 0) 0f else (net.toFloat() / income).coerceIn(-1f, 1f)

    /** Relative change versus the previous period, or null when there is nothing to compare. */
    fun change(current: Long, previous: Long): Float? =
        if (previous <= 0) null else (current - previous).toFloat() / previous
}

class GetMonthlyReportUseCase @Inject constructor(
    private val transactions: TransactionsRepository,
) {
    operator fun invoke(anchor: JalaliDate, period: ReportPeriod): Flow<Report> {
        val current: TimeRange
        val previous: TimeRange
        when (period) {
            ReportPeriod.MONTH -> {
                current = anchor.monthRange()
                previous = anchor.plusMonths(-1).monthRange()
            }
            ReportPeriod.YEAR -> {
                current = anchor.yearRange()
                previous = anchor.plusMonths(-12).yearRange()
            }
        }
        val trendMonths = if (period == ReportPeriod.YEAR) 12 else 6
        val trendEnd = if (period == ReportPeriod.YEAR) JalaliDate(anchor.year, 12, 1) else anchor
        val trendRange = TimeRange(
            start = trendEnd.firstDayOfMonth().plusMonths(-(trendMonths - 1)).monthRange().start,
            end = trendEnd.monthRange().end,
        )

        return combine(
            transactions.observeInRange(current),
            transactions.observeInRange(previous),
            transactions.observeInRange(trendRange),
        ) { now, before, trendRows ->
            val expense = Aggregations.totalOf(now, TransactionType.EXPENSE)
            val days = Duration.between(current.start, current.end).toDays().coerceAtLeast(1)
            Report(
                period = period,
                anchor = anchor,
                income = Aggregations.totalOf(now, TransactionType.INCOME),
                expense = expense,
                previousIncome = Aggregations.totalOf(before, TransactionType.INCOME),
                previousExpense = Aggregations.totalOf(before, TransactionType.EXPENSE),
                expenseByCategory = Aggregations.categoryTotals(now, TransactionType.EXPENSE),
                incomeByCategory = Aggregations.categoryTotals(now, TransactionType.INCOME),
                trend = Aggregations.monthlyTotals(trendRows, trendEnd, trendMonths),
                dailyAverageExpense = expense / days,
            )
        }
    }
}

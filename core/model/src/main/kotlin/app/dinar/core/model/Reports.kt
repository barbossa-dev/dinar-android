package app.dinar.core.model

/** Spending (or income) of one category within a period. */
data class CategoryTotal(
    val category: Category,
    val total: Long,
    val share: Float,
)

/** Income and expense of a single Jalali month. */
data class MonthlyTotal(
    val year: Int,
    val month: Int,
    val income: Long,
    val expense: Long,
) {
    val net: Long get() = income - expense
}

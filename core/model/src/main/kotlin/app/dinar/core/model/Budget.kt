package app.dinar.core.model

/** A monthly spending limit for one expense category. */
data class Budget(
    val categoryId: Long,
    val monthlyLimit: Long,
)

data class BudgetProgress(
    val category: Category,
    val limit: Long,
    val spent: Long,
) {
    val remaining: Long get() = limit - spent
    val fraction: Float get() = if (limit <= 0) 0f else (spent.toFloat() / limit).coerceAtLeast(0f)
    val isOverBudget: Boolean get() = spent > limit
    val isNearLimit: Boolean get() = !isOverBudget && fraction >= 0.8f
}

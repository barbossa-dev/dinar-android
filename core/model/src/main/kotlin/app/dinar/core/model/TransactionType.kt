package app.dinar.core.model

enum class TransactionType { EXPENSE, INCOME, TRANSFER }

/** Categories only exist for money going in or out; transfers move money between accounts. */
enum class CategoryKind { EXPENSE, INCOME }

/** Filters for the transaction list. A `null` filter means "any". */
data class TransactionFilter(
    val query: String = "",
    val type: TransactionType? = null,
    val accountId: Long? = null,
    val categoryId: Long? = null,
)

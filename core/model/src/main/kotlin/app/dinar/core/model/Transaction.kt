package app.dinar.core.model

import java.time.Instant

data class Transaction(
    val id: Long,
    val type: TransactionType,
    /** Always positive; [type] decides the direction. Stored in rial. */
    val amount: Long,
    val categoryId: Long?,
    val accountId: Long,
    /** Destination account, only for [TransactionType.TRANSFER]. */
    val toAccountId: Long?,
    val note: String,
    val occurredAt: Instant,
)

/** A transaction joined with everything the UI needs to render it. */
data class TransactionDetails(
    val transaction: Transaction,
    val category: Category?,
    val account: Account,
    val toAccount: Account?,
) {
    /** Signed effect on the user's total wealth (transfers are neutral). */
    val signedAmount: Long
        get() = when (transaction.type) {
            TransactionType.INCOME -> transaction.amount
            TransactionType.EXPENSE -> -transaction.amount
            TransactionType.TRANSFER -> 0
        }
}

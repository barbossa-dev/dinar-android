package app.dinar.core.model

enum class AccountType { CASH, BANK_CARD, SAVINGS, WALLET }

data class Account(
    val id: Long,
    val name: String,
    val type: AccountType,
    /** Balance the account had when it was added to Dinar, in the smallest stored unit (rial). */
    val initialBalance: Long,
    val colorArgb: Long,
    val isArchived: Boolean = false,
)

/** An account together with its balance after applying every transaction. */
data class AccountWithBalance(
    val account: Account,
    val balance: Long,
)

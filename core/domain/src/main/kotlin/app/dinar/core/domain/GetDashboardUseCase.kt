package app.dinar.core.domain

import app.dinar.core.common.time.JalaliDate
import app.dinar.core.common.time.monthRange
import app.dinar.core.data.repository.AccountsRepository
import app.dinar.core.data.repository.TransactionsRepository
import app.dinar.core.model.AccountWithBalance
import app.dinar.core.model.TransactionDetails
import app.dinar.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class Dashboard(
    val month: JalaliDate,
    val accounts: List<AccountWithBalance>,
    val totalBalance: Long,
    val monthIncome: Long,
    val monthExpense: Long,
    val recent: List<TransactionDetails>,
    val budgetLimit: Long,
    val budgetSpent: Long,
) {
    val budgetFraction: Float
        get() = if (budgetLimit <= 0) 0f else budgetSpent.toFloat() / budgetLimit
}

/** Everything the home screen shows, as a single reactive stream. */
class GetDashboardUseCase @Inject constructor(
    private val accounts: AccountsRepository,
    private val transactions: TransactionsRepository,
    private val getBudgetProgress: GetBudgetProgressUseCase,
) {
    operator fun invoke(month: JalaliDate = JalaliDate.today()): Flow<Dashboard> = combine(
        accounts.observeAccountsWithBalance(),
        transactions.observeInRange(month.monthRange()),
        transactions.observeRecent(limit = 6),
        getBudgetProgress(month),
    ) { accountList, monthRows, recent, budgets ->
        Dashboard(
            month = month,
            accounts = accountList,
            totalBalance = accountList.sumOf { it.balance },
            monthIncome = Aggregations.totalOf(monthRows, TransactionType.INCOME),
            monthExpense = Aggregations.totalOf(monthRows, TransactionType.EXPENSE),
            recent = recent,
            budgetLimit = budgets.sumOf { it.limit },
            budgetSpent = budgets.sumOf { it.spent },
        )
    }
}

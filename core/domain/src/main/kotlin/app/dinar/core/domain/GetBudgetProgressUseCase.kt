package app.dinar.core.domain

import app.dinar.core.common.time.JalaliDate
import app.dinar.core.common.time.monthRange
import app.dinar.core.data.repository.BudgetsRepository
import app.dinar.core.data.repository.CategoriesRepository
import app.dinar.core.data.repository.TransactionsRepository
import app.dinar.core.model.BudgetProgress
import app.dinar.core.model.CategoryKind
import app.dinar.core.model.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetBudgetProgressUseCase @Inject constructor(
    private val budgets: BudgetsRepository,
    private val categories: CategoriesRepository,
    private val transactions: TransactionsRepository,
) {
    operator fun invoke(month: JalaliDate = JalaliDate.today()): Flow<List<BudgetProgress>> = combine(
        budgets.observeBudgets(),
        categories.observeCategories(CategoryKind.EXPENSE),
        transactions.observeCategoryTotals(TransactionType.EXPENSE, month.monthRange()),
    ) { budgetList, categoryList, spent ->
        Aggregations.budgetProgress(budgetList, categoryList, spent)
    }
}

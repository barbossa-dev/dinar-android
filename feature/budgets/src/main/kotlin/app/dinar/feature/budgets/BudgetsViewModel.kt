package app.dinar.feature.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dinar.core.common.format.MoneyFormatter
import app.dinar.core.common.time.JalaliDate
import app.dinar.core.data.repository.BudgetsRepository
import app.dinar.core.data.repository.CategoriesRepository
import app.dinar.core.data.repository.UserDataRepository
import app.dinar.core.domain.GetBudgetProgressUseCase
import app.dinar.core.model.BudgetProgress
import app.dinar.core.model.Category
import app.dinar.core.model.CategoryKind
import app.dinar.core.model.CurrencyUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BudgetsUiState {
    data object Loading : BudgetsUiState
    data class Success(
        val month: JalaliDate,
        val daysLeft: Int,
        val items: List<BudgetProgress>,
        val unbudgeted: List<Category>,
        val unit: CurrencyUnit,
    ) : BudgetsUiState {
        val totalLimit: Long get() = items.sumOf { it.limit }
        val totalSpent: Long get() = items.sumOf { it.spent }
        val fraction: Float get() = if (totalLimit <= 0) 0f else totalSpent.toFloat() / totalLimit

        /** How much can be spent per remaining day without going over. */
        val dailyAllowance: Long get() = if (daysLeft <= 0) 0 else ((totalLimit - totalSpent) / daysLeft).coerceAtLeast(0)
    }
}

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    getBudgetProgress: GetBudgetProgressUseCase,
    categories: CategoriesRepository,
    userData: UserDataRepository,
    private val budgets: BudgetsRepository,
) : ViewModel() {

    private val today = JalaliDate.today()

    val uiState: StateFlow<BudgetsUiState> = combine(
        getBudgetProgress(today),
        categories.observeCategories(CategoryKind.EXPENSE),
        userData.userPreferences,
    ) { progress, expenseCategories, prefs ->
        val budgeted = progress.map { it.category.id }.toSet()
        BudgetsUiState.Success(
            month = today,
            daysLeft = today.lengthOfMonth - today.day + 1,
            items = progress,
            unbudgeted = expenseCategories.filterNot { it.id in budgeted },
            unit = prefs.currencyUnit,
        ) as BudgetsUiState
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), BudgetsUiState.Loading)

    /** [amountText] is in the user's display unit. */
    fun saveBudget(categoryId: Long, amountText: String) {
        val unit = (uiState.value as? BudgetsUiState.Success)?.unit ?: CurrencyUnit.TOMAN
        val rial = MoneyFormatter.toRial(amountText, unit) ?: return
        if (rial <= 0) return
        viewModelScope.launch { budgets.setBudget(categoryId, rial) }
    }

    fun removeBudget(categoryId: Long) {
        viewModelScope.launch { budgets.removeBudget(categoryId) }
    }
}

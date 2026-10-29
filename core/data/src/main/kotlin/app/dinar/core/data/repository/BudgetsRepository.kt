package app.dinar.core.data.repository

import app.dinar.core.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetsRepository {
    fun observeBudgets(): Flow<List<Budget>>
    suspend fun setBudget(categoryId: Long, monthlyLimit: Long)
    suspend fun removeBudget(categoryId: Long)
}

package app.dinar.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import app.dinar.core.database.model.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets")
    fun observeAll(): Flow<List<BudgetEntity>>

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE category_id = :categoryId")
    suspend fun delete(categoryId: Long)
}

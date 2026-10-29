package app.dinar.core.data.repository

import app.dinar.core.model.Category
import app.dinar.core.model.CategoryKind
import kotlinx.coroutines.flow.Flow

interface CategoriesRepository {
    fun observeCategories(): Flow<List<Category>>
    fun observeCategories(kind: CategoryKind): Flow<List<Category>>
    suspend fun save(category: Category): Long
    suspend fun delete(id: Long)
}

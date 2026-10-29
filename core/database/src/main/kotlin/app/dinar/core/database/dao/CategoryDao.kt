package app.dinar.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import app.dinar.core.database.model.CategoryEntity
import app.dinar.core.model.CategoryKind
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Query("SELECT * FROM categories ORDER BY sort_order, id")
    fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE kind = :kind ORDER BY sort_order, id")
    fun observeByKind(kind: CategoryKind): Flow<List<CategoryEntity>>

    @Upsert
    suspend fun upsert(category: CategoryEntity): Long

    @Insert
    suspend fun insertAll(categories: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun delete(id: Long)
}

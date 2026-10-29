package app.dinar.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import app.dinar.core.database.model.CategorySumEntity
import app.dinar.core.database.model.PopulatedTransaction
import app.dinar.core.database.model.TransactionEntity
import app.dinar.core.model.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Transaction
    @Query(
        """
        SELECT * FROM transactions
        WHERE occurred_at >= :startMillis AND occurred_at < :endMillis
        ORDER BY occurred_at DESC, id DESC
        """,
    )
    fun observeInRange(startMillis: Long, endMillis: Long): Flow<List<PopulatedTransaction>>

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY occurred_at DESC, id DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<PopulatedTransaction>>

    /**
     * Full-text-ish search over notes and category names, with optional filters.
     * Pass `null` for a filter to ignore it.
     */
    @Transaction
    @Query(
        """
        SELECT t.* FROM transactions t
        LEFT JOIN categories c ON c.id = t.category_id
        WHERE (:query = '' OR t.note LIKE '%' || :query || '%' OR c.name LIKE '%' || :query || '%')
            AND (:typeName IS NULL OR t.type = :typeName)
            AND (:accountId IS NULL OR t.account_id = :accountId OR t.to_account_id = :accountId)
            AND (:categoryId IS NULL OR t.category_id = :categoryId)
            AND t.occurred_at >= :startMillis AND t.occurred_at < :endMillis
        ORDER BY t.occurred_at DESC, t.id DESC
        """,
    )
    fun search(
        query: String,
        typeName: String?,
        accountId: Long?,
        categoryId: Long?,
        startMillis: Long,
        endMillis: Long,
    ): Flow<List<PopulatedTransaction>>

    @Transaction
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): PopulatedTransaction?

    @Query(
        """
        SELECT category_id, SUM(amount) AS total FROM transactions
        WHERE type = :type AND category_id IS NOT NULL
            AND occurred_at >= :startMillis AND occurred_at < :endMillis
        GROUP BY category_id
        """,
    )
    fun observeCategorySums(
        type: TransactionType,
        startMillis: Long,
        endMillis: Long,
    ): Flow<List<CategorySumEntity>>

    @Upsert
    suspend fun upsert(transaction: TransactionEntity): Long

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun delete(id: Long)

    @Transaction
    @Query("SELECT * FROM transactions ORDER BY occurred_at")
    suspend fun getAll(): List<PopulatedTransaction>
}

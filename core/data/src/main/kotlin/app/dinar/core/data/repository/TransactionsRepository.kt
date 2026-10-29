package app.dinar.core.data.repository

import app.dinar.core.common.time.TimeRange
import app.dinar.core.model.Transaction
import app.dinar.core.model.TransactionDetails
import app.dinar.core.model.TransactionFilter
import app.dinar.core.model.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionsRepository {
    fun observeInRange(range: TimeRange): Flow<List<TransactionDetails>>
    fun observeRecent(limit: Int): Flow<List<TransactionDetails>>
    fun search(filter: TransactionFilter, range: TimeRange): Flow<List<TransactionDetails>>

    /** Totals per category id for [type] in [range]. */
    fun observeCategoryTotals(type: TransactionType, range: TimeRange): Flow<Map<Long, Long>>

    suspend fun get(id: Long): TransactionDetails?

    /** Inserts or updates; returns the id of the saved row. */
    suspend fun save(transaction: Transaction): Long
    suspend fun delete(id: Long)

    /** Every transaction as CSV, ready to share. */
    suspend fun exportCsv(): String
}

package app.dinar.core.data.repository

import app.dinar.core.common.network.Dispatcher
import app.dinar.core.common.network.DinarDispatchers
import app.dinar.core.common.time.JalaliDate
import app.dinar.core.common.time.TimeRange
import app.dinar.core.database.dao.AccountDao
import app.dinar.core.database.dao.BudgetDao
import app.dinar.core.database.dao.CategoryDao
import app.dinar.core.database.dao.TransactionDao
import app.dinar.core.database.model.BudgetEntity
import app.dinar.core.database.model.asEntity
import app.dinar.core.database.model.asExternalModel
import app.dinar.core.datastore.DinarPreferencesDataSource
import app.dinar.core.model.Account
import app.dinar.core.model.Budget
import app.dinar.core.model.Category
import app.dinar.core.model.CategoryKind
import app.dinar.core.model.CurrencyUnit
import app.dinar.core.model.ThemeConfig
import app.dinar.core.model.Transaction
import app.dinar.core.model.TransactionDetails
import app.dinar.core.model.TransactionFilter
import app.dinar.core.model.TransactionType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class OfflineTransactionsRepository @Inject constructor(
    private val dao: TransactionDao,
    @Dispatcher(DinarDispatchers.IO) private val io: CoroutineDispatcher,
) : TransactionsRepository {

    override fun observeInRange(range: TimeRange): Flow<List<TransactionDetails>> =
        dao.observeInRange(range.start.toEpochMilli(), range.end.toEpochMilli())
            .map { list -> list.map { it.asExternalModel() } }

    override fun observeRecent(limit: Int): Flow<List<TransactionDetails>> =
        dao.observeRecent(limit).map { list -> list.map { it.asExternalModel() } }

    override fun search(filter: TransactionFilter, range: TimeRange): Flow<List<TransactionDetails>> =
        dao.search(
            query = filter.query.trim(),
            typeName = filter.type?.name,
            accountId = filter.accountId,
            categoryId = filter.categoryId,
            startMillis = range.start.toEpochMilli(),
            endMillis = range.end.toEpochMilli(),
        ).map { list -> list.map { it.asExternalModel() } }

    override fun observeCategoryTotals(type: TransactionType, range: TimeRange): Flow<Map<Long, Long>> =
        dao.observeCategorySums(type, range.start.toEpochMilli(), range.end.toEpochMilli())
            .map { sums -> sums.associate { it.categoryId to it.total } }

    override suspend fun get(id: Long): TransactionDetails? =
        withContext(io) { dao.getById(id)?.asExternalModel() }

    override suspend fun save(transaction: Transaction): Long = withContext(io) {
        require(transaction.amount > 0) { "Amount must be positive" }
        val id = dao.upsert(transaction.asEntity())
        // Upsert returns -1 when it updated an existing row.
        if (id == -1L) transaction.id else id
    }

    override suspend fun delete(id: Long) = withContext(io) { dao.delete(id) }

    override suspend fun exportCsv(): String = withContext(io) {
        buildString {
            appendLine("date,type,amount_rial,category,account,to_account,note")
            dao.getAll().map { it.asExternalModel() }.forEach { d ->
                val t = d.transaction
                val date = JalaliDate.fromInstant(t.occurredAt)
                appendLine(
                    listOf(
                        "%04d/%02d/%02d".format(date.year, date.month, date.day),
                        t.type.name,
                        t.amount.toString(),
                        d.category?.name.orEmpty(),
                        d.account.name,
                        d.toAccount?.name.orEmpty(),
                        t.note,
                    ).joinToString(",") { it.csvEscaped() },
                )
            }
        }
    }

    private fun String.csvEscaped(): String =
        if (any { it == ',' || it == '"' || it == '\n' }) "\"" + replace("\"", "\"\"") + "\"" else this
}

internal class OfflineAccountsRepository @Inject constructor(
    private val dao: AccountDao,
) : AccountsRepository {
    override fun observeAccountsWithBalance() =
        dao.observeAccountsWithBalance().map { list -> list.map { it.asExternalModel() } }

    override fun observeAccounts(): Flow<List<Account>> =
        dao.observeAccounts().map { list -> list.map { it.asExternalModel() } }

    override suspend fun save(account: Account): Long {
        val id = dao.upsert(account.asEntity())
        return if (id == -1L) account.id else id
    }

    override suspend fun archive(id: Long) = dao.archive(id)
}

internal class OfflineCategoriesRepository @Inject constructor(
    private val dao: CategoryDao,
) : CategoriesRepository {
    override fun observeCategories(): Flow<List<Category>> =
        dao.observeAll().map { list -> list.map { it.asExternalModel() } }

    override fun observeCategories(kind: CategoryKind): Flow<List<Category>> =
        dao.observeByKind(kind).map { list -> list.map { it.asExternalModel() } }

    override suspend fun save(category: Category): Long {
        val id = dao.upsert(category.asEntity())
        return if (id == -1L) category.id else id
    }

    override suspend fun delete(id: Long) = dao.delete(id)
}

internal class OfflineBudgetsRepository @Inject constructor(
    private val dao: BudgetDao,
) : BudgetsRepository {
    override fun observeBudgets(): Flow<List<Budget>> =
        dao.observeAll().map { list -> list.map { it.asExternalModel() } }

    override suspend fun setBudget(categoryId: Long, monthlyLimit: Long) =
        dao.upsert(BudgetEntity(categoryId, monthlyLimit))

    override suspend fun removeBudget(categoryId: Long) = dao.delete(categoryId)
}

internal class OfflineFirstUserDataRepository @Inject constructor(
    private val source: DinarPreferencesDataSource,
) : UserDataRepository {
    override val userPreferences = source.userPreferences
    override suspend fun setUserName(name: String) { source.setUserName(name) }
    override suspend fun setThemeConfig(theme: ThemeConfig) { source.setThemeConfig(theme) }
    override suspend fun setDynamicColor(enabled: Boolean) { source.setDynamicColor(enabled) }
    override suspend fun setCurrencyUnit(unit: CurrencyUnit) { source.setCurrencyUnit(unit) }
    override suspend fun setPersianDigits(enabled: Boolean) { source.setPersianDigits(enabled) }
    override suspend fun setHideBalances(hidden: Boolean) { source.setHideBalances(hidden) }
    override suspend fun setOnboardingCompleted(done: Boolean) { source.setOnboardingCompleted(done) }
}

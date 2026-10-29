package app.dinar.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import app.dinar.core.database.dao.AccountDao
import app.dinar.core.database.dao.BudgetDao
import app.dinar.core.database.dao.CategoryDao
import app.dinar.core.database.dao.TransactionDao
import app.dinar.core.database.model.AccountEntity
import app.dinar.core.database.model.BudgetEntity
import app.dinar.core.database.model.CategoryEntity
import app.dinar.core.database.model.TransactionEntity
import app.dinar.core.database.util.InstantConverters

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(InstantConverters::class)
internal abstract class DinarDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
}

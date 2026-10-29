package app.dinar.core.database.di

import android.content.Context
import androidx.room.Room
import app.dinar.core.database.DinarDatabase
import app.dinar.core.database.SeedData
import app.dinar.core.database.dao.AccountDao
import app.dinar.core.database.dao.BudgetDao
import app.dinar.core.database.dao.CategoryDao
import app.dinar.core.database.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
internal object DatabaseModule {
    @Provides
    @Singleton
    fun providesDinarDatabase(@ApplicationContext context: Context): DinarDatabase =
        Room.databaseBuilder(context, DinarDatabase::class.java, "dinar-database")
            .addCallback(SeedData.callback)
            .build()
}

@Module
@InstallIn(SingletonComponent::class)
internal object DaosModule {
    @Provides fun providesAccountDao(db: DinarDatabase): AccountDao = db.accountDao()
    @Provides fun providesCategoryDao(db: DinarDatabase): CategoryDao = db.categoryDao()
    @Provides fun providesTransactionDao(db: DinarDatabase): TransactionDao = db.transactionDao()
    @Provides fun providesBudgetDao(db: DinarDatabase): BudgetDao = db.budgetDao()
}

package app.dinar.core.data.di

import app.dinar.core.data.repository.AccountsRepository
import app.dinar.core.data.repository.BudgetsRepository
import app.dinar.core.data.repository.CategoriesRepository
import app.dinar.core.data.repository.OfflineAccountsRepository
import app.dinar.core.data.repository.OfflineBudgetsRepository
import app.dinar.core.data.repository.OfflineCategoriesRepository
import app.dinar.core.data.repository.OfflineFirstUserDataRepository
import app.dinar.core.data.repository.OfflineTransactionsRepository
import app.dinar.core.data.repository.TransactionsRepository
import app.dinar.core.data.repository.UserDataRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class DataModule {
    @Binds internal abstract fun bindsTransactionsRepository(impl: OfflineTransactionsRepository): TransactionsRepository
    @Binds internal abstract fun bindsAccountsRepository(impl: OfflineAccountsRepository): AccountsRepository
    @Binds internal abstract fun bindsCategoriesRepository(impl: OfflineCategoriesRepository): CategoriesRepository
    @Binds internal abstract fun bindsBudgetsRepository(impl: OfflineBudgetsRepository): BudgetsRepository
    @Binds internal abstract fun bindsUserDataRepository(impl: OfflineFirstUserDataRepository): UserDataRepository
}

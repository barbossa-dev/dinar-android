package app.dinar.core.data.repository

import app.dinar.core.model.Account
import app.dinar.core.model.AccountWithBalance
import kotlinx.coroutines.flow.Flow

interface AccountsRepository {
    fun observeAccountsWithBalance(): Flow<List<AccountWithBalance>>
    fun observeAccounts(): Flow<List<Account>>
    suspend fun save(account: Account): Long

    /** Accounts are archived rather than deleted so history stays intact. */
    suspend fun archive(id: Long)
}

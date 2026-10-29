package app.dinar.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import app.dinar.core.database.model.AccountEntity
import app.dinar.core.database.model.AccountWithBalanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {

    /**
     * Every active account with its live balance:
     * initial balance + income − expense − transfers out + transfers in.
     */
    @Query(
        """
        SELECT a.*,
            a.initial_balance
            + COALESCE((
                SELECT SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE -t.amount END)
                FROM transactions t WHERE t.account_id = a.id
            ), 0)
            + COALESCE((
                SELECT SUM(t.amount) FROM transactions t
                WHERE t.to_account_id = a.id AND t.type = 'TRANSFER'
            ), 0) AS balance
        FROM accounts a
        WHERE a.is_archived = 0
        ORDER BY a.id
        """,
    )
    fun observeAccountsWithBalance(): Flow<List<AccountWithBalanceEntity>>

    @Query("SELECT * FROM accounts WHERE is_archived = 0 ORDER BY id")
    fun observeAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccount(id: Long): AccountEntity?

    @Upsert
    suspend fun upsert(account: AccountEntity): Long

    @Query("UPDATE accounts SET is_archived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("SELECT COUNT(*) FROM accounts WHERE is_archived = 0")
    suspend fun activeCount(): Int
}

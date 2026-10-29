package app.dinar.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.dinar.core.model.Account
import app.dinar.core.model.AccountType
import app.dinar.core.model.AccountWithBalance

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    @ColumnInfo(name = "initial_balance") val initialBalance: Long,
    @ColumnInfo(name = "color_argb") val colorArgb: Long,
    @ColumnInfo(name = "is_archived", defaultValue = "0") val isArchived: Boolean = false,
)

data class AccountWithBalanceEntity(
    @Embedded val account: AccountEntity,
    val balance: Long,
)

fun AccountEntity.asExternalModel() = Account(
    id = id,
    name = name,
    type = type,
    initialBalance = initialBalance,
    colorArgb = colorArgb,
    isArchived = isArchived,
)

fun Account.asEntity() = AccountEntity(
    id = id,
    name = name,
    type = type,
    initialBalance = initialBalance,
    colorArgb = colorArgb,
    isArchived = isArchived,
)

fun AccountWithBalanceEntity.asExternalModel() =
    AccountWithBalance(account = account.asExternalModel(), balance = balance)

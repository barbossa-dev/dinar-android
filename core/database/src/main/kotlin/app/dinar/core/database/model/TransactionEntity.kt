package app.dinar.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import app.dinar.core.model.Transaction
import app.dinar.core.model.TransactionDetails
import app.dinar.core.model.TransactionType
import java.time.Instant

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.SET_NULL,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["to_account_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index("category_id"),
        Index("account_id"),
        Index("to_account_id"),
        Index("occurred_at"),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Long,
    @ColumnInfo(name = "category_id") val categoryId: Long?,
    @ColumnInfo(name = "account_id") val accountId: Long,
    @ColumnInfo(name = "to_account_id") val toAccountId: Long?,
    val note: String,
    @ColumnInfo(name = "occurred_at") val occurredAt: Instant,
)

/** A transaction with its category and accounts, loaded by Room in one transaction. */
data class PopulatedTransaction(
    @Embedded val entity: TransactionEntity,
    @Relation(parentColumn = "category_id", entityColumn = "id")
    val category: CategoryEntity?,
    @Relation(parentColumn = "account_id", entityColumn = "id")
    val account: AccountEntity,
    @Relation(parentColumn = "to_account_id", entityColumn = "id")
    val toAccount: AccountEntity?,
)

fun TransactionEntity.asExternalModel() = Transaction(
    id = id,
    type = type,
    amount = amount,
    categoryId = categoryId,
    accountId = accountId,
    toAccountId = toAccountId,
    note = note,
    occurredAt = occurredAt,
)

fun Transaction.asEntity() = TransactionEntity(
    id = id,
    type = type,
    amount = amount,
    categoryId = categoryId,
    accountId = accountId,
    toAccountId = toAccountId,
    note = note,
    occurredAt = occurredAt,
)

fun PopulatedTransaction.asExternalModel() = TransactionDetails(
    transaction = entity.asExternalModel(),
    category = category?.asExternalModel(),
    account = account.asExternalModel(),
    toAccount = toAccount?.asExternalModel(),
)

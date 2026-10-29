package app.dinar.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import app.dinar.core.model.Budget

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class BudgetEntity(
    @PrimaryKey @ColumnInfo(name = "category_id") val categoryId: Long,
    @ColumnInfo(name = "monthly_limit") val monthlyLimit: Long,
)

/** Sum of amounts per category within a time range. */
data class CategorySumEntity(
    @ColumnInfo(name = "category_id") val categoryId: Long,
    val total: Long,
)

fun BudgetEntity.asExternalModel() = Budget(categoryId = categoryId, monthlyLimit = monthlyLimit)
fun Budget.asEntity() = BudgetEntity(categoryId = categoryId, monthlyLimit = monthlyLimit)

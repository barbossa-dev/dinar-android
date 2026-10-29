package app.dinar.core.database.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import app.dinar.core.model.Category
import app.dinar.core.model.CategoryIcon
import app.dinar.core.model.CategoryKind

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val kind: CategoryKind,
    val icon: CategoryIcon,
    @ColumnInfo(name = "color_argb") val colorArgb: Long,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
)

fun CategoryEntity.asExternalModel() = Category(
    id = id,
    name = name,
    kind = kind,
    icon = icon,
    colorArgb = colorArgb,
    sortOrder = sortOrder,
)

fun Category.asEntity() = CategoryEntity(
    id = id,
    name = name,
    kind = kind,
    icon = icon,
    colorArgb = colorArgb,
    sortOrder = sortOrder,
)

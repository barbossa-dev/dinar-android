package app.dinar.core.database

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import app.dinar.core.model.AccountType
import app.dinar.core.model.CategoryIcon
import app.dinar.core.model.CategoryKind

/** Default categories and accounts inserted the first time the database is created. */
internal object SeedData {

    private data class SeedCategory(val name: String, val kind: CategoryKind, val icon: CategoryIcon, val color: Long)

    private val categories = listOf(
        SeedCategory("خوراک", CategoryKind.EXPENSE, CategoryIcon.GROCERIES, 0xFFE0B35F),
        SeedCategory("رستوران و کافه", CategoryKind.EXPENSE, CategoryIcon.RESTAURANT, 0xFFE07A5F),
        SeedCategory("حمل‌ونقل", CategoryKind.EXPENSE, CategoryIcon.TRANSPORT, 0xFF5B8DEF),
        SeedCategory("خودرو", CategoryKind.EXPENSE, CategoryIcon.CAR, 0xFF3D5A80),
        SeedCategory("قبوض", CategoryKind.EXPENSE, CategoryIcon.BILLS, 0xFF2A9D8F),
        SeedCategory("اینترنت و موبایل", CategoryKind.EXPENSE, CategoryIcon.INTERNET, 0xFF48A9C5),
        SeedCategory("مسکن و اجاره", CategoryKind.EXPENSE, CategoryIcon.HOME, 0xFF8D6E63),
        SeedCategory("خرید", CategoryKind.EXPENSE, CategoryIcon.SHOPPING, 0xFFD16BA5),
        SeedCategory("پوشاک", CategoryKind.EXPENSE, CategoryIcon.CLOTHES, 0xFFB388EB),
        SeedCategory("سلامت", CategoryKind.EXPENSE, CategoryIcon.HEALTH, 0xFFEF476F),
        SeedCategory("آموزش", CategoryKind.EXPENSE, CategoryIcon.EDUCATION, 0xFF4361EE),
        SeedCategory("تفریح", CategoryKind.EXPENSE, CategoryIcon.ENTERTAINMENT, 0xFFF4A261),
        SeedCategory("سفر", CategoryKind.EXPENSE, CategoryIcon.TRAVEL, 0xFF06AED5),
        SeedCategory("هدیه", CategoryKind.EXPENSE, CategoryIcon.GIFT, 0xFFE76F51),
        SeedCategory("خیریه", CategoryKind.EXPENSE, CategoryIcon.CHARITY, 0xFF90BE6D),
        SeedCategory("سایر", CategoryKind.EXPENSE, CategoryIcon.OTHER, 0xFF8A909C),
        SeedCategory("حقوق", CategoryKind.INCOME, CategoryIcon.SALARY, 0xFF0B7A5E),
        SeedCategory("پروژه و فریلنس", CategoryKind.INCOME, CategoryIcon.FREELANCE, 0xFF2A9D8F),
        SeedCategory("سرمایه‌گذاری", CategoryKind.INCOME, CategoryIcon.INVESTMENT, 0xFF43AA8B),
        SeedCategory("اجاره", CategoryKind.INCOME, CategoryIcon.RENT_INCOME, 0xFF8D6E63),
        SeedCategory("پاداش", CategoryKind.INCOME, CategoryIcon.BONUS, 0xFFE0B35F),
        SeedCategory("هدیه", CategoryKind.INCOME, CategoryIcon.GIFT, 0xFFE76F51),
        SeedCategory("بازپرداخت", CategoryKind.INCOME, CategoryIcon.REFUND, 0xFF5B8DEF),
        SeedCategory("سایر", CategoryKind.INCOME, CategoryIcon.OTHER, 0xFF8A909C),
    )

    val callback = object : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            categories.forEachIndexed { index, c ->
                db.execSQL(
                    "INSERT INTO categories (name, kind, icon, color_argb, sort_order) VALUES (?, ?, ?, ?, ?)",
                    arrayOf<Any>(c.name, c.kind.name, c.icon.name, c.color, index),
                )
            }
            listOf(
                Triple("نقدی", AccountType.CASH, 0xFF0B7A5EL),
                Triple("کارت بانکی", AccountType.BANK_CARD, 0xFF5B8DEFL),
            ).forEach { (name, type, color) ->
                db.execSQL(
                    "INSERT INTO accounts (name, type, initial_balance, color_argb, is_archived) VALUES (?, ?, 0, ?, 0)",
                    arrayOf<Any>(name, type.name, color),
                )
            }
        }
    }
}

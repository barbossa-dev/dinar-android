package app.dinar.core.model

data class Category(
    val id: Long,
    val name: String,
    val kind: CategoryKind,
    val icon: CategoryIcon,
    val colorArgb: Long,
    val sortOrder: Int = 0,
)

/**
 * Icons are stored as stable keys so that both the database and the UI layer
 * can agree on them without the model depending on any UI toolkit.
 */
enum class CategoryIcon {
    GROCERIES, RESTAURANT, TRANSPORT, CAR, HOME, BILLS, INTERNET, PHONE,
    SHOPPING, CLOTHES, HEALTH, EDUCATION, ENTERTAINMENT, TRAVEL, GIFT, PETS,
    SPORTS, BEAUTY, KIDS, CHARITY, SALARY, FREELANCE, INVESTMENT, RENT_INCOME,
    REFUND, SALE, BONUS, OTHER,
}

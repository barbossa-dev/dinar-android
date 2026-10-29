package app.dinar.core.model

enum class ThemeConfig { FOLLOW_SYSTEM, LIGHT, DARK }

/** The unit amounts are shown in. Values are always stored in rial. */
enum class CurrencyUnit(val rialFactor: Long) {
    TOMAN(10),
    RIAL(1),
}

data class UserPreferences(
    val userName: String = "",
    val themeConfig: ThemeConfig = ThemeConfig.FOLLOW_SYSTEM,
    val useDynamicColor: Boolean = false,
    val currencyUnit: CurrencyUnit = CurrencyUnit.TOMAN,
    val usePersianDigits: Boolean = true,
    val hideBalances: Boolean = false,
    val hasCompletedOnboarding: Boolean = false,
)

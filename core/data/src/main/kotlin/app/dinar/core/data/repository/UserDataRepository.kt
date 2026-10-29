package app.dinar.core.data.repository

import app.dinar.core.model.CurrencyUnit
import app.dinar.core.model.ThemeConfig
import app.dinar.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow

interface UserDataRepository {
    val userPreferences: Flow<UserPreferences>
    suspend fun setUserName(name: String)
    suspend fun setThemeConfig(theme: ThemeConfig)
    suspend fun setDynamicColor(enabled: Boolean)
    suspend fun setCurrencyUnit(unit: CurrencyUnit)
    suspend fun setPersianDigits(enabled: Boolean)
    suspend fun setHideBalances(hidden: Boolean)
    suspend fun setOnboardingCompleted(done: Boolean)
}

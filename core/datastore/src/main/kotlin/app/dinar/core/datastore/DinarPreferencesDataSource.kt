package app.dinar.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import app.dinar.core.model.CurrencyUnit
import app.dinar.core.model.ThemeConfig
import app.dinar.core.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

/** Single source of truth for user preferences, backed by Preferences DataStore. */
class DinarPreferencesDataSource @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) {
    private object Keys {
        val userName = stringPreferencesKey("user_name")
        val theme = stringPreferencesKey("theme_config")
        val dynamicColor = booleanPreferencesKey("use_dynamic_color")
        val currency = stringPreferencesKey("currency_unit")
        val persianDigits = booleanPreferencesKey("use_persian_digits")
        val hideBalances = booleanPreferencesKey("hide_balances")
        val onboarding = booleanPreferencesKey("has_completed_onboarding")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data
        .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e }
        .map { p ->
            UserPreferences(
                userName = p[Keys.userName].orEmpty(),
                themeConfig = p[Keys.theme].toEnum(ThemeConfig.FOLLOW_SYSTEM),
                useDynamicColor = p[Keys.dynamicColor] ?: false,
                currencyUnit = p[Keys.currency].toEnum(CurrencyUnit.TOMAN),
                usePersianDigits = p[Keys.persianDigits] ?: true,
                hideBalances = p[Keys.hideBalances] ?: false,
                hasCompletedOnboarding = p[Keys.onboarding] ?: false,
            )
        }

    suspend fun setUserName(name: String) = dataStore.edit { it[Keys.userName] = name.trim() }
    suspend fun setThemeConfig(theme: ThemeConfig) = dataStore.edit { it[Keys.theme] = theme.name }
    suspend fun setDynamicColor(enabled: Boolean) = dataStore.edit { it[Keys.dynamicColor] = enabled }
    suspend fun setCurrencyUnit(unit: CurrencyUnit) = dataStore.edit { it[Keys.currency] = unit.name }
    suspend fun setPersianDigits(enabled: Boolean) = dataStore.edit { it[Keys.persianDigits] = enabled }
    suspend fun setHideBalances(hidden: Boolean) = dataStore.edit { it[Keys.hideBalances] = hidden }
    suspend fun setOnboardingCompleted(done: Boolean) = dataStore.edit { it[Keys.onboarding] = done }
}

private inline fun <reified T : Enum<T>> String?.toEnum(default: T): T =
    this?.let { name -> enumValues<T>().firstOrNull { it.name == name } } ?: default

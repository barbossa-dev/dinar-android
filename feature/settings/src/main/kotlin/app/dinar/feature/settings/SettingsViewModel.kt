package app.dinar.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dinar.core.common.format.MoneyFormatter
import app.dinar.core.data.repository.AccountsRepository
import app.dinar.core.data.repository.TransactionsRepository
import app.dinar.core.data.repository.UserDataRepository
import app.dinar.core.model.Account
import app.dinar.core.model.AccountType
import app.dinar.core.model.AccountWithBalance
import app.dinar.core.model.CurrencyUnit
import app.dinar.core.model.ThemeConfig
import app.dinar.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SettingsUiState {
    data object Loading : SettingsUiState
    data class Success(
        val preferences: UserPreferences,
        val accounts: List<AccountWithBalance>,
    ) : SettingsUiState
}

private val ACCOUNT_COLORS = listOf(0xFF0B7A5E, 0xFF5B8DEF, 0xFFE0B35F, 0xFFD16BA5, 0xFFE07A5F, 0xFF2A9D8F)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userData: UserDataRepository,
    private val accounts: AccountsRepository,
    private val transactions: TransactionsRepository,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        userData.userPreferences,
        accounts.observeAccountsWithBalance(),
    ) { prefs, list -> SettingsUiState.Success(prefs, list) as SettingsUiState }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading)

    private val _exports = Channel<String>(Channel.BUFFERED)

    /** CSV documents ready to be shared. */
    val exports = _exports.receiveAsFlow()

    fun setUserName(name: String) = launch { userData.setUserName(name) }
    fun setTheme(theme: ThemeConfig) = launch { userData.setThemeConfig(theme) }
    fun setDynamicColor(enabled: Boolean) = launch { userData.setDynamicColor(enabled) }
    fun setCurrency(unit: CurrencyUnit) = launch { userData.setCurrencyUnit(unit) }
    fun setPersianDigits(enabled: Boolean) = launch { userData.setPersianDigits(enabled) }
    fun setHideBalances(hidden: Boolean) = launch { userData.setHideBalances(hidden) }

    fun addAccount(name: String, type: AccountType, initialBalanceText: String) = launch {
        val unit = (uiState.value as? SettingsUiState.Success)?.preferences?.currencyUnit ?: CurrencyUnit.TOMAN
        val count = (uiState.value as? SettingsUiState.Success)?.accounts?.size ?: 0
        accounts.save(
            Account(
                id = 0,
                name = name.trim(),
                type = type,
                initialBalance = MoneyFormatter.toRial(initialBalanceText, unit) ?: 0,
                colorArgb = ACCOUNT_COLORS[count % ACCOUNT_COLORS.size],
            ),
        )
    }

    fun archiveAccount(id: Long) = launch { accounts.archive(id) }

    fun exportCsv() = launch { _exports.send(transactions.exportCsv()) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}

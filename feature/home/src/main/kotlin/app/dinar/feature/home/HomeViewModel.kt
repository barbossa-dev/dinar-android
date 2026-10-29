package app.dinar.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dinar.core.data.repository.UserDataRepository
import app.dinar.core.domain.Dashboard
import app.dinar.core.domain.GetDashboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(
        val greeting: String,
        val userName: String,
        val hideBalances: Boolean,
        val dashboard: Dashboard,
    ) : HomeUiState
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    getDashboard: GetDashboardUseCase,
    private val userData: UserDataRepository,
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        getDashboard(),
        userData.userPreferences,
    ) { dashboard, prefs ->
        HomeUiState.Success(
            greeting = greetingFor(LocalTime.now()),
            userName = prefs.userName,
            hideBalances = prefs.hideBalances,
            dashboard = dashboard,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState.Loading,
    )

    fun setHideBalances(hidden: Boolean) {
        viewModelScope.launch { userData.setHideBalances(hidden) }
    }

    private fun greetingFor(time: LocalTime): String = when (time.hour) {
        in 5..11 -> "صبح بخیر"
        in 12..16 -> "ظهر بخیر"
        in 17..20 -> "عصر بخیر"
        else -> "شب بخیر"
    }
}

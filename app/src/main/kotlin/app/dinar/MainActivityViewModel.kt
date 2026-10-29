package app.dinar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dinar.core.data.repository.UserDataRepository
import app.dinar.core.model.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface MainActivityUiState {
    data object Loading : MainActivityUiState
    data class Success(val preferences: UserPreferences) : MainActivityUiState
}

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    userData: UserDataRepository,
) : ViewModel() {
    val uiState: StateFlow<MainActivityUiState> = userData.userPreferences
        .map<UserPreferences, MainActivityUiState>(MainActivityUiState::Success)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainActivityUiState.Loading)
}

package app.dinar.feature.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dinar.core.common.time.JalaliDate
import app.dinar.core.domain.GetMonthlyReportUseCase
import app.dinar.core.domain.Report
import app.dinar.core.domain.ReportPeriod
import app.dinar.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed interface ReportsUiState {
    data object Loading : ReportsUiState
    data class Success(
        val report: Report,
        /** Which side of the ledger the breakdown shows. */
        val breakdown: TransactionType,
    ) : ReportsUiState
}

@HiltViewModel
class ReportsViewModel @Inject constructor(
    getReport: GetMonthlyReportUseCase,
) : ViewModel() {

    private val period = MutableStateFlow(ReportPeriod.MONTH)
    private val anchor = MutableStateFlow(JalaliDate.today())
    private val breakdown = MutableStateFlow(TransactionType.EXPENSE)

    val uiState: StateFlow<ReportsUiState> =
        combine(period, anchor, ::Pair)
            .flatMapLatest { (p, a) -> getReport(a, p) }
            .combine(breakdown) { report, side -> ReportsUiState.Success(report, side) as ReportsUiState }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ReportsUiState.Loading)

    fun setPeriod(value: ReportPeriod) { period.value = value }
    fun setBreakdown(value: TransactionType) { breakdown.value = value }
    fun previous() { anchor.value = anchor.value.plusMonths(if (period.value == ReportPeriod.YEAR) -12 else -1) }
    fun next() { anchor.value = anchor.value.plusMonths(if (period.value == ReportPeriod.YEAR) 12 else 1) }
}

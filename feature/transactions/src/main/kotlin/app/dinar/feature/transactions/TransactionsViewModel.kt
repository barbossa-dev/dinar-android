package app.dinar.feature.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.dinar.core.common.time.JalaliDate
import app.dinar.core.common.time.monthRange
import app.dinar.core.data.repository.TransactionsRepository
import app.dinar.core.model.Transaction
import app.dinar.core.model.TransactionDetails
import app.dinar.core.model.TransactionFilter
import app.dinar.core.model.TransactionType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

data class DayGroup(
    val date: JalaliDate,
    val items: List<TransactionDetails>,
    /** Net of income and expense on this day. */
    val net: Long,
)

data class TransactionsUiState(
    val month: JalaliDate,
    val query: String,
    val typeFilter: TransactionType?,
    val groups: List<DayGroup>,
    val income: Long,
    val expense: Long,
    val isLoading: Boolean,
) {
    val isEmpty: Boolean get() = !isLoading && groups.isEmpty()
    val isFiltering: Boolean get() = query.isNotBlank() || typeFilter != null
}

@OptIn(FlowPreview::class)
@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactions: TransactionsRepository,
) : ViewModel() {

    private val month = MutableStateFlow(JalaliDate.today().firstDayOfMonth())
    private val query = MutableStateFlow("")
    private val typeFilter = MutableStateFlow<TransactionType?>(null)

    /** The last deleted transaction, kept so the user can undo. */
    private var lastDeleted: Transaction? = null

    private val results = combine(month, query.debounce(250), typeFilter, ::Triple)
        .flatMapLatest { (m, q, type) ->
            transactions.search(TransactionFilter(query = q, type = type), m.monthRange())
        }

    val uiState: StateFlow<TransactionsUiState> = combine(month, query, typeFilter, results) { m, q, type, rows ->
        val zone = ZoneId.systemDefault()
        TransactionsUiState(
            month = m,
            query = q,
            typeFilter = type,
            groups = rows
                .groupBy { JalaliDate.fromInstant(it.transaction.occurredAt, zone) }
                .map { (date, items) -> DayGroup(date, items, items.sumOf { it.signedAmount }) },
            income = rows.filter { it.transaction.type == TransactionType.INCOME }.sumOf { it.transaction.amount },
            expense = rows.filter { it.transaction.type == TransactionType.EXPENSE }.sumOf { it.transaction.amount },
            isLoading = false,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        TransactionsUiState(month.value, "", null, emptyList(), 0, 0, isLoading = true),
    )

    fun onQueryChange(value: String) { query.value = value }
    fun onTypeFilter(type: TransactionType?) { typeFilter.value = type }
    fun previousMonth() { month.value = month.value.plusMonths(-1) }
    fun nextMonth() { month.value = month.value.plusMonths(1) }

    fun delete(details: TransactionDetails) {
        lastDeleted = details.transaction
        viewModelScope.launch { transactions.delete(details.transaction.id) }
    }

    fun undoDelete() {
        val restored = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch { transactions.save(restored) }
    }
}

package app.dinar.feature.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dinar.core.common.format.DateFormatter
import app.dinar.core.common.time.JalaliDate
import app.dinar.core.designsystem.component.EmptyState
import app.dinar.core.designsystem.icon.DinarIcons
import app.dinar.core.designsystem.theme.DinarTheme
import app.dinar.core.model.TransactionDetails
import app.dinar.core.model.TransactionType
import app.dinar.core.ui.AmountText
import app.dinar.core.ui.LocalDisplaySettings
import app.dinar.core.ui.TransactionRow
import kotlinx.coroutines.launch

@Composable
internal fun TransactionsRoute(
    onTransactionClick: (Long) -> Unit,
    onAddTransaction: () -> Unit,
    viewModel: TransactionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    TransactionsScreen(
        state = state,
        onTransactionClick = onTransactionClick,
        onAddTransaction = onAddTransaction,
        onQueryChange = viewModel::onQueryChange,
        onTypeFilter = viewModel::onTypeFilter,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onDelete = viewModel::delete,
        onUndoDelete = viewModel::undoDelete,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TransactionsScreen(
    state: TransactionsUiState,
    onTransactionClick: (Long) -> Unit,
    onAddTransaction: () -> Unit,
    onQueryChange: (String) -> Unit,
    onTypeFilter: (TransactionType?) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDelete: (TransactionDetails) -> Unit,
    onUndoDelete: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val settings = LocalDisplaySettings.current
    val today = JalaliDate.today()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("تراکنش‌ها") },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(DinarIcons.Add, contentDescription = "تراکنش جدید")
            }
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 88.dp,
            ),
        ) {
            item("search") {
                OutlinedTextField(
                    value = state.query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                    placeholder = { Text("جستجو در یادداشت‌ها و دسته‌ها") },
                    leadingIcon = { Icon(DinarIcons.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(DinarIcons.Close, contentDescription = "پاک کردن جستجو")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                )
            }
            item("month") {
                MonthSwitcher(state.month, onPreviousMonth, onNextMonth)
            }
            item("filters") {
                TypeFilters(state.typeFilter, onTypeFilter)
            }
            item("summary") {
                MonthSummary(state.income, state.expense)
            }
            if (state.isEmpty) {
                item("empty") {
                    EmptyState(
                        icon = if (state.isFiltering) DinarIcons.Search else DinarIcons.Receipt,
                        title = if (state.isFiltering) "نتیجه‌ای پیدا نشد" else "این ماه تراکنشی ندارد",
                        body = if (state.isFiltering) "عبارت یا فیلتر دیگری را امتحان کن." else "با دکمه‌ی + اولین تراکنش ماه را ثبت کن.",
                    )
                }
            }
            state.groups.forEach { group ->
                stickyHeader(key = "h-${group.date}") {
                    DayHeader(group, today)
                }
                items(group.items, key = { it.transaction.id }) { details ->
                    SwipeToDelete(
                        onDelete = {
                            onDelete(details)
                            scope.launch {
                                val result = snackbar.showSnackbar(
                                    message = "تراکنش حذف شد",
                                    actionLabel = "بازگردانی",
                                    duration = SnackbarDuration.Short,
                                )
                                if (result == SnackbarResult.ActionPerformed) onUndoDelete()
                            }
                        },
                    ) {
                        TransactionRow(
                            details = details,
                            onClick = { onTransactionClick(details.transaction.id) },
                            today = today,
                            showDate = false,
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthSwitcher(month: JalaliDate, onPrevious: () -> Unit, onNext: () -> Unit) {
    val settings = LocalDisplaySettings.current
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onPrevious) { Icon(DinarIcons.PreviousMonth, contentDescription = "ماه قبل") }
        Text(
            settings.monthYear(month),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).semantics { heading() },
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        IconButton(onClick = onNext) { Icon(DinarIcons.NextMonth, contentDescription = "ماه بعد") }
    }
}

@Composable
private fun TypeFilters(selected: TransactionType?, onSelect: (TransactionType?) -> Unit) {
    val options = listOf(
        null to "همه",
        TransactionType.EXPENSE to "هزینه",
        TransactionType.INCOME to "درآمد",
        TransactionType.TRANSFER to "انتقال",
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(options) { (type, label) ->
            FilterChip(
                selected = selected == type,
                onClick = { onSelect(type) },
                label = { Text(label) },
                leadingIcon = if (selected == type) {
                    { Icon(DinarIcons.Check, contentDescription = null) }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun MonthSummary(income: Long, expense: Long) {
    val colors = DinarTheme.extendedColors
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
        shape = MaterialTheme.shapes.large,
    ) {
        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCell("درآمد", income, colors.income, Modifier.weight(1f))
            SummaryCell("هزینه", expense, colors.expense, Modifier.weight(1f))
            SummaryCell("خالص", income - expense, MaterialTheme.colorScheme.onSurface, Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryCell(label: String, rial: Long, color: androidx.compose.ui.graphics.Color, modifier: Modifier) {
    Column(modifier) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        AmountText(rial, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = color)
    }
}

@Composable
private fun DayHeader(group: DayGroup, today: JalaliDate) {
    val settings = LocalDisplaySettings.current
    val weekday = JalaliDate.WEEKDAY_NAMES[group.date.toLocalDate().dayOfWeek.value - 1]
    val label = when (group.date) {
        today -> "امروز"
        today.plusDays(-1) -> "دیروز"
        else -> "$weekday ${DateFormatter.dayMonth(group.date, settings.persianDigits)}"
    }
    Row(
        Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        AmountText(
            group.net,
            signed = true,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SwipeToDelete(onDelete: () -> Unit, content: @Composable () -> Unit) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(DinarIcons.Delete, contentDescription = "حذف", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        },
    ) { content() }
}

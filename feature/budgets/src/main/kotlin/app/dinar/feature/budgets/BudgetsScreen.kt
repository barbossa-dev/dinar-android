package app.dinar.feature.budgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dinar.core.common.format.MoneyFormatter
import app.dinar.core.common.format.toLatinDigits
import app.dinar.core.designsystem.component.DinarProgressBar
import app.dinar.core.designsystem.component.EmptyState
import app.dinar.core.designsystem.component.IconBadge
import app.dinar.core.designsystem.icon.DinarIcons
import app.dinar.core.designsystem.theme.DinarTheme
import app.dinar.core.model.BudgetProgress
import app.dinar.core.model.Category
import app.dinar.core.ui.AmountText
import app.dinar.core.ui.LocalDisplaySettings
import app.dinar.core.ui.color
import app.dinar.core.ui.imageVector

/** What the bottom sheet is editing: an existing budget, or a new one. */
private data class SheetTarget(val existing: BudgetProgress?)

@Composable
internal fun BudgetsRoute(viewModel: BudgetsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BudgetsScreen(state, viewModel::saveBudget, viewModel::removeBudget)
}

@Composable
internal fun BudgetsScreen(
    state: BudgetsUiState,
    onSave: (Long, String) -> Unit,
    onRemove: (Long) -> Unit,
) {
    var sheet by remember { mutableStateOf<SheetTarget?>(null) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("بودجه") }) },
        floatingActionButton = {
            if (state is BudgetsUiState.Success && state.unbudgeted.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { sheet = SheetTarget(null) },
                    icon = { Icon(DinarIcons.Add, contentDescription = null) },
                    text = { Text("بودجه جدید") },
                )
            }
        },
    ) { padding ->
        when (state) {
            BudgetsUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            is BudgetsUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 96.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                if (state.items.isEmpty()) {
                    item {
                        EmptyState(
                            icon = DinarIcons.Budget,
                            title = "هنوز بودجه‌ای تعیین نکرده‌ای",
                            body = "برای هر دسته سقف خرج ماهانه بگذار تا دینار قبل از عبور از آن خبرت کند.",
                        )
                    }
                } else {
                    item { OverviewCard(state) }
                    items(state.items, key = { it.category.id }) { item ->
                        BudgetRow(item, onClick = { sheet = SheetTarget(item) })
                    }
                }
            }
        }
    }

    val current = sheet
    if (current != null && state is BudgetsUiState.Success) {
        BudgetSheet(
            target = current,
            categories = state.unbudgeted,
            onDismiss = { sheet = null },
            onSave = { id, amount -> onSave(id, amount); sheet = null },
            onRemove = { id -> onRemove(id); sheet = null },
        )
    }
}

@Composable
private fun OverviewCard(state: BudgetsUiState.Success) {
    val settings = LocalDisplaySettings.current
    val colors = DinarTheme.extendedColors
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.balanceCard, contentColor = colors.onBalanceCard),
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("بودجه‌ی ${settings.monthYear(state.month)}", style = MaterialTheme.typography.labelLarge, color = colors.onBalanceCardMuted)
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                AmountText(state.totalLimit - state.totalSpent, style = MaterialTheme.typography.headlineMedium)
                Text("${settings.unitLabel} باقی‌مانده", style = MaterialTheme.typography.bodySmall, color = colors.onBalanceCardMuted, modifier = Modifier.padding(bottom = 6.dp))
            }
            DinarProgressBar(
                state.fraction,
                color = MaterialTheme.colorScheme.inversePrimary,
                trackColor = colors.balanceCardChip,
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${settings.amount(state.totalSpent)} از ${settings.amount(state.totalLimit)}",
                    style = MaterialTheme.typography.labelMedium,
                )
                Text("${settings.number(state.daysLeft)} روز مانده", style = MaterialTheme.typography.labelMedium)
            }
            if (state.dailyAllowance > 0) {
                Text(
                    "می‌توانی روزانه تا ${settings.amount(state.dailyAllowance)} ${settings.unitLabel} خرج کنی.",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onBalanceCardMuted,
                )
            }
        }
    }
}

@Composable
private fun BudgetRow(item: BudgetProgress, onClick: () -> Unit) {
    val settings = LocalDisplaySettings.current
    val colors = DinarTheme.extendedColors
    val statusColor = when {
        item.isOverBudget -> MaterialTheme.colorScheme.error
        item.isNearLimit -> colors.warning
        else -> item.category.color
    }
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconBadge(item.category.icon.imageVector, item.category.color)
                Column(Modifier.weight(1f)) {
                    Text(item.category.name, style = MaterialTheme.typography.titleSmall)
                    Text(
                        when {
                            item.isOverBudget -> "${settings.amount(-item.remaining)} بیشتر از بودجه"
                            else -> "${settings.amount(item.remaining)} ${settings.unitLabel} باقی‌مانده"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = if (item.isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    settings.percent(item.fraction),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = statusColor,
                )
            }
            DinarProgressBar(item.fraction, color = statusColor)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AmountText(item.spent, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                AmountText(item.limit, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BudgetSheet(
    target: SheetTarget,
    categories: List<Category>,
    onDismiss: () -> Unit,
    onSave: (Long, String) -> Unit,
    onRemove: (Long) -> Unit,
) {
    val settings = LocalDisplaySettings.current
    val existing = target.existing
    var categoryId by rememberSaveable { mutableStateOf(existing?.category?.id ?: categories.firstOrNull()?.id) }
    var amount by rememberSaveable {
        mutableStateOf(existing?.let { (it.limit / settings.currencyUnit.rialFactor).toString() }.orEmpty())
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                if (existing == null) "بودجه جدید" else "بودجه‌ی ${existing.category.name}",
                style = MaterialTheme.typography.titleLarge,
            )
            if (existing == null) {
                Text("دسته", style = MaterialTheme.typography.labelLarge)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { c ->
                        FilterChip(
                            selected = c.id == categoryId,
                            onClick = { categoryId = c.id },
                            label = { Text(c.name) },
                            leadingIcon = { Icon(c.icon.imageVector, contentDescription = null, tint = c.color) },
                        )
                    }
                }
            }
            val preview = MoneyFormatter.toRial(amount, settings.currencyUnit)?.let { settings.amount(it) }
            OutlinedTextField(
                value = amount,
                onValueChange = { value -> amount = value.toLatinDigits().filter(Char::isDigit).take(13) },
                label = { Text("سقف ماهانه (${settings.unitLabel})") },
                supportingText = { preview?.let { Text(it + " " + settings.unitLabel) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (existing != null) {
                    OutlinedButton(onClick = { onRemove(existing.category.id) }, modifier = Modifier.weight(1f)) {
                        Text("حذف بودجه", color = MaterialTheme.colorScheme.error)
                    }
                }
                Button(
                    onClick = { categoryId?.let { onSave(it, amount) } },
                    enabled = categoryId != null && amount.isNotEmpty() && amount.trimStart('0').isNotEmpty(),
                    modifier = Modifier.weight(1f),
                ) { Text("ذخیره") }
            }
            Box(Modifier.padding(bottom = 8.dp))
        }
    }
}

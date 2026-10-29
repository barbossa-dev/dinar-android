package app.dinar.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dinar.core.designsystem.component.DinarProgressBar
import app.dinar.core.designsystem.component.EmptyState
import app.dinar.core.designsystem.component.IconBadge
import app.dinar.core.designsystem.component.SectionHeader
import app.dinar.core.designsystem.icon.DinarIcons
import app.dinar.core.designsystem.theme.DinarTheme
import app.dinar.core.domain.Dashboard
import app.dinar.core.model.AccountWithBalance
import app.dinar.core.model.TransactionType
import app.dinar.core.ui.AmountText
import app.dinar.core.ui.LocalDisplaySettings
import app.dinar.core.ui.TransactionRow
import app.dinar.core.ui.imageVector
import app.dinar.core.common.time.JalaliDate

@Composable
internal fun HomeRoute(
    onAddTransaction: (TransactionType) -> Unit,
    onTransactionClick: (Long) -> Unit,
    onSeeAllTransactions: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBudgets: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HomeScreen(
        state = state,
        onAddTransaction = onAddTransaction,
        onTransactionClick = onTransactionClick,
        onSeeAllTransactions = onSeeAllTransactions,
        onOpenSettings = onOpenSettings,
        onOpenBudgets = onOpenBudgets,
        onToggleHideBalances = viewModel::setHideBalances,
    )
}

@Composable
internal fun HomeScreen(
    state: HomeUiState,
    onAddTransaction: (TransactionType) -> Unit,
    onTransactionClick: (Long) -> Unit,
    onSeeAllTransactions: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenBudgets: () -> Unit,
    onToggleHideBalances: (Boolean) -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    if (state is HomeUiState.Success) {
                        Column {
                            Text(
                                state.greeting,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                state.userName.ifBlank { "دینار" },
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                },
                actions = {
                    if (state is HomeUiState.Success) {
                        IconButton(onClick = { onToggleHideBalances(!state.hideBalances) }) {
                            Icon(
                                if (state.hideBalances) DinarIcons.Show else DinarIcons.Hide,
                                contentDescription = if (state.hideBalances) "نمایش مبالغ" else "پنهان کردن مبالغ",
                            )
                        }
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(DinarIcons.Settings, contentDescription = "تنظیمات")
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onAddTransaction(TransactionType.EXPENSE) },
                icon = { Icon(DinarIcons.Add, contentDescription = null) },
                text = { Text("تراکنش جدید") },
            )
        },
    ) { padding ->
        when (state) {
            HomeUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            is HomeUiState.Success -> HomeContent(
                dashboard = state.dashboard,
                contentPadding = padding,
                onAddTransaction = onAddTransaction,
                onTransactionClick = onTransactionClick,
                onSeeAllTransactions = onSeeAllTransactions,
                onOpenBudgets = onOpenBudgets,
            )
        }
    }
}

@Composable
private fun HomeContent(
    dashboard: Dashboard,
    contentPadding: PaddingValues,
    onAddTransaction: (TransactionType) -> Unit,
    onTransactionClick: (Long) -> Unit,
    onSeeAllTransactions: () -> Unit,
    onOpenBudgets: () -> Unit,
) {
    val today = JalaliDate.today()
    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding() + 4.dp,
            bottom = contentPadding.calculateBottomPadding() + 96.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item("balance") {
            BalanceCard(dashboard, onOpenBudgets, Modifier.padding(horizontal = 16.dp))
        }
        item("actions") {
            QuickActions(onAddTransaction, Modifier.padding(horizontal = 16.dp))
        }
        if (dashboard.accounts.isNotEmpty()) {
            item("accounts") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionHeader("حساب‌ها", Modifier.padding(horizontal = 16.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(dashboard.accounts, key = { it.account.id }) { AccountCard(it) }
                    }
                }
            }
        }
        item("recent") {
            Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                SectionHeader("تراکنش‌های اخیر", actionLabel = "مشاهده همه", onAction = onSeeAllTransactions)
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
                    shape = MaterialTheme.shapes.large,
                ) {
                    if (dashboard.recent.isEmpty()) {
                        EmptyState(
                            icon = DinarIcons.Receipt,
                            title = "هنوز تراکنشی ثبت نشده",
                            body = "اولین هزینه یا درآمدت را ثبت کن تا دینار برایت حساب‌وکتاب کند.",
                        )
                    } else {
                        dashboard.recent.forEachIndexed { index, details ->
                            TransactionRow(
                                details = details,
                                onClick = { onTransactionClick(details.transaction.id) },
                                today = today,
                            )
                            if (index < dashboard.recent.lastIndex) {
                                HorizontalDivider(
                                    Modifier.padding(start = 72.dp, end = 16.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(dashboard: Dashboard, onOpenBudgets: () -> Unit, modifier: Modifier = Modifier) {
    val colors = DinarTheme.extendedColors
    val settings = LocalDisplaySettings.current
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = colors.balanceCard, contentColor = colors.onBalanceCard),
    ) {
        Column(Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("موجودی کل", style = MaterialTheme.typography.labelLarge, color = colors.onBalanceCardMuted)
                Text(
                    settings.monthYear(dashboard.month),
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(colors.balanceCardChip)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                )
            }
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AmountText(
                    rial = dashboard.totalBalance,
                    style = MaterialTheme.typography.displaySmall,
                    maskable = true,
                )
                Text(
                    settings.unitLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.onBalanceCardMuted,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FlowStat("درآمد این ماه", dashboard.monthIncome, DinarIcons.Income, Modifier.weight(1f))
                FlowStat("هزینه این ماه", dashboard.monthExpense, DinarIcons.Expense, Modifier.weight(1f))
            }
            if (dashboard.budgetLimit > 0) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.semantics(mergeDescendants = true) {},
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("بودجه‌ی ماه", style = MaterialTheme.typography.labelMedium, color = colors.onBalanceCardMuted)
                        Text(
                            "${settings.percent(dashboard.budgetFraction)} مصرف شده",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                    DinarProgressBar(
                        fraction = dashboard.budgetFraction,
                        color = if (dashboard.budgetFraction > 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.inversePrimary,
                        trackColor = colors.balanceCardChip,
                        height = 6.dp,
                    )
                }
            } else {
                FilledTonalButton(onClick = onOpenBudgets) { Text("تعیین بودجه ماهانه") }
            }
        }
    }
}

@Composable
private fun FlowStat(label: String, rial: Long, icon: ImageVector, modifier: Modifier = Modifier) {
    val colors = DinarTheme.extendedColors
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(colors.balanceCardChip),
            contentAlignment = Alignment.Center,
        ) { Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp)) }
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.onBalanceCardMuted)
            AmountText(rial, style = MaterialTheme.typography.titleSmall, maskable = true)
        }
    }
}

@Composable
private fun QuickActions(onAddTransaction: (TransactionType) -> Unit, modifier: Modifier = Modifier) {
    val colors = DinarTheme.extendedColors
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickAction("هزینه", DinarIcons.Expense, colors.expense, Modifier.weight(1f)) {
            onAddTransaction(TransactionType.EXPENSE)
        }
        QuickAction("درآمد", DinarIcons.Income, colors.income, Modifier.weight(1f)) {
            onAddTransaction(TransactionType.INCOME)
        }
        QuickAction("انتقال", DinarIcons.Transfer, colors.transfer, Modifier.weight(1f)) {
            onAddTransaction(TransactionType.TRANSFER)
        }
    }
}

@Composable
private fun QuickAction(label: String, icon: ImageVector, tint: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            IconBadge(icon, tint, size = 40.dp)
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AccountCard(item: AccountWithBalance) {
    val tint = Color(item.account.colorArgb)
    Card(
        modifier = Modifier.width(168.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            IconBadge(item.account.type.imageVector, tint, size = 36.dp)
            Text(item.account.name, style = MaterialTheme.typography.labelLarge, maxLines = 1)
            AmountText(item.balance, style = MaterialTheme.typography.titleMedium, maskable = true)
        }
    }
}

@Preview
@Composable
private fun HomeLoadingPreview() {
    DinarTheme {
        HomeScreen(HomeUiState.Loading, {}, {}, {}, {}, {}, {})
    }
}

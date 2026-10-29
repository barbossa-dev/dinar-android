package app.dinar.feature.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dinar.core.common.time.JalaliDate
import app.dinar.core.designsystem.component.BarGroup
import app.dinar.core.designsystem.component.DinarProgressBar
import app.dinar.core.designsystem.component.DonutChart
import app.dinar.core.designsystem.component.DonutSlice
import app.dinar.core.designsystem.component.EmptyState
import app.dinar.core.designsystem.component.GroupedBarChart
import app.dinar.core.designsystem.component.IconBadge
import app.dinar.core.designsystem.icon.DinarIcons
import app.dinar.core.designsystem.theme.DinarTheme
import app.dinar.core.domain.Report
import app.dinar.core.domain.ReportPeriod
import app.dinar.core.model.CategoryTotal
import app.dinar.core.model.TransactionType
import app.dinar.core.ui.AmountText
import app.dinar.core.ui.LocalDisplaySettings
import app.dinar.core.ui.color
import app.dinar.core.ui.imageVector

@Composable
internal fun ReportsRoute(viewModel: ReportsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ReportsScreen(
        state = state,
        onPeriodChange = viewModel::setPeriod,
        onBreakdownChange = viewModel::setBreakdown,
        onPrevious = viewModel::previous,
        onNext = viewModel::next,
    )
}

@Composable
internal fun ReportsScreen(
    state: ReportsUiState,
    onPeriodChange: (ReportPeriod) -> Unit,
    onBreakdownChange: (TransactionType) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    Scaffold(topBar = { TopAppBar(title = { Text("گزارش‌ها") }) }) { padding ->
        when (state) {
            ReportsUiState.Loading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            is ReportsUiState.Success -> LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 24.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                item { PeriodSelector(state.report.period, onPeriodChange) }
                item { PeriodSwitcher(state.report, onPrevious, onNext) }
                item { SummaryCards(state.report) }
                item { BreakdownCard(state.report, state.breakdown, onBreakdownChange) }
                item { TrendCard(state.report) }
                item { InsightsCard(state.report) }
            }
        }
    }
}

@Composable
private fun PeriodSelector(selected: ReportPeriod, onChange: (ReportPeriod) -> Unit) {
    val options = listOf(ReportPeriod.MONTH to "ماهانه", ReportPeriod.YEAR to "سالانه")
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { i, (period, label) ->
            SegmentedButton(
                selected = period == selected,
                onClick = { onChange(period) },
                shape = SegmentedButtonDefaults.itemShape(i, options.size),
            ) { Text(label) }
        }
    }
}

@Composable
private fun PeriodSwitcher(report: Report, onPrevious: () -> Unit, onNext: () -> Unit) {
    val settings = LocalDisplaySettings.current
    val title = when (report.period) {
        ReportPeriod.MONTH -> settings.monthYear(report.anchor)
        ReportPeriod.YEAR -> "سال ${settings.number(report.anchor.year)}"
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onPrevious) { Icon(DinarIcons.PreviousMonth, contentDescription = "دوره قبل") }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f).semantics { heading() },
        )
        IconButton(onClick = onNext) { Icon(DinarIcons.NextMonth, contentDescription = "دوره بعد") }
    }
}

@Composable
private fun SummaryCards(report: Report) {
    val colors = DinarTheme.extendedColors
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        StatCard(
            label = "درآمد",
            rial = report.income,
            change = report.change(report.income, report.previousIncome),
            positiveIsGood = true,
            accent = colors.income,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "هزینه",
            rial = report.expense,
            change = report.change(report.expense, report.previousExpense),
            positiveIsGood = false,
            accent = colors.expense,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    rial: Long,
    change: Float?,
    positiveIsGood: Boolean,
    accent: Color,
    modifier: Modifier,
) {
    val settings = LocalDisplaySettings.current
    val colors = DinarTheme.extendedColors
    ReportCard(modifier) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(8.dp).background(accent, RoundedCornerShape(2.dp)))
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AmountText(rial, style = MaterialTheme.typography.titleLarge)
            if (change != null) {
                val good = (change >= 0) == positiveIsGood
                Text(
                    text = (if (change >= 0) "▲ " else "▼ ") + settings.percent(kotlin.math.abs(change)) + " نسبت به قبل",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (good) colors.income else colors.expense,
                )
            } else {
                Text(" ", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun BreakdownCard(report: Report, side: TransactionType, onChange: (TransactionType) -> Unit) {
    val settings = LocalDisplaySettings.current
    val totals = if (side == TransactionType.INCOME) report.incomeByCategory else report.expenseByCategory
    val total = if (side == TransactionType.INCOME) report.income else report.expense
    ReportCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("بر اساس دسته", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                TextButton(onClick = { onChange(TransactionType.EXPENSE) }, enabled = side != TransactionType.EXPENSE) {
                    Text("هزینه‌ها")
                }
                TextButton(onClick = { onChange(TransactionType.INCOME) }, enabled = side != TransactionType.INCOME) {
                    Text("درآمدها")
                }
            }
            if (totals.isEmpty()) {
                EmptyState(DinarIcons.Reports, "داده‌ای برای نمایش نیست", "در این دوره تراکنشی از این نوع ثبت نشده.")
            } else {
                val top = groupSmallSlices(totals)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    DonutChart(
                        slices = top.map { DonutSlice(it.second.toFloat(), it.third) },
                        contentDescription = top.joinToString("، ") { "${it.first} ${settings.percent(it.second.toFloat() / total)}" },
                        modifier = Modifier.size(150.dp),
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(settings.compact(total), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                            Text(settings.unitLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        top.forEach { (name, amount, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(Modifier.size(10.dp).background(color, RoundedCornerShape(3.dp)))
                                Text(name, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), maxLines = 1)
                                Text(
                                    settings.percent(amount.toFloat() / total),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                }
                totals.take(5).forEach { CategoryBar(it) }
            }
        }
    }
}

/** Keeps the five biggest slices and folds the rest into «سایر». */
@Composable
private fun groupSmallSlices(totals: List<CategoryTotal>): List<Triple<String, Long, Color>> {
    val head = totals.take(5).map { Triple(it.category.name, it.total, it.category.color) }
    val rest = totals.drop(5).sumOf { it.total }
    return if (rest > 0) head + Triple("سایر", rest, MaterialTheme.colorScheme.outline) else head
}

@Composable
private fun CategoryBar(item: CategoryTotal) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        IconBadge(item.category.icon.imageVector, item.category.color, size = 36.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.category.name, style = MaterialTheme.typography.labelLarge)
                AmountText(item.total, style = MaterialTheme.typography.labelLarge)
            }
            DinarProgressBar(item.share, color = item.category.color, height = 6.dp)
        }
    }
}

@Composable
private fun TrendCard(report: Report) {
    val settings = LocalDisplaySettings.current
    val colors = DinarTheme.extendedColors
    val incomeColor = colors.income
    val expenseColor = colors.expense
    ReportCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (report.period == ReportPeriod.YEAR) "ماه‌های سال" else "شش ماه اخیر",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                Legend("درآمد", incomeColor)
                Legend("هزینه", expenseColor)
            }
            GroupedBarChart(
                groups = report.trend.map { m ->
                    BarGroup(
                        label = JalaliDate.MONTH_NAMES[m.month - 1].take(3),
                        values = listOf(m.income.toFloat(), m.expense.toFloat()),
                    )
                },
                seriesColors = listOf(incomeColor, expenseColor),
                contentDescription = report.trend.joinToString("، ") {
                    "${JalaliDate.MONTH_NAMES[it.month - 1]}: درآمد ${settings.compact(it.income)}، هزینه ${settings.compact(it.expense)}"
                },
                highlightIndex = report.trend.indexOfFirst { it.month == report.anchor.month && it.year == report.anchor.year }
                    .takeIf { it >= 0 } ?: report.trend.lastIndex,
            )
        }
    }
}

@Composable
private fun Legend(label: String, color: Color) {
    Row(
        Modifier.padding(start = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(Modifier.size(8.dp).background(color, RoundedCornerShape(2.dp)))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InsightsCard(report: Report) {
    val settings = LocalDisplaySettings.current
    val colors = DinarTheme.extendedColors
    ReportCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("نکته‌ها", style = MaterialTheme.typography.titleSmall)
            Insight("میانگین هزینه‌ی روزانه", settings.amount(report.dailyAverageExpense) + " " + settings.unitLabel)
            Insight(
                "نرخ پس‌انداز",
                settings.percent(report.savingsRate),
                if (report.savingsRate >= 0) colors.income else colors.expense,
            )
            Insight("مانده‌ی دوره", settings.signed(report.net) + " " + settings.unitLabel)
            report.expenseByCategory.firstOrNull()?.let {
                Insight("بیشترین هزینه", "${it.category.name} (${settings.percent(it.share)})")
            }
        }
    }
}

@Composable
private fun Insight(label: String, value: String, valueColor: Color = MaterialTheme.colorScheme.onSurface) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun ReportCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) { content() }
}

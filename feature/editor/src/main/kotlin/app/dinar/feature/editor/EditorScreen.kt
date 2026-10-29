package app.dinar.feature.editor

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dinar.core.common.format.MoneyFormatter
import app.dinar.core.common.format.toPersianDigits
import app.dinar.core.designsystem.component.IconBadge
import app.dinar.core.designsystem.icon.DinarIcons
import app.dinar.core.designsystem.theme.DinarTheme
import app.dinar.core.model.Account
import app.dinar.core.model.Category
import app.dinar.core.model.TransactionType
import app.dinar.core.ui.JalaliDatePickerDialog
import app.dinar.core.ui.LocalDisplaySettings
import app.dinar.core.ui.color
import app.dinar.core.ui.imageVector
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width

@Composable
internal fun EditorRoute(
    onClose: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { onClose() }
    }
    EditorScreen(
        state = state,
        onClose = onClose,
        onTypeChange = viewModel::onTypeChange,
        onDigit = viewModel::onDigit,
        onTripleZero = viewModel::onTripleZero,
        onBackspace = viewModel::onBackspace,
        onClearAmount = viewModel::onClearAmount,
        onCategorySelect = viewModel::onCategorySelect,
        onAccountSelect = viewModel::onAccountSelect,
        onToAccountSelect = viewModel::onToAccountSelect,
        onNoteChange = viewModel::onNoteChange,
        onDateChange = viewModel::onDateChange,
        onSave = viewModel::save,
        onDelete = viewModel::delete,
        onErrorShown = viewModel::onErrorShown,
    )
}

@Composable
internal fun EditorScreen(
    state: EditorUiState,
    onClose: () -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onDigit: (Char) -> Unit,
    onTripleZero: () -> Unit,
    onBackspace: () -> Unit,
    onClearAmount: () -> Unit,
    onCategorySelect: (Long) -> Unit,
    onAccountSelect: (Long) -> Unit,
    onToAccountSelect: (Long) -> Unit,
    onNoteChange: (String) -> Unit,
    onDateChange: (app.dinar.core.common.time.JalaliDate) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onErrorShown: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.error) {
        state.error?.let {
            snackbar.showSnackbar(it)
            onErrorShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "ویرایش تراکنش" else "تراکنش جدید") },
                navigationIcon = {
                    IconButton(onClick = onClose) { Icon(DinarIcons.Close, contentDescription = "بستن") }
                },
                actions = {
                    if (state.isEditing) {
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(DinarIcons.Delete, contentDescription = "حذف تراکنش")
                        }
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TypeSelector(state.form.type, onTypeChange)
            AmountDisplay(state, onClearAmount)
            DetailsRow(
                state = state,
                onDateClick = { showDatePicker = true },
                onAccountSelect = onAccountSelect,
                onToAccountSelect = onToAccountSelect,
            )
            Box(Modifier.weight(1f)) {
                if (state.form.type == TransactionType.TRANSFER) {
                    TransferHint(state.selectedAccount, state.selectedToAccount)
                } else {
                    CategoryGrid(state.categories, state.form.categoryId, onCategorySelect)
                }
            }
            OutlinedTextField(
                value = state.form.note,
                onValueChange = onNoteChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("یادداشت (اختیاری)") },
                leadingIcon = { Icon(DinarIcons.Note, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
            )
            Keypad(onDigit, onTripleZero, onBackspace)
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 0.dp),
                shape = MaterialTheme.shapes.large,
            ) {
                Icon(DinarIcons.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(if (state.isEditing) "ذخیره تغییرات" else "ثبت تراکنش", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.height(4.dp))
        }
    }

    if (showDatePicker) {
        JalaliDatePickerDialog(
            initial = state.form.date,
            onDismiss = { showDatePicker = false },
            onConfirm = {
                onDateChange(it)
                showDatePicker = false
            },
        )
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = { Icon(DinarIcons.Delete, contentDescription = null) },
            title = { Text("حذف تراکنش؟") },
            text = { Text("این تراکنش برای همیشه حذف می‌شود و موجودی حساب‌ها دوباره محاسبه می‌شود.") },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text("حذف", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("انصراف") } },
        )
    }
}

@Composable
private fun TypeSelector(selected: TransactionType, onChange: (TransactionType) -> Unit) {
    val options = listOf(
        TransactionType.EXPENSE to "هزینه",
        TransactionType.INCOME to "درآمد",
        TransactionType.TRANSFER to "انتقال",
    )
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
        options.forEachIndexed { index, (type, label) ->
            SegmentedButton(
                selected = selected == type,
                onClick = { onChange(type) },
                shape = SegmentedButtonDefaults.itemShape(index, options.size),
            ) { Text(label) }
        }
    }
}

@Composable
private fun AmountDisplay(state: EditorUiState, onClear: () -> Unit) {
    val settings = LocalDisplaySettings.current
    val colors = DinarTheme.extendedColors
    val digits = state.form.amountDigits
    val formatted = digits.toLongOrNull()?.let {
        val text = MoneyFormatter.amount(it * state.unit.rialFactor, state.unit, settings.persianDigits)
        text
    } ?: if (settings.persianDigits) "۰" else "0"
    val color = when {
        digits.isEmpty() -> MaterialTheme.colorScheme.onSurfaceVariant
        state.form.type == TransactionType.INCOME -> colors.income
        state.form.type == TransactionType.EXPENSE -> colors.expense
        else -> MaterialTheme.colorScheme.onSurface
    }
    val scale by animateFloatAsState(if (formatted.length > 11) 0.8f else 1f, label = "amountScale")
    Column(
        Modifier.fillMaxWidth().semantics { liveRegion = LiveRegionMode.Polite },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("مبلغ", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AnimatedContent(targetState = formatted, label = "amount") { text ->
                Text(
                    text,
                    style = MaterialTheme.typography.displayMedium.copy(textDirection = TextDirection.Ltr),
                    fontWeight = FontWeight.ExtraBold,
                    color = color,
                    maxLines = 1,
                    modifier = Modifier.scale(scale),
                )
            }
            Text(
                MoneyFormatter.unitLabel(state.unit),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 10.dp),
            )
        }
        if (digits.isNotEmpty()) {
            TextButton(onClick = onClear, contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)) {
                Text("پاک کردن", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun DetailsRow(
    state: EditorUiState,
    onDateClick: () -> Unit,
    onAccountSelect: (Long) -> Unit,
    onToAccountSelect: (Long) -> Unit,
) {
    val settings = LocalDisplaySettings.current
    Row(
        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        AssistChip(
            onClick = onDateClick,
            label = { Text(settings.fullDate(state.form.date)) },
            leadingIcon = { Icon(DinarIcons.Calendar, contentDescription = null, Modifier.size(18.dp)) },
        )
        AccountChip(
            prefix = if (state.form.type == TransactionType.TRANSFER) "از" else null,
            selected = state.selectedAccount,
            accounts = state.accounts,
            onSelect = onAccountSelect,
        )
        if (state.form.type == TransactionType.TRANSFER) {
            AccountChip(
                prefix = "به",
                selected = state.selectedToAccount,
                accounts = state.accounts,
                onSelect = onToAccountSelect,
            )
        }
    }
}

@Composable
private fun AccountChip(
    prefix: String?,
    selected: Account?,
    accounts: List<Account>,
    onSelect: (Long) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        AssistChip(
            onClick = { expanded = true },
            label = {
                Text(listOfNotNull(prefix, selected?.name ?: "انتخاب حساب").joinToString(" "))
            },
            leadingIcon = {
                Icon(
                    selected?.type?.imageVector ?: DinarIcons.Budget,
                    contentDescription = null,
                    Modifier.size(18.dp),
                )
            },
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text(account.name) },
                    leadingIcon = { Icon(account.type.imageVector, contentDescription = null) },
                    trailingIcon = {
                        if (account.id == selected?.id) Icon(DinarIcons.Check, contentDescription = "انتخاب‌شده")
                    },
                    onClick = {
                        onSelect(account.id)
                        expanded = false
                    },
                )
            }
        }
    }
}

@Composable
private fun CategoryGrid(categories: List<Category>, selectedId: Long?, onSelect: (Long) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 78.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp),
    ) {
        items(categories, key = { it.id }) { category ->
            val isSelected = category.id == selectedId
            OutlinedCard(
                onClick = { onSelect(category.id) },
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) category.color else MaterialTheme.colorScheme.outlineVariant,
                ),
                colors = androidx.compose.material3.CardDefaults.outlinedCardColors(
                    containerColor = if (isSelected) {
                        category.color.copy(alpha = 0.12f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    },
                ),
                modifier = Modifier.semantics { this.selected = isSelected },
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    IconBadge(category.icon.imageVector, category.color, size = 36.dp)
                    Text(
                        category.name,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun TransferHint(from: Account?, to: Account?) {
    val colors = DinarTheme.extendedColors
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            AccountBubble(from)
            IconBadge(DinarIcons.Transfer, colors.transfer, size = 40.dp)
            AccountBubble(to)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "انتقال بین حساب‌ها روی موجودی کل اثری ندارد.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun AccountBubble(account: Account?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        IconBadge(
            account?.type?.imageVector ?: DinarIcons.Budget,
            account?.let { androidx.compose.ui.graphics.Color(it.colorArgb) } ?: MaterialTheme.colorScheme.outline,
            size = 56.dp,
        )
        Text(account?.name ?: "—", style = MaterialTheme.typography.labelLarge)
    }
}

/** A phone-style keypad; always left-to-right like every numeric keypad in Iran. */
@Composable
private fun Keypad(onDigit: (Char) -> Unit, onTripleZero: () -> Unit, onBackspace: () -> Unit) {
    val haptics = LocalHapticFeedback.current
    val persian = LocalDisplaySettings.current.persianDigits
    val rows = listOf("123", "456", "789")
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            rows.forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { d ->
                        Key(if (persian) d.toString().toPersianDigits() else d.toString(), Modifier.weight(1f)) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDigit(d)
                        }
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Key(if (persian) "۰۰۰" else "000", Modifier.weight(1f)) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onTripleZero()
                }
                Key(if (persian) "۰" else "0", Modifier.weight(1f)) {
                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onDigit('0')
                }
                Surface(
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBackspace()
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(DinarIcons.Backspace, contentDescription = "پاک کردن رقم آخر")
                    }
                }
            }
        }
    }
}

@Composable
private fun Key(label: String, modifier: Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLowest,
        tonalElevation = 1.dp,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(label, style = MaterialTheme.typography.titleLarge)
        }
    }
}

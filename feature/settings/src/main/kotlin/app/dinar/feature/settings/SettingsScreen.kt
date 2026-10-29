package app.dinar.feature.settings

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.dinar.core.common.format.toLatinDigits
import app.dinar.core.designsystem.component.IconBadge
import app.dinar.core.designsystem.icon.DinarIcons
import app.dinar.core.model.AccountType
import app.dinar.core.model.AccountWithBalance
import app.dinar.core.model.CurrencyUnit
import app.dinar.core.model.ThemeConfig
import app.dinar.core.model.UserPreferences
import app.dinar.core.ui.AmountText
import app.dinar.core.ui.imageVector
import app.dinar.core.ui.label
import java.io.File

@Composable
internal fun SettingsRoute(onBack: () -> Unit, viewModel: SettingsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(viewModel) {
        viewModel.exports.collect { csv -> shareCsv(context, csv) }
    }
    SettingsScreen(
        state = state,
        onBack = onBack,
        onUserName = viewModel::setUserName,
        onTheme = viewModel::setTheme,
        onDynamicColor = viewModel::setDynamicColor,
        onCurrency = viewModel::setCurrency,
        onPersianDigits = viewModel::setPersianDigits,
        onHideBalances = viewModel::setHideBalances,
        onAddAccount = viewModel::addAccount,
        onArchiveAccount = viewModel::archiveAccount,
        onExport = viewModel::exportCsv,
    )
}

@Composable
internal fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onUserName: (String) -> Unit,
    onTheme: (ThemeConfig) -> Unit,
    onDynamicColor: (Boolean) -> Unit,
    onCurrency: (CurrencyUnit) -> Unit,
    onPersianDigits: (Boolean) -> Unit,
    onHideBalances: (Boolean) -> Unit,
    onAddAccount: (String, AccountType, String) -> Unit,
    onArchiveAccount: (Long) -> Unit,
    onExport: () -> Unit,
) {
    var editName by rememberSaveable { mutableStateOf(false) }
    var addAccount by rememberSaveable { mutableStateOf(false) }
    var archiveTarget by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(DinarIcons.Back, contentDescription = "بازگشت") }
                },
            )
        },
    ) { padding ->
        if (state !is SettingsUiState.Success) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) { CircularProgressIndicator() }
            return@Scaffold
        }
        val prefs = state.preferences
        LazyColumn(
            contentPadding = PaddingValues(
                start = 16.dp, end = 16.dp,
                top = padding.calculateTopPadding(),
                bottom = padding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionTitle("پروفایل") }
            item {
                SettingsCard {
                    SettingRow(
                        icon = DinarIcons.Person,
                        title = "نام",
                        subtitle = prefs.userName.ifBlank { "تعیین نشده" },
                        onClick = { editName = true },
                    )
                }
            }

            item { SectionTitle("ظاهر") }
            item { SettingsCard { ThemeSelector(prefs, onTheme, onDynamicColor) } }

            item { SectionTitle("نمایش اعداد") }
            item {
                SettingsCard {
                    CurrencySelector(prefs.currencyUnit, onCurrency)
                    Divider()
                    SwitchRow(DinarIcons.Digits, "ارقام فارسی", "۱۲۳ به‌جای 123", prefs.usePersianDigits, onPersianDigits)
                    Divider()
                    SwitchRow(DinarIcons.Hide, "پنهان کردن مبالغ", "برای استفاده در جمع", prefs.hideBalances, onHideBalances)
                }
            }

            item { SectionTitle("حساب‌ها") }
            item {
                SettingsCard {
                    state.accounts.forEachIndexed { i, item ->
                        AccountRow(item, canArchive = state.accounts.size > 1, onArchive = { archiveTarget = item.account.id })
                        if (i < state.accounts.lastIndex) Divider()
                    }
                    Divider()
                    SettingRow(DinarIcons.Add, "افزودن حساب", null, onClick = { addAccount = true })
                }
            }

            item { SectionTitle("داده‌ها") }
            item {
                SettingsCard {
                    SettingRow(DinarIcons.Export, "خروجی CSV", "همه‌ی تراکنش‌ها برای اکسل یا Google Sheets", onClick = onExport)
                }
            }
            item {
                Text(
                    "دینار · نسخه ۱٫۰ · همه‌ی داده‌ها فقط روی همین دستگاه ذخیره می‌شوند.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }
        }

        if (editName) {
            NameDialog(prefs.userName, onDismiss = { editName = false }, onSave = { onUserName(it); editName = false })
        }
        if (addAccount) {
            AddAccountDialog(
                unitLabel = if (prefs.currencyUnit == CurrencyUnit.TOMAN) "تومان" else "ریال",
                onDismiss = { addAccount = false },
                onSave = { name, type, balance -> onAddAccount(name, type, balance); addAccount = false },
            )
        }
        archiveTarget?.let { id ->
            AlertDialog(
                onDismissRequest = { archiveTarget = null },
                title = { Text("بایگانی حساب؟") },
                text = { Text("حساب از فهرست‌ها پنهان می‌شود ولی تراکنش‌های قبلی آن باقی می‌مانند.") },
                confirmButton = { TextButton(onClick = { onArchiveAccount(id); archiveTarget = null }) { Text("بایگانی") } },
                dismissButton = { TextButton(onClick = { archiveTarget = null }) { Text("انصراف") } },
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, top = 12.dp).semantics { heading() },
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLowest),
    ) { Column { content() } }
}

@Composable
private fun Divider() = HorizontalDivider(Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant)

private val itemColors @Composable get() = ListItemDefaults.colors(containerColor = Color.Transparent)

@Composable
private fun SettingRow(icon: ImageVector, title: String, subtitle: String?, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = subtitle?.let { { Text(it) } },
        leadingContent = { Icon(icon, contentDescription = null) },
        colors = itemColors,
        modifier = Modifier.clickable(role = Role.Button, onClick = onClick),
    )
}

@Composable
private fun SwitchRow(icon: ImageVector, title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Switch(checked = checked, onCheckedChange = null) },
        colors = itemColors,
        modifier = Modifier.clickable(role = Role.Switch) { onChange(!checked) },
    )
}

@Composable
private fun ThemeSelector(prefs: UserPreferences, onTheme: (ThemeConfig) -> Unit, onDynamicColor: (Boolean) -> Unit) {
    val options = listOf(
        ThemeConfig.FOLLOW_SYSTEM to "مطابق سیستم",
        ThemeConfig.LIGHT to "روشن",
        ThemeConfig.DARK to "تیره",
    )
    Column(Modifier.selectableGroup()) {
        options.forEach { (theme, label) ->
            ListItem(
                headlineContent = { Text(label) },
                leadingContent = { RadioButton(selected = prefs.themeConfig == theme, onClick = null) },
                colors = itemColors,
                modifier = Modifier.selectable(
                    selected = prefs.themeConfig == theme,
                    role = Role.RadioButton,
                    onClick = { onTheme(theme) },
                ),
            )
        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        Divider()
        SwitchRow(DinarIcons.Palette, "رنگ‌های پویا", "هماهنگ با والپیپر (Material You)", prefs.useDynamicColor, onDynamicColor)
    }
}

@Composable
private fun CurrencySelector(selected: CurrencyUnit, onSelect: (CurrencyUnit) -> Unit) {
    ListItem(
        headlineContent = { Text("واحد پول") },
        leadingContent = { Icon(DinarIcons.Currency, contentDescription = null) },
        trailingContent = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected == CurrencyUnit.TOMAN, { onSelect(CurrencyUnit.TOMAN) }, { Text("تومان") })
                FilterChip(selected == CurrencyUnit.RIAL, { onSelect(CurrencyUnit.RIAL) }, { Text("ریال") })
            }
        },
        colors = itemColors,
    )
}

@Composable
private fun AccountRow(item: AccountWithBalance, canArchive: Boolean, onArchive: () -> Unit) {
    ListItem(
        headlineContent = { Text(item.account.name) },
        supportingContent = { AmountText(item.balance, style = MaterialTheme.typography.bodyMedium, maskable = true) },
        leadingContent = { IconBadge(item.account.type.imageVector, Color(item.account.colorArgb), size = 40.dp) },
        trailingContent = {
            if (canArchive) {
                IconButton(onClick = onArchive) { Icon(DinarIcons.Delete, contentDescription = "بایگانی ${item.account.name}") }
            }
        },
        colors = itemColors,
    )
}

@Composable
private fun NameDialog(initial: String, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by rememberSaveable { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("نام شما") },
        text = {
            OutlinedTextField(name, { name = it.take(40) }, singleLine = true, placeholder = { Text("مثلاً سارا") })
        },
        confirmButton = { TextButton(onClick = { onSave(name) }) { Text("ذخیره") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddAccountDialog(unitLabel: String, onDismiss: () -> Unit, onSave: (String, AccountType, String) -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(AccountType.BANK_CARD) }
    var balance by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("حساب جدید") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it.take(30) }, label = { Text("نام حساب") }, singleLine = true)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccountType.entries.forEach { t ->
                        FilterChip(
                            selected = type == t,
                            onClick = { type = t },
                            label = { Text(t.label) },
                            leadingIcon = { Icon(t.imageVector, contentDescription = null) },
                        )
                    }
                }
                OutlinedTextField(
                    balance,
                    { balance = it.toLatinDigits().filter(Char::isDigit).take(13) },
                    label = { Text("موجودی فعلی ($unitLabel)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, type, balance) }, enabled = name.isNotBlank()) { Text("افزودن") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("انصراف") } },
    )
}

private fun shareCsv(context: Context, csv: String) {
    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    // A UTF-8 BOM makes Excel open Persian text correctly.
    val file = File(dir, "dinar-transactions.csv").apply { writeText("﻿" + csv) }
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.exports", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری خروجی"))
}

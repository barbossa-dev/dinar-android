package app.dinar.feature.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import app.dinar.core.common.format.MoneyFormatter
import app.dinar.core.common.time.JalaliDate
import app.dinar.core.data.repository.AccountsRepository
import app.dinar.core.data.repository.CategoriesRepository
import app.dinar.core.data.repository.TransactionsRepository
import app.dinar.core.data.repository.UserDataRepository
import app.dinar.core.model.Account
import app.dinar.core.model.Category
import app.dinar.core.model.CategoryKind
import app.dinar.core.model.CurrencyUnit
import app.dinar.core.model.Transaction
import app.dinar.core.model.TransactionType
import app.dinar.feature.editor.navigation.EditorRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.ZoneId
import javax.inject.Inject

/** What the user has entered so far. Amount digits are in the display unit. */
data class EditorForm(
    val type: TransactionType,
    val amountDigits: String = "",
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val toAccountId: Long? = null,
    val note: String = "",
    val date: JalaliDate = JalaliDate.today(),
    val time: LocalTime = LocalTime.now(),
)

data class EditorUiState(
    val isEditing: Boolean,
    val isLoading: Boolean,
    val form: EditorForm,
    val categories: List<Category>,
    val accounts: List<Account>,
    val unit: CurrencyUnit,
    val error: String? = null,
) {
    val selectedCategory: Category? = categories.firstOrNull { it.id == form.categoryId }
    val selectedAccount: Account? = accounts.firstOrNull { it.id == form.accountId }
    val selectedToAccount: Account? = accounts.firstOrNull { it.id == form.toAccountId }
    val canSave: Boolean
        get() = form.amountDigits.trimStart('0').isNotEmpty() && selectedAccount != null &&
            when (form.type) {
                TransactionType.TRANSFER -> selectedToAccount != null && form.toAccountId != form.accountId
                else -> selectedCategory != null
            }
}

sealed interface EditorEvent {
    data object Saved : EditorEvent
    data object Deleted : EditorEvent
}

@HiltViewModel
class EditorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val transactions: TransactionsRepository,
    private val categories: CategoriesRepository,
    accounts: AccountsRepository,
    userData: UserDataRepository,
) : ViewModel() {

    private val route = savedStateHandle.toRoute<EditorRoute>()
    private val editingId = route.transactionId.takeIf { it > 0 }

    private val form = MutableStateFlow(EditorForm(type = route.transactionType))
    private val loading = MutableStateFlow(editingId != null)
    private val error = MutableStateFlow<String?>(null)

    private val _events = Channel<EditorEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<EditorUiState> = combine(
        form,
        categories.observeCategories(),
        accounts.observeAccounts(),
        userData.userPreferences,
        combine(loading, error, ::Pair),
    ) { f, allCategories, accountList, prefs, (isLoading, err) ->
        val kind = if (f.type == TransactionType.INCOME) CategoryKind.INCOME else CategoryKind.EXPENSE
        EditorUiState(
            isEditing = editingId != null,
            isLoading = isLoading,
            form = f,
            categories = allCategories.filter { it.kind == kind },
            accounts = accountList,
            unit = prefs.currencyUnit,
            error = err,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EditorUiState(
            isEditing = editingId != null,
            isLoading = true,
            form = form.value,
            categories = emptyList(),
            accounts = emptyList(),
            unit = CurrencyUnit.TOMAN,
        ),
    )

    init {
        viewModelScope.launch {
            val unit = userData.userPreferences.first().currencyUnit
            val accountList = accounts.observeAccounts().first()
            if (editingId != null) {
                transactions.get(editingId)?.let { details ->
                    val t = details.transaction
                    val zone = ZoneId.systemDefault()
                    form.value = EditorForm(
                        type = t.type,
                        amountDigits = (t.amount / unit.rialFactor).toString(),
                        categoryId = t.categoryId,
                        accountId = t.accountId,
                        toAccountId = t.toAccountId,
                        note = t.note,
                        date = JalaliDate.fromInstant(t.occurredAt, zone),
                        time = t.occurredAt.atZone(zone).toLocalTime(),
                    )
                }
                loading.value = false
            } else {
                form.update {
                    it.copy(
                        accountId = accountList.firstOrNull()?.id,
                        toAccountId = accountList.getOrNull(1)?.id,
                    )
                }
            }
            ensureCategorySelected()
        }
    }

    fun onTypeChange(type: TransactionType) {
        form.update { it.copy(type = type, categoryId = null) }
        viewModelScope.launch { ensureCategorySelected() }
    }

    fun onDigit(digit: Char) = form.update {
        val next = (it.amountDigits + digit).trimStart('0')
        if (next.length > 13) it else it.copy(amountDigits = next)
    }

    fun onTripleZero() = form.update {
        if (it.amountDigits.isEmpty() || it.amountDigits.length > 10) it else it.copy(amountDigits = it.amountDigits + "000")
    }

    fun onBackspace() = form.update { it.copy(amountDigits = it.amountDigits.dropLast(1)) }
    fun onClearAmount() = form.update { it.copy(amountDigits = "") }
    fun onCategorySelect(id: Long) = form.update { it.copy(categoryId = id) }
    fun onAccountSelect(id: Long) = form.update {
        it.copy(accountId = id, toAccountId = if (it.toAccountId == id) it.accountId else it.toAccountId)
    }
    fun onToAccountSelect(id: Long) = form.update {
        it.copy(toAccountId = id, accountId = if (it.accountId == id) it.toAccountId else it.accountId)
    }
    fun onNoteChange(note: String) = form.update { it.copy(note = note.take(120)) }
    fun onDateChange(date: JalaliDate) = form.update { it.copy(date = date) }
    fun onErrorShown() { error.value = null }

    fun save() {
        val state = uiState.value
        if (!state.canSave) {
            error.value = when {
                state.form.amountDigits.isEmpty() -> "مبلغ را وارد کنید"
                state.form.type == TransactionType.TRANSFER -> "حساب مبدأ و مقصد باید متفاوت باشند"
                else -> "یک دسته‌بندی انتخاب کنید"
            }
            return
        }
        val f = state.form
        val amount = MoneyFormatter.toRial(f.amountDigits, state.unit) ?: return
        val occurredAt = f.date.toLocalDate().atTime(f.time).atZone(ZoneId.systemDefault()).toInstant()
        viewModelScope.launch {
            transactions.save(
                Transaction(
                    id = editingId ?: 0L,
                    type = f.type,
                    amount = amount,
                    categoryId = if (f.type == TransactionType.TRANSFER) null else f.categoryId,
                    accountId = f.accountId!!,
                    toAccountId = if (f.type == TransactionType.TRANSFER) f.toAccountId else null,
                    note = f.note.trim(),
                    occurredAt = occurredAt,
                ),
            )
            _events.send(EditorEvent.Saved)
        }
    }

    fun delete() {
        val id = editingId ?: return
        viewModelScope.launch {
            transactions.delete(id)
            _events.send(EditorEvent.Deleted)
        }
    }

    private suspend fun ensureCategorySelected() {
        val current = form.value
        if (current.type == TransactionType.TRANSFER || current.categoryId != null) return
        val kind = if (current.type == TransactionType.INCOME) CategoryKind.INCOME else CategoryKind.EXPENSE
        val first = categories.observeCategories(kind).first().firstOrNull()
        form.update { if (it.categoryId == null && it.type == current.type) it.copy(categoryId = first?.id) else it }
    }
}

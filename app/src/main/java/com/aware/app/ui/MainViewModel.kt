package com.aware.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aware.app.data.AccountEntity
import com.aware.app.data.AccountKind
import com.aware.app.data.BudgetBucketEntity
import com.aware.app.data.BudgetPeriod
import com.aware.app.data.BudgetScope
import com.aware.app.data.CaptureCandidateEntity
import com.aware.app.data.CategoryEntity
import com.aware.app.data.DashboardSummary
import com.aware.app.data.AwareRepository
import com.aware.app.data.RecurrenceCadence
import com.aware.app.data.RecurringRuleEntity
import com.aware.app.data.TransactionEntity
import com.aware.app.data.TransactionSource
import com.aware.app.data.TransactionStatus
import com.aware.app.data.TransactionType
import com.aware.app.ai.GroqCategorySuggester
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class MainUiState(
    val summary: DashboardSummary = DashboardSummary(),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val pending: List<CaptureCandidateEntity> = emptyList(),
    val budgets: List<BudgetBucketEntity> = emptyList(),
    val recurring: List<RecurringRuleEntity> = emptyList(),
)

class MainViewModel(private val repository: AwareRepository, private val categorySuggester: GroqCategorySuggester) : ViewModel() {
    private val messageEvents = MutableSharedFlow<String>(extraBufferCapacity = 8)
    val messages: SharedFlow<String> = messageEvents.asSharedFlow()
    private val selectedReview = MutableStateFlow<CaptureCandidateEntity?>(null)
    val reviewCandidate: StateFlow<CaptureCandidateEntity?> = selectedReview
    private val aiSuggestionState = MutableStateFlow<String?>(null)
    val aiSuggestion: StateFlow<String?> = aiSuggestionState
    private val groqConfiguredState = MutableStateFlow(categorySuggester.configured())
    val groqConfigured: StateFlow<Boolean> = groqConfiguredState
    private val appLockState = MutableStateFlow(repository.appLockEnabled())
    val appLockEnabled: StateFlow<Boolean> = appLockState
    private val smartNudgesState = MutableStateFlow(repository.smartNudgesEnabled())
    val smartNudgesEnabled: StateFlow<Boolean> = smartNudgesState

    val state: StateFlow<MainUiState> = combine(
        repository.dashboard(), repository.accounts, repository.categories, repository.transactions,
        repository.pending, repository.budgets(), repository.recurring,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        MainUiState(
            summary = values[0] as DashboardSummary,
            accounts = values[1] as List<AccountEntity>,
            categories = values[2] as List<CategoryEntity>,
            transactions = values[3] as List<TransactionEntity>,
            pending = values[4] as List<CaptureCandidateEntity>,
            budgets = values[5] as List<BudgetBucketEntity>,
            recurring = values[6] as List<RecurringRuleEntity>,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    fun openReview(id: Long) = viewModelScope.launch { selectedReview.value = repository.candidate(id) }
    fun closeReview() { selectedReview.value = null }
    fun saveGroqKey(value: String) = launchMutation("Couldn’t save the Groq key", "Groq key saved") {
        categorySuggester.saveKey(value)
        groqConfiguredState.value = value.isNotBlank()
    }
    fun setAppLock(enabled: Boolean) { repository.setAppLock(enabled); appLockState.value = enabled }
    fun setSmartNudges(enabled: Boolean) { repository.setSmartNudges(enabled); smartNudgesState.value = enabled }
    fun suggestCategory(candidate: CaptureCandidateEntity) = viewModelScope.launch {
        aiSuggestionState.value = categorySuggester.suggest(candidate.merchant, state.value.categories.filterNot { it.isIncome }.map { it.name })
    }

    fun confirmCandidate(
        id: Long,
        amountPaise: Long,
        merchant: String,
        type: TransactionType,
        categoryId: Long?,
        accountId: Long?,
        destinationAccountId: Long?,
        learnRule: Boolean,
    ) = launchMutation("Couldn’t add that captured payment") {
        repository.postCandidate(id, amountPaise, merchant, type, categoryId, accountId, destinationAccountId, learnRule)
        selectedReview.value = null
    }

    fun dismissCandidate(id: Long) = launchMutation("Couldn’t discard that captured payment") {
        repository.dismissCandidate(id)
        selectedReview.value = null
    }

    fun addManual(
        amountPaise: Long,
        merchant: String,
        type: TransactionType,
        accountId: Long,
        destinationAccountId: Long?,
        categoryId: Long?,
        note: String,
        tags: String,
        occurredAt: Long = System.currentTimeMillis(),
    ) = launchMutation("Couldn’t add the transaction", "Transaction added") {
        repository.addTransaction(
            TransactionEntity(
                amountPaise = amountPaise,
                type = type,
                source = TransactionSource.MANUAL,
                status = TransactionStatus.POSTED,
                accountId = accountId,
                destinationAccountId = destinationAccountId,
                categoryId = categoryId,
                merchant = merchant.ifBlank { type.name.lowercase().replaceFirstChar(Char::uppercase) },
                note = note,
                tags = tags.split(',').map(String::trim).filter(String::isNotBlank).distinct().joinToString(","),
                occurredAt = occurredAt,
            ),
        )
    }

    fun deleteTransaction(id: Long) = launchMutation("Couldn’t delete the transaction", "Transaction deleted") {
        repository.deleteTransaction(id)
    }

    fun updateTransaction(
        transaction: TransactionEntity,
        amountPaise: Long,
        merchant: String,
        type: TransactionType,
        accountId: Long,
        destinationAccountId: Long?,
        categoryId: Long?,
        note: String,
        tags: String,
        occurredAt: Long,
    ) = launchMutation("Couldn’t save the transaction", "Transaction updated") {
        repository.updateTransaction(
            transaction.copy(
                amountPaise = amountPaise,
                merchant = merchant.ifBlank { type.name.lowercase().replaceFirstChar(Char::uppercase) },
                type = type,
                accountId = accountId,
                destinationAccountId = destinationAccountId.takeIf { type == TransactionType.TRANSFER },
                categoryId = categoryId.takeUnless { type == TransactionType.TRANSFER },
                note = note,
                tags = tags.split(',').map(String::trim).filter(String::isNotBlank).distinct().joinToString(","),
                occurredAt = occurredAt,
            ),
        )
    }

    fun addBudget(
        name: String,
        capPaise: Long,
        scope: BudgetScope,
        categoryId: Long?,
        accountId: Long?,
        payee: String?,
        period: BudgetPeriod,
        startAt: Long,
        endAt: Long?,
        isRecurring: Boolean,
    ) = launchMutation("Couldn’t create the budget", "Budget created") {
        repository.addBudget(BudgetBucketEntity(
            monthKey = java.time.YearMonth.from(java.time.Instant.ofEpochMilli(startAt).atZone(ZoneId.systemDefault())).toString(),
            name = name,
            capPaise = capPaise,
            scope = scope,
            categoryId = categoryId.takeIf { scope == BudgetScope.CATEGORY },
            accountId = accountId.takeIf { scope == BudgetScope.ACCOUNT },
            payee = payee?.trim()?.takeIf(String::isNotEmpty).takeIf { scope == BudgetScope.PAYEE },
            period = period,
            startAt = startAt,
            endAt = endAt,
            isRecurring = isRecurring,
        ))
    }

    fun addAccount(name: String, kind: AccountKind, openingBalancePaise: Long) = launchMutation("Couldn’t add the account", "Account added") {
        repository.addAccount(AccountEntity(name = name, kind = kind, openingBalancePaise = openingBalancePaise))
    }

    fun saveAccount(id: Long, name: String, kind: AccountKind, openingBalancePaise: Long, isDefault: Boolean) = launchMutation("Couldn’t save the account", "Account saved") {
        val existing = state.value.accounts.firstOrNull { it.id == id }
        repository.saveAccount(
            AccountEntity(
                id = id,
                name = name.trim(),
                kind = kind,
                openingBalancePaise = openingBalancePaise,
                currency = existing?.currency ?: "INR",
                isDefault = isDefault,
                createdAt = existing?.createdAt ?: System.currentTimeMillis(),
            ),
        )
    }

    fun addCategory(name: String, emoji: String, colorArgb: Long, isIncome: Boolean) = launchMutation("Couldn’t add the category", "Category added") {
        repository.addCategory(CategoryEntity(name = name.trim(), emoji = emoji.ifBlank { "✨" }, colorArgb = colorArgb, isIncome = isIncome))
    }

    fun addRecurring(name: String, amountPaise: Long, type: TransactionType, accountId: Long, categoryId: Long?, cadence: RecurrenceCadence, customIntervalDays: Int, startAt: Long, endAt: Long?, reminderMinutesBefore: Int) = launchMutation("Couldn’t add the recurring item", "Recurring item added") {
        repository.addRecurring(RecurringRuleEntity(name = name, amountPaise = amountPaise, type = type, accountId = accountId, categoryId = categoryId, cadence = cadence, customIntervalDays = customIntervalDays, startAt = startAt, endAt = endAt, nextExpectedAt = startAt, reminderMinutesBefore = reminderMinutesBefore))
    }

    private fun launchMutation(
        failureMessage: String,
        successMessage: String? = null,
        operation: suspend () -> Unit,
    ) = viewModelScope.launch {
        runCatching { operation() }
            .onSuccess { successMessage?.let { messageEvents.emit(it) } }
            .onFailure { error ->
                val detail = error.message
                    ?.takeIf { it.isNotBlank() && !it.contains("SQL", ignoreCase = true) }
                    ?.take(100)
                messageEvents.emit(if (detail == null) "$failureMessage. Please try again." else "$failureMessage: $detail")
            }
    }

    class Factory(private val repository: AwareRepository, private val categorySuggester: GroqCategorySuggester) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(repository, categorySuggester) as T
    }
}

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
import com.aware.app.statement.IncorrectStatementPasswordException
import com.aware.app.statement.StatementCandidate
import com.aware.app.statement.StatementImportParser
import com.aware.app.statement.StatementParseResult
import com.aware.app.statement.StatementPasswordRequiredException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

data class MainUiState(
    val summary: DashboardSummary = DashboardSummary(),
    val accounts: List<AccountEntity> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val pending: List<CaptureCandidateEntity> = emptyList(),
    val budgets: List<BudgetBucketEntity> = emptyList(),
    val recurring: List<RecurringRuleEntity> = emptyList(),
)

data class StatementReviewRow(
    val source: StatementCandidate,
    val selected: Boolean,
    val duplicateReason: String? = null,
    val type: TransactionType = source.type,
    val categoryId: Long? = null,
    val destinationAccountId: Long? = null,
)

data class StatementReview(
    val fileName: String,
    val sheetName: String,
    val skippedRows: Int,
    val balanceChecks: Int,
    val balancedMatches: Int,
    val accountId: Long,
    val rows: List<StatementReviewRow>,
    val aiBusy: Boolean = false,
)

sealed interface StatementImportUiState {
    data object Idle : StatementImportUiState
    data class Reading(val fileName: String) : StatementImportUiState
    data class NeedsPassword(val fileName: String, val error: String? = null) : StatementImportUiState
    data class PasswordForgotten(val review: StatementReview) : StatementImportUiState
    data class Reviewing(val review: StatementReview) : StatementImportUiState
}

class MainViewModel(
    private val repository: AwareRepository,
    private val categorySuggester: GroqCategorySuggester,
    private val statementParser: StatementImportParser = StatementImportParser(),
) : ViewModel() {
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
    private val statementImportState = MutableStateFlow<StatementImportUiState>(StatementImportUiState.Idle)
    val statementImport: StateFlow<StatementImportUiState> = statementImportState
    private var pendingStatementBytes: ByteArray? = null
    private var pendingStatementName: String? = null
    private var statementParseJob: Job? = null

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

    fun openStatement(fileName: String, bytes: ByteArray) {
        statementParseJob?.cancel()
        clearPendingStatement()
        val extension = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        if (extension !in setOf("csv", "xls", "xlsx")) {
            bytes.fill(0)
            messageEvents.tryEmit("Choose a CSV, XLS, or XLSX account statement")
            return
        }
        if (bytes.size > StatementImportParser.MAX_FILE_BYTES) {
            bytes.fill(0)
            messageEvents.tryEmit("Statement files must be 15 MB or smaller")
            return
        }
        pendingStatementName = fileName
        pendingStatementBytes = bytes
        statementImportState.value = StatementImportUiState.Reading(fileName)
        statementParseJob = parsePendingStatement(password = null, showPasswordAnimation = false)
    }

    fun unlockStatement(password: CharArray) {
        if (pendingStatementBytes == null || pendingStatementName == null) {
            password.fill('\u0000')
            return
        }
        statementImportState.value = StatementImportUiState.Reading(pendingStatementName.orEmpty())
        statementParseJob?.cancel()
        statementParseJob = parsePendingStatement(password, showPasswordAnimation = true)
    }

    fun finishPasswordAnimation() {
        val current = statementImportState.value as? StatementImportUiState.PasswordForgotten ?: return
        statementImportState.value = StatementImportUiState.Reviewing(current.review)
    }

    fun closeStatementImport() {
        statementParseJob?.cancel()
        clearPendingStatement()
        statementImportState.value = StatementImportUiState.Idle
    }

    fun setStatementAccount(accountId: Long) = updateStatementReview { review ->
        val recalculated = review.rows.map { row ->
            val duplicate = duplicateReason(row.source, row.type, accountId)
            row.copy(
                duplicateReason = duplicate,
                selected = if (duplicate != null) false else row.selected,
                destinationAccountId = row.destinationAccountId.takeUnless { it == accountId },
            )
        }
        review.copy(accountId = accountId, rows = recalculated)
    }

    fun toggleStatementRow(rowNumber: Int) = updateStatementReview { review ->
        review.copy(rows = review.rows.map { row ->
            if (row.source.rowNumber == rowNumber) row.copy(selected = !row.selected) else row
        })
    }

    fun selectNewStatementRows() = updateStatementReview { review ->
        review.copy(rows = review.rows.map { row ->
            row.copy(selected = row.duplicateReason == null && row.source.directionVerified && row.type != TransactionType.TRANSFER)
        })
    }

    fun setStatementType(rowNumber: Int, type: TransactionType) = updateStatementReview { review ->
        review.copy(rows = review.rows.map { row ->
            if (row.source.rowNumber != rowNumber) row else {
                val category = if (type == TransactionType.TRANSFER) null else localCategory(row.source.merchant, type)
                val duplicate = duplicateReason(row.source, type, review.accountId)
                row.copy(
                    type = type,
                    categoryId = category,
                    destinationAccountId = row.destinationAccountId.takeIf { type == TransactionType.TRANSFER },
                    duplicateReason = duplicate,
                    selected = if (duplicate != null) false else row.selected,
                )
            }
        })
    }

    fun setStatementCategory(rowNumber: Int, categoryId: Long?) = updateStatementReview { review ->
        review.copy(rows = review.rows.map { row -> if (row.source.rowNumber == rowNumber) row.copy(categoryId = categoryId) else row })
    }

    fun setStatementDestination(rowNumber: Int, accountId: Long?) = updateStatementReview { review ->
        review.copy(rows = review.rows.map { row -> if (row.source.rowNumber == rowNumber) row.copy(destinationAccountId = accountId) else row })
    }

    fun categoriseStatementWithGroq() = viewModelScope.launch {
        if (!categorySuggester.configured()) {
            messageEvents.emit("Add a Groq key before using AI categorisation")
            return@launch
        }
        val current = (statementImportState.value as? StatementImportUiState.Reviewing)?.review ?: return@launch
        val targets = current.rows.filter { it.selected && it.categoryId == null && it.type != TransactionType.TRANSFER }.take(MAX_AI_IMPORT_ROWS)
        if (targets.isEmpty()) {
            messageEvents.emit("Every selected row already has a category")
            return@launch
        }
        statementImportState.value = StatementImportUiState.Reviewing(current.copy(aiBusy = true))
        var matched = 0
        targets.forEach { target ->
            val isIncome = target.type == TransactionType.INCOME || target.type == TransactionType.REFUND
            val allowed = state.value.categories.filter { it.isIncome == isIncome }.map { it.name }
            val suggestion = categorySuggester.suggest(target.source.merchant, allowed)
            val categoryId = suggestion?.let { name -> state.value.categories.firstOrNull { it.name.equals(name, ignoreCase = true) }?.id }
            if (categoryId != null) {
                matched++
                setStatementCategory(target.source.rowNumber, categoryId)
            }
        }
        updateStatementReview { it.copy(aiBusy = false) }
        messageEvents.emit("Groq suggested $matched ${if (matched == 1) "category" else "categories"}")
    }

    fun importReviewedStatement() = viewModelScope.launch {
        val review = (statementImportState.value as? StatementImportUiState.Reviewing)?.review ?: return@launch
        val selected = review.rows.filter(StatementReviewRow::selected)
        if (selected.isEmpty()) {
            messageEvents.emit("Select at least one statement row")
            return@launch
        }
        if (selected.any { it.type == TransactionType.TRANSFER && (it.destinationAccountId == null || it.destinationAccountId == review.accountId) }) {
            messageEvents.emit("Choose a receiving account for every selected transfer")
            return@launch
        }
        runCatching {
            repository.importTransactions(selected.map { row ->
                TransactionEntity(
                    amountPaise = row.source.amountPaise,
                    type = row.type,
                    source = TransactionSource.STATEMENT,
                    status = TransactionStatus.POSTED,
                    accountId = review.accountId,
                    destinationAccountId = row.destinationAccountId.takeIf { row.type == TransactionType.TRANSFER },
                    categoryId = row.categoryId.takeUnless { row.type == TransactionType.TRANSFER },
                    merchant = row.source.merchant,
                    note = buildString {
                        append("Imported from ").append(review.fileName)
                        row.source.reference?.let { append(" · Ref ").append(it) }
                    },
                    occurredAt = row.source.occurredAt,
                    sourceFingerprint = row.source.fingerprint,
                )
            })
        }.onSuccess { result ->
            statementImportState.value = StatementImportUiState.Idle
            messageEvents.emit(buildString {
                append(result.added).append(if (result.added == 1) " transaction added" else " transactions added")
                if (result.duplicates > 0) append(" · ").append(result.duplicates).append(" duplicate skipped")
            })
        }.onFailure {
            messageEvents.emit("Couldn’t import the statement. Nothing was added.")
        }
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

    fun addCategory(
        name: String,
        emoji: String,
        colorArgb: Long,
        isIncome: Boolean,
        onAdded: (Long) -> Unit = {},
    ) = viewModelScope.launch {
        runCatching {
            repository.addCategory(
                CategoryEntity(
                    name = name.trim(),
                    emoji = emoji.ifBlank { "✨" },
                    colorArgb = colorArgb,
                    isIncome = isIncome,
                ),
            )
        }.onSuccess { id ->
            onAdded(id)
            messageEvents.emit("Category added")
        }.onFailure { error ->
            val detail = error.message
                ?.takeIf { it.isNotBlank() && !it.contains("SQL", ignoreCase = true) }
                ?.take(100)
            messageEvents.emit(
                if (detail == null) "Couldn’t add the category. Please try again."
                else "Couldn’t add the category: $detail",
            )
        }
    }

    fun addRecurring(name: String, amountPaise: Long, type: TransactionType, accountId: Long, categoryId: Long?, cadence: RecurrenceCadence, customIntervalDays: Int, startAt: Long, endAt: Long?, reminderMinutesBefore: Int) = launchMutation("Couldn’t add the recurring item", "Recurring item added") {
        repository.addRecurring(RecurringRuleEntity(name = name, amountPaise = amountPaise, type = type, accountId = accountId, categoryId = categoryId, cadence = cadence, customIntervalDays = customIntervalDays, startAt = startAt, endAt = endAt, nextExpectedAt = startAt, reminderMinutesBefore = reminderMinutesBefore))
    }

    private fun parsePendingStatement(password: CharArray?, showPasswordAnimation: Boolean) = viewModelScope.launch {
        val bytes = pendingStatementBytes ?: run { password?.fill('\u0000'); return@launch }
        val name = pendingStatementName ?: run { password?.fill('\u0000'); return@launch }
        try {
            val parsed = withContext(Dispatchers.Default) { statementParser.parse(name, bytes, password) }
            val review = createStatementReview(parsed)
            clearPendingStatement()
            if (review.rows.isEmpty()) {
                statementImportState.value = StatementImportUiState.Idle
                messageEvents.emit("No usable transactions were found in that statement")
            } else {
                statementImportState.value = if (showPasswordAnimation) {
                    StatementImportUiState.PasswordForgotten(review)
                } else {
                    StatementImportUiState.Reviewing(review)
                }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: StatementPasswordRequiredException) {
            statementImportState.value = StatementImportUiState.NeedsPassword(name)
        } catch (_: IncorrectStatementPasswordException) {
            statementImportState.value = StatementImportUiState.NeedsPassword(name, "That password didn’t unlock this statement")
        } catch (_: Throwable) {
            clearPendingStatement()
            statementImportState.value = StatementImportUiState.Idle
            messageEvents.emit("Couldn’t read that statement. Check its format and try again.")
        } finally {
            password?.fill('\u0000')
        }
    }

    private fun createStatementReview(parsed: StatementParseResult): StatementReview {
        val accountId = state.value.accounts.firstOrNull { it.isDefault }?.id ?: state.value.accounts.firstOrNull()?.id ?: 0L
        val rows = parsed.candidates.map { candidate ->
            val duplicate = duplicateReason(candidate, candidate.type, accountId)
            StatementReviewRow(
                source = candidate,
                selected = duplicate == null && candidate.directionVerified && candidate.type != TransactionType.TRANSFER,
                duplicateReason = duplicate,
                categoryId = localCategory(candidate.merchant, candidate.type),
            )
        }
        return StatementReview(
            fileName = parsed.fileName,
            sheetName = parsed.sheetName,
            skippedRows = parsed.skippedRows,
            balanceChecks = parsed.balanceChecks,
            balancedMatches = parsed.balancedMatches,
            accountId = accountId,
            rows = rows,
        )
    }

    private fun updateStatementReview(transform: (StatementReview) -> StatementReview) {
        val current = statementImportState.value as? StatementImportUiState.Reviewing ?: return
        statementImportState.value = StatementImportUiState.Reviewing(transform(current.review))
    }

    private fun duplicateReason(candidate: StatementCandidate, type: TransactionType, accountId: Long): String? {
        state.value.transactions.firstOrNull { it.sourceFingerprint == candidate.fingerprint }?.let { return "Already imported" }
        val candidateDate = Instant.ofEpochMilli(candidate.occurredAt).atZone(ZoneId.systemDefault()).toLocalDate()
        val candidateWords = merchantWords(candidate.merchant)
        val likely = state.value.transactions.any { transaction ->
            transaction.accountId == accountId && transaction.amountPaise == candidate.amountPaise && transaction.type == type &&
                Instant.ofEpochMilli(transaction.occurredAt).atZone(ZoneId.systemDefault()).toLocalDate() == candidateDate &&
                candidateWords.intersect(merchantWords(transaction.merchant)).isNotEmpty()
        }
        return if (likely) "Matches an existing transaction" else null
    }

    private fun merchantWords(value: String): Set<String> = value.lowercase(Locale.ROOT)
        .replace(Regex("[a-z0-9._%+-]+@[a-z0-9.-]+"), " ")
        .replace(Regex("[^a-z ]"), " ")
        .split(Regex("\\s+"))
        .filter { it.length >= 3 && it !in DUPLICATE_STOP_WORDS }
        .toSet()

    private fun localCategory(merchant: String, type: TransactionType): Long? {
        val income = type == TransactionType.INCOME || type == TransactionType.REFUND
        val available = state.value.categories.filter { it.isIncome == income }
        val text = merchant.lowercase(Locale.ROOT)
        val preferred = if (income) {
            if (listOf("salary", "payroll", "wages").any(text::contains)) "Salary" else "Other income"
        } else when {
            listOf("swiggy", "zomato", "food delivery").any(text::contains) -> "Food delivery"
            listOf("grocery", "groceries", "supermarket", "bigbasket", "blinkit", "zepto").any(text::contains) -> "Groceries"
            listOf("restaurant", "cafe", "coffee", "dining").any(text::contains) -> "Dining"
            listOf("uber", "ola", "metro", "rail", "flight", "petrol", "fuel").any(text::contains) -> "Travel"
            listOf("amazon", "flipkart", "myntra", "shopping").any(text::contains) -> "Shopping"
            listOf("netflix", "spotify", "subscription", "prime video").any(text::contains) -> "Subscriptions"
            listOf("hospital", "pharma", "medical", "clinic", "doctor").any(text::contains) -> "Health"
            listOf("school", "tuition", "course", "university", "education", "book").any(text::contains) -> "Education"
            else -> return null
        }
        return available.firstOrNull { it.name.equals(preferred, ignoreCase = true) }?.id
    }

    private fun clearPendingStatement() {
        pendingStatementBytes?.fill(0)
        pendingStatementBytes = null
        pendingStatementName = null
    }

    override fun onCleared() {
        statementParseJob?.cancel()
        clearPendingStatement()
        super.onCleared()
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

    companion object {
        private const val MAX_AI_IMPORT_ROWS = 25
        private val DUPLICATE_STOP_WORDS = setOf(
            "upi", "bank", "payment", "paid", "debit", "debited", "credit", "credited", "transaction", "transfer", "ref",
        )
    }
}

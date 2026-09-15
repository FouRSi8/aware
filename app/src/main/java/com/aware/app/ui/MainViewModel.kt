package com.aware.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.aware.app.data.AccountEntity
import com.aware.app.data.AccountKind
import com.aware.app.data.BudgetBucketEntity
import com.aware.app.data.BudgetPeriod
import com.aware.app.data.BudgetScope
import com.aware.app.data.CategoryEntity
import com.aware.app.data.DashboardSummary
import com.aware.app.data.AwareRepository
import com.aware.app.data.RecurrenceCadence
import com.aware.app.data.RecurringRuleEntity
import com.aware.app.data.TransactionEntity
import com.aware.app.data.TransactionSource
import com.aware.app.data.TransactionStatus
import com.aware.app.data.TransactionType
import com.aware.app.data.IncomeKind
import com.aware.app.data.ExpenseNature
import com.aware.app.data.MonthlyPlanEntity
import com.aware.app.data.MerchantRuleEntity
import com.aware.app.data.SavingsGoalEntity
import com.aware.app.ai.GroqCategorySuggester
import com.aware.app.ai.GroqCategoryInput
import com.aware.app.ai.MerchantCategorizer
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
    val budgets: List<BudgetBucketEntity> = emptyList(),
    val recurring: List<RecurringRuleEntity> = emptyList(),
    val monthlyPlan: MonthlyPlanEntity? = null,
    val savingsGoals: List<SavingsGoalEntity> = emptyList(),
    val merchantRules: List<MerchantRuleEntity> = emptyList(),
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
        repository.budgets(), repository.recurring, repository.monthlyPlan(), repository.savingsGoals, repository.merchantRules,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        MainUiState(
            summary = values[0] as DashboardSummary,
            accounts = values[1] as List<AccountEntity>,
            categories = values[2] as List<CategoryEntity>,
            transactions = values[3] as List<TransactionEntity>,
            budgets = values[4] as List<BudgetBucketEntity>,
            recurring = values[5] as List<RecurringRuleEntity>,
            monthlyPlan = values[6] as MonthlyPlanEntity?,
            savingsGoals = values[7] as List<SavingsGoalEntity>,
            merchantRules = values[8] as List<MerchantRuleEntity>,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MainUiState())

    fun savedGroqKey(): String = categorySuggester.savedKey()
    fun saveGroqKey(value: String) = launchMutation("Couldn’t save the Groq key", "Groq key saved") {
        categorySuggester.saveKey(value)
        groqConfiguredState.value = value.isNotBlank()
    }
    fun setAppLock(enabled: Boolean) { repository.setAppLock(enabled); appLockState.value = enabled }
    fun setSmartNudges(enabled: Boolean) { repository.setSmartNudges(enabled); smartNudgesState.value = enabled }
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

    fun categoriseStatement() = viewModelScope.launch {
        val current = (statementImportState.value as? StatementImportUiState.Reviewing)?.review ?: return@launch
        val targets = current.rows.filter { it.selected && it.categoryId == null && it.type != TransactionType.TRANSFER }.take(MAX_AI_IMPORT_ROWS)
        if (targets.isEmpty()) {
            messageEvents.emit("Every selected row already has a category")
            return@launch
        }
        statementImportState.value = StatementImportUiState.Reviewing(current.copy(aiBusy = true))
        val snapshot = state.value
        val localMatches = targets.mapNotNull { target ->
            localCategory(target.source.merchant, target.type)?.let { target.source.rowNumber to it }
        }.toMap()
        val unresolved = targets.filterNot { it.source.rowNumber in localMatches }
        val cloudMatches = if (unresolved.isNotEmpty() && categorySuggester.configured()) {
            val cloudSuggestions = categorySuggester.suggestBatch(
                rows = unresolved.map { row ->
                    GroqCategoryInput(row.source.rowNumber, row.source.merchant, categoryUsesIncomeList(row.type))
                },
                expenseCategories = snapshot.categories.filterNot(CategoryEntity::isIncome).map(CategoryEntity::name),
                incomeCategories = snapshot.categories.filter(CategoryEntity::isIncome).map(CategoryEntity::name),
            )
            cloudSuggestions.mapNotNull { (rowId, categoryName) ->
                val row = unresolved.firstOrNull { it.source.rowNumber == rowId } ?: return@mapNotNull null
                snapshot.categories.firstOrNull {
                    it.isIncome == categoryUsesIncomeList(row.type) && it.name.equals(categoryName, ignoreCase = true)
                }?.id?.let { rowId to it }
            }.toMap()
        } else emptyMap()
        val matches = localMatches + cloudMatches
        updateStatementReview { review ->
            review.copy(
                aiBusy = false,
                rows = review.rows.map { row -> row.copy(categoryId = matches[row.source.rowNumber] ?: row.categoryId) },
            )
        }
        val stillUnresolved = unresolved.size - cloudMatches.size
        messageEvents.emit(buildString {
            append(localMatches.size).append(" matched locally")
            if (categorySuggester.configured()) append(" · ").append(cloudMatches.size).append(" by Groq")
            if (stillUnresolved > 0) append(" · ").append(stillUnresolved).append(" need review")
        })
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
                    incomeKind = inferIncomeKind(row.source.merchant, row.type),
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

    fun addManual(
        amountPaise: Long,
        merchant: String,
        type: TransactionType,
        accountId: Long,
        destinationAccountId: Long?,
        categoryId: Long?,
        note: String,
        tags: String,
        incomeKind: IncomeKind?,
        linkedTransactionId: Long?,
        occurredAt: Long = System.currentTimeMillis(),
    ) = launchMutation("Couldn’t add the transaction", "Transaction added") {
        val resolvedCategory = resolveCategory(merchant, type, categoryId)
        repository.addTransaction(
            TransactionEntity(
                amountPaise = amountPaise,
                type = type,
                source = TransactionSource.MANUAL,
                status = TransactionStatus.POSTED,
                accountId = accountId,
                destinationAccountId = destinationAccountId,
                categoryId = resolvedCategory.takeUnless { type == TransactionType.TRANSFER },
                merchant = merchant.ifBlank { type.name.lowercase().replaceFirstChar(Char::uppercase) },
                note = note,
                tags = tags.split(',').map(String::trim).filter(String::isNotBlank).distinct().joinToString(","),
                occurredAt = occurredAt,
                incomeKind = incomeKind.takeIf { type == TransactionType.INCOME },
                linkedTransactionId = linkedTransactionId.takeIf { type == TransactionType.REFUND },
            ),
        )
    }

    private suspend fun resolveCategory(merchant: String, type: TransactionType, selected: Long?): Long? {
        if (selected != null || type in setOf(TransactionType.TRANSFER, TransactionType.ADJUSTMENT) || merchant.isBlank()) return selected
        localCategory(merchant, type)?.let { return it }
        if (!categorySuggester.configured()) return null
        val snapshot = state.value
        val incomeList = categoryUsesIncomeList(type)
        val name = categorySuggester.suggestBatch(
            rows = listOf(GroqCategoryInput(0, merchant, incomeList)),
            expenseCategories = snapshot.categories.filterNot(CategoryEntity::isIncome).map(CategoryEntity::name),
            incomeCategories = snapshot.categories.filter(CategoryEntity::isIncome).map(CategoryEntity::name),
        )[0] ?: return null
        return snapshot.categories.firstOrNull { it.isIncome == incomeList && it.name.equals(name, true) }?.id
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
        incomeKind: IncomeKind?,
        linkedTransactionId: Long?,
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
                incomeKind = incomeKind.takeIf { type == TransactionType.INCOME },
                linkedTransactionId = linkedTransactionId.takeIf { type == TransactionType.REFUND },
            ),
        )
    }

    fun saveBudget(
        existing: BudgetBucketEntity?,
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
    ) = launchMutation("Couldn’t save the budget", if (existing == null) "Budget created" else "Budget updated") {
        repository.addBudget(BudgetBucketEntity(
            id = existing?.id ?: 0L,
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
            allocationPaise = existing?.allocationPaise ?: 0L,
            alert50 = existing?.alert50 ?: true,
            alert75 = existing?.alert75 ?: true,
            alert90 = existing?.alert90 ?: true,
            alert100 = existing?.alert100 ?: true,
        ))
    }

    fun deleteBudget(item: BudgetBucketEntity) = launchMutation("Couldn’t delete the budget", "Budget deleted") {
        repository.deleteBudget(item)
    }

    fun saveMonthlyPlan(expectedIncomePaise: Long, savingsTargetPaise: Long, commitmentTargetPaise: Long) =
        launchMutation("Couldn’t save the monthly plan", "Monthly plan saved") {
            repository.saveMonthlyPlan(
                MonthlyPlanEntity(
                    monthKey = java.time.YearMonth.now().toString(),
                    expectedIncomePaise = expectedIncomePaise,
                    savingsTargetPaise = savingsTargetPaise,
                    commitmentTargetPaise = commitmentTargetPaise,
                ),
            )
        }

    fun saveSavingsGoal(id: Long, name: String, targetPaise: Long, savedPaise: Long, targetAt: Long?) =
        launchMutation("Couldn’t save the goal", "Savings goal saved") {
            val existing = state.value.savingsGoals.firstOrNull { it.id == id }
            repository.saveSavingsGoal(
                SavingsGoalEntity(
                    id = id,
                    name = name.trim(),
                    targetPaise = targetPaise,
                    savedPaise = savedPaise.coerceAtMost(targetPaise),
                    targetAt = targetAt,
                    createdAt = existing?.createdAt ?: System.currentTimeMillis(),
                ),
            )
        }

    fun deleteSavingsGoal(item: SavingsGoalEntity) = launchMutation("Couldn’t delete the goal", "Savings goal deleted") {
        repository.deleteSavingsGoal(item)
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

    fun saveCategory(
        id: Long,
        name: String,
        emoji: String,
        colorArgb: Long,
        isIncome: Boolean,
        expenseNature: ExpenseNature,
        onResult: (Result<Long>) -> Unit,
    ) = viewModelScope.launch {
        val result = runCatching {
            val existing = state.value.categories.firstOrNull { it.id == id }
            repository.saveCategory(
                CategoryEntity(
                    id = id,
                    name = name.trim(),
                    emoji = emoji.ifBlank { "✨" },
                    colorArgb = colorArgb,
                    isIncome = isIncome,
                    isArchived = existing?.isArchived ?: false,
                    expenseNature = if (isIncome) ExpenseNature.DISCRETIONARY else expenseNature,
                ),
            )
        }
        onResult(result)
        result.onSuccess {
            messageEvents.emit(if (id == 0L) "Category added" else "Category updated")
        }.onFailure { error ->
            val detail = error.message
                ?.takeIf { it.isNotBlank() && !it.contains("SQL", ignoreCase = true) }
                ?.take(100)
            messageEvents.emit(
                if (detail == null) "Couldn’t save the category. Please try again."
                else "Couldn’t save the category: $detail",
            )
        }
    }

    fun saveRecurring(existing: RecurringRuleEntity?, name: String, amountPaise: Long, type: TransactionType, accountId: Long, categoryId: Long?, cadence: RecurrenceCadence, customIntervalDays: Int, startAt: Long, endAt: Long?, reminderMinutesBefore: Int) = launchMutation("Couldn’t save the recurring item", if (existing == null) "Recurring item added" else "Recurring item updated") {
        repository.saveRecurring(RecurringRuleEntity(id = existing?.id ?: 0L, name = name, amountPaise = amountPaise, type = type, accountId = accountId, categoryId = categoryId, cadence = cadence, customIntervalDays = customIntervalDays, startAt = startAt, endAt = endAt, nextExpectedAt = startAt, reminderMinutesBefore = reminderMinutesBefore, matchingTolerancePaise = existing?.matchingTolerancePaise ?: 0L, isActive = existing?.isActive ?: true))
    }

    fun deleteRecurring(item: RecurringRuleEntity) = launchMutation("Couldn’t delete the recurring item", "Recurring item deleted") {
        repository.deleteRecurring(item)
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
        val snapshot = state.value
        return MerchantCategorizer.suggest(
            merchant = merchant,
            isIncome = categoryUsesIncomeList(type),
            categories = snapshot.categories,
            rules = snapshot.merchantRules,
            history = snapshot.transactions,
        )?.takeIf { it.confidence >= MerchantCategorizer.AUTO_APPLY_CONFIDENCE }?.categoryId
    }

    private fun categoryUsesIncomeList(type: TransactionType): Boolean =
        type == TransactionType.INCOME || type == TransactionType.REFUND

    private fun inferIncomeKind(merchant: String, type: TransactionType): IncomeKind? = when {
        type != TransactionType.INCOME -> null
        listOf("salary", "payroll", "wages").any { merchant.contains(it, ignoreCase = true) } -> IncomeKind.SALARY
        else -> null
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

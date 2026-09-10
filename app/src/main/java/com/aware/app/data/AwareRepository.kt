package com.aware.app.data

import androidx.room.withTransaction
import com.aware.app.security.SecureStore
import com.aware.app.sms.ParsedSms
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.ZonedDateTime
import com.aware.app.backup.BackupPayload
import com.aware.app.backup.BackupCodec

class AwareRepository(
    private val database: AppDatabase,
    private val secureStore: SecureStore,
) {
    val accounts = database.accountDao().observeAll()
    val categories = database.categoryDao().observeAll()
    val transactions = database.transactionDao().observeAll()
    val pending = database.captureDao().observePending()
    val recurring = database.recurringDao().observeActive()

    fun budgets(month: YearMonth = YearMonth.now()): Flow<List<BudgetBucketEntity>> =
        database.budgetDao().observeActive(month.toString(), System.currentTimeMillis())

    fun dashboard(month: YearMonth = YearMonth.now()): Flow<DashboardSummary> {
        val zone = ZoneId.systemDefault()
        val from = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val to = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return combine(database.transactionDao().observeRange(from, to), pending) { items, candidates ->
            val income = items.filter { it.type == TransactionType.INCOME }.sumOf { it.amountPaise }
            val refunds = items.filter { it.type == TransactionType.REFUND }.sumOf { it.amountPaise }
            val spending = items.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amountPaise }
            DashboardSummary(
                incomePaise = income,
                otherIncomePaise = refunds,
                spendingPaise = spending,
                savingsPaise = income + refunds - spending,
                pendingCount = candidates.size,
            )
        }
    }

    suspend fun ensureStarterStructure() = database.withTransaction {
        if (database.accountDao().defaultAccount() == null) {
            database.accountDao().insert(AccountEntity(name = "Main account", kind = AccountKind.BANK, isDefault = true))
            database.accountDao().insert(AccountEntity(name = "Cash wallet", kind = AccountKind.CASH))
        }
        val defaults = listOf(
            // aware's balanced ledger palette: distinct at a glance in both appearances.
            CategoryEntity(name = "Food delivery", emoji = "🛵", colorArgb = 0xFFD97762L),
            CategoryEntity(name = "Groceries", emoji = "🛒", colorArgb = 0xFF7B9E87L),
            CategoryEntity(name = "Dining", emoji = "🍜", colorArgb = 0xFFD9A441L),
            CategoryEntity(name = "Travel", emoji = "🚇", colorArgb = 0xFF6E8FB3L),
            CategoryEntity(name = "Family", emoji = "💜", colorArgb = 0xFFA67C91L),
            CategoryEntity(name = "Shopping", emoji = "🛍️", colorArgb = 0xFFC97B84L),
            CategoryEntity(name = "Subscriptions", emoji = "🔁", colorArgb = 0xFF7C74A8L),
            CategoryEntity(name = "Health", emoji = "💊", colorArgb = 0xFF82A39AL),
            CategoryEntity(name = "Education", emoji = "🎓", colorArgb = 0xFF8796C7L),
            CategoryEntity(name = "Flexible", emoji = "✨", colorArgb = 0xFFC8A96BL),
            CategoryEntity(name = "Salary", emoji = "💸", colorArgb = 0xFF3E8E6AL, isIncome = true),
            CategoryEntity(name = "Other income", emoji = "➕", colorArgb = 0xFF5D94B8L, isIncome = true),
        )
        database.categoryDao().insertAll(defaults)
    }

    suspend fun saveCapture(parsed: ParsedSms, source: TransactionSource = TransactionSource.SMS): Long {
        val candidate = CaptureCandidateEntity(
            sender = parsed.sender,
            encryptedBody = secureStore.encrypt(parsed.rawBody),
            amountPaise = parsed.amountPaise,
            type = parsed.type,
            merchant = parsed.merchant,
            accountSuffix = parsed.accountSuffix,
            reference = parsed.reference,
            confidence = parsed.confidence,
            fingerprint = parsed.fingerprint,
            receivedAt = parsed.receivedAt,
            source = source,
        )
        return database.captureDao().insert(candidate)
    }

    suspend fun postCandidate(
        id: Long,
        amountPaise: Long? = null,
        merchant: String? = null,
        type: TransactionType? = null,
        categoryId: Long? = null,
        accountId: Long? = null,
        destinationAccountId: Long? = null,
        learnRule: Boolean = false,
    ): Long = database.withTransaction {
        val candidate = database.captureDao().byId(id) ?: return@withTransaction -1L
        if (candidate.status != TransactionStatus.PENDING_REVIEW) return@withTransaction -1L
        val source = database.accountDao().defaultAccount() ?: return@withTransaction -1L
        val finalType = type ?: candidate.type
        val cash = if (finalType == TransactionType.TRANSFER) database.accountDao().cashAccount() else null
        val merchantRule = database.merchantRuleDao().find(candidate.merchant.lowercase())
        val finalMerchant = merchant?.trim()?.takeIf(String::isNotEmpty) ?: merchantRule?.displayMerchant ?: candidate.merchant
        val finalCategory = if (finalType == TransactionType.TRANSFER) null else categoryId ?: merchantRule?.categoryId?.takeIf { finalType == candidate.type }
        val finalAccount = accountId ?: merchantRule?.accountId ?: source.id
        val finalDestination = destinationAccountId ?: cash?.id
        if (finalType == TransactionType.TRANSFER && (finalDestination == null || finalDestination == finalAccount)) return@withTransaction -1L
        val finalAmount = amountPaise ?: candidate.amountPaise
        val nearExpected = database.transactionDao().expectedNear(
            finalType,
            candidate.receivedAt - 3 * 24 * 3600_000L,
            candidate.receivedAt + 3 * 24 * 3600_000L,
        ).minByOrNull { kotlin.math.abs(it.amountPaise - finalAmount) }?.takeIf {
            kotlin.math.abs(it.amountPaise - finalAmount) <= maxOf(100L, (it.amountPaise * 0.02).toLong())
        }
        val newTransaction = TransactionEntity(
                amountPaise = finalAmount,
                type = finalType,
                source = candidate.source,
                accountId = finalAccount,
                destinationAccountId = finalDestination.takeIf { finalType == TransactionType.TRANSFER },
                categoryId = finalCategory,
                merchant = finalMerchant,
                occurredAt = candidate.receivedAt,
                sourceFingerprint = candidate.fingerprint,
            )
        val transactionId = if (nearExpected != null) {
            database.transactionDao().update(newTransaction.copy(id = nearExpected.id, createdAt = nearExpected.createdAt))
            nearExpected.id
        } else database.transactionDao().insert(newTransaction)
        if (transactionId > 0) {
            database.captureDao().update(candidate.copy(status = TransactionStatus.POSTED, encryptedBody = ""))
            if (learnRule && finalCategory != null) {
                database.merchantRuleDao().upsert(
                    MerchantRuleEntity(
                        normalizedMerchant = candidate.merchant.lowercase(),
                        displayMerchant = finalMerchant,
                        categoryId = finalCategory,
                        accountId = finalAccount,
                    ),
                )
            }
        }
        transactionId
    }

    suspend fun dismissCandidate(id: Long) {
        database.captureDao().byId(id)?.let {
            database.captureDao().update(it.copy(status = TransactionStatus.DISMISSED, encryptedBody = ""))
        }
    }

    suspend fun addTransaction(item: TransactionEntity): Long = database.transactionDao().insert(item)
    suspend fun importTransactions(items: List<TransactionEntity>): ImportResult = database.withTransaction {
        var added = 0
        var duplicates = 0
        items.forEach { item ->
            if (database.transactionDao().insert(item) > 0) added++ else duplicates++
        }
        ImportResult(added, duplicates)
    }
    suspend fun updateTransaction(item: TransactionEntity) = database.transactionDao().update(item)
    suspend fun deleteTransaction(id: Long) {
        database.transactionDao().byId(id)?.let { database.transactionDao().delete(it) }
    }
    suspend fun addAccount(item: AccountEntity) = database.accountDao().insert(item)
    suspend fun saveAccount(item: AccountEntity): Long = database.withTransaction {
        if (item.isDefault) database.accountDao().clearDefault()
        val id = if (item.id == 0L) {
            database.accountDao().insert(item)
        } else {
            database.accountDao().update(item)
            item.id
        }
        if (database.accountDao().defaultAccount() == null) {
            database.accountDao().byId(id)?.let { database.accountDao().update(it.copy(isDefault = true)) }
        }
        id
    }
    suspend fun addCategory(item: CategoryEntity): Long = saveCategory(item)

    suspend fun saveCategory(item: CategoryEntity): Long {
        val duplicate = database.categoryDao().byName(item.name)
        require(duplicate == null || duplicate.id == item.id) { "A category named “${item.name}” already exists" }
        return if (item.id == 0L) {
            database.categoryDao().insert(item)
        } else {
            database.categoryDao().update(item)
            item.id
        }
    }
    suspend fun addBudget(item: BudgetBucketEntity) = database.budgetDao().upsert(item)
    suspend fun addRecurring(item: RecurringRuleEntity) = database.recurringDao().insert(item)
    suspend fun latestPending() = database.captureDao().latestPending()
    suspend fun candidate(id: Long) = database.captureDao().byId(id)
    fun decryptCandidateBody(candidate: CaptureCandidateEntity) = secureStore.decrypt(candidate.encryptedBody)
    suspend fun pendingCount() = database.captureDao().pendingCount()
    suspend fun latestWeeklyReport() = database.weeklyReportDao().latest()

    suspend fun generateLatestWeeklyReportIfMissing(now: Long = System.currentTimeMillis()): WeeklyReportEntity {
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val currentWeekStart = today.minusDays((today.dayOfWeek.value - 1).toLong())
        val weekStart = currentWeekStart.minusWeeks(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val weekEnd = currentWeekStart.atStartOfDay(zone).toInstant().toEpochMilli()
        database.weeklyReportDao().latest()?.takeIf { it.weekStart == weekStart }?.let { return it }

        val transactions = database.transactionDao().rangeOnce(weekStart, weekEnd)
        val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
        val report = WeeklyReportEntity(
            weekStart = weekStart,
            weekEndExclusive = weekEnd,
            generatedAt = now,
            incomePaise = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amountPaise },
            refundPaise = transactions.filter { it.type == TransactionType.REFUND }.sumOf { it.amountPaise },
            spendingPaise = expenses.sumOf { it.amountPaise },
            transferPaise = transactions.filter { it.type == TransactionType.TRANSFER }.sumOf { it.amountPaise },
            topMerchant = expenses.groupBy { it.merchant.trim() }.maxByOrNull { (_, items) -> items.sumOf { it.amountPaise } }?.key?.takeIf(String::isNotBlank),
        )
        database.weeklyReportDao().upsert(report)
        return report
    }
    suspend fun cleanupExpiredRawBodies() = database.captureDao().deleteExpired(Instant.now().minusSeconds(7 * 24 * 3600).toEpochMilli())

    suspend fun materializeDueRecurring(now: Long = System.currentTimeMillis()) = database.withTransaction {
        database.recurringDao().due(now).forEach { rule ->
            database.transactionDao().insert(
                TransactionEntity(
                    amountPaise = rule.amountPaise,
                    type = rule.type,
                    status = TransactionStatus.EXPECTED,
                    source = TransactionSource.RECURRING,
                    accountId = rule.accountId,
                    categoryId = rule.categoryId,
                    merchant = rule.name,
                    note = "Expected ${rule.cadence.name.lowercase()} cash flow",
                    occurredAt = rule.nextExpectedAt,
                    sourceFingerprint = "recurring:${rule.id}:${rule.nextExpectedAt}",
                ),
            )
            val next = Instant.ofEpochMilli(rule.nextExpectedAt).atZone(ZoneId.systemDefault()).let {
                when (rule.cadence) {
                    RecurrenceCadence.DAILY -> it.plusDays(1)
                    RecurrenceCadence.WEEKLY -> it.plusWeeks(1)
                    RecurrenceCadence.MONTHLY -> it.plusMonths(1)
                    RecurrenceCadence.YEARLY -> it.plusYears(1)
                    RecurrenceCadence.CUSTOM -> it.plusDays(rule.customIntervalDays.coerceAtLeast(1).toLong())
                }
            }.toInstant().toEpochMilli()
            database.recurringDao().update(rule.copy(nextExpectedAt = next, isActive = rule.endAt == null || next <= rule.endAt))
        }
    }

    fun appLockEnabled() = secureStore.getBoolean("app_lock")
    fun setAppLock(enabled: Boolean) = secureStore.putBoolean("app_lock", enabled)
    fun smartNudgesEnabled() = secureStore.getBoolean("smart_nudges", true)
    fun setSmartNudges(enabled: Boolean) = secureStore.putBoolean("smart_nudges", enabled)

    suspend fun createEncryptedBackup(password: CharArray): ByteArray = BackupCodec.encrypt(
        BackupPayload(
            accounts = database.accountDao().allOnce(),
            categories = database.categoryDao().allOnce(),
            transactions = database.transactionDao().allOnce(),
            budgets = database.budgetDao().allOnce(),
            recurring = database.recurringDao().allOnce(),
            merchantRules = database.merchantRuleDao().allOnce(),
        ),
        password,
    )

    suspend fun createCsv(): String = BackupCodec.csv(
        database.transactionDao().allOnce(),
        database.accountDao().allOnce(),
        database.categoryDao().allOnce(),
    )

    suspend fun restoreEncryptedBackup(bytes: ByteArray, password: CharArray) {
        val payload = BackupCodec.decrypt(bytes, password)
        require(payload.version == 1) { "Unsupported backup version" }
        database.withTransaction {
            database.merchantRuleDao().clear()
            database.budgetDao().clear()
            database.recurringDao().clear()
            database.transactionDao().clear()
            database.categoryDao().clear()
            database.accountDao().clear()
            database.accountDao().restoreAll(payload.accounts)
            database.categoryDao().restoreAll(payload.categories)
            database.transactionDao().restoreAll(payload.transactions)
            database.budgetDao().restoreAll(payload.budgets)
            database.recurringDao().restoreAll(payload.recurring)
            database.merchantRuleDao().restoreAll(payload.merchantRules)
        }
    }

    data class ImportResult(val added: Int, val duplicates: Int)

    data class BudgetAlert(val budgetId: Long, val monthKey: String, val name: String, val percent: Int, val spentPaise: Long, val capPaise: Long)

    suspend fun newBudgetAlerts(month: YearMonth = YearMonth.now()): List<BudgetAlert> {
        if (!smartNudgesEnabled()) return emptyList()
        val zone = ZoneId.systemDefault()
        val now = java.time.LocalDate.now(zone)
        return database.budgetDao().activeOnce(month.toString(), System.currentTimeMillis()).mapNotNull { budget ->
            if (budget.capPaise <= 0) return@mapNotNull null
            val startDate = when (budget.period) {
                BudgetPeriod.DAILY -> now
                BudgetPeriod.WEEKLY -> now.minusDays((now.dayOfWeek.value - 1).toLong())
                BudgetPeriod.MONTHLY -> now.withDayOfMonth(1)
                BudgetPeriod.YEARLY -> now.withDayOfYear(1)
            }
            val endDate = when (budget.period) {
                BudgetPeriod.DAILY -> startDate.plusDays(1)
                BudgetPeriod.WEEKLY -> startDate.plusWeeks(1)
                BudgetPeriod.MONTHLY -> startDate.plusMonths(1)
                BudgetPeriod.YEARLY -> startDate.plusYears(1)
            }
            val from = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val to = endDate.atStartOfDay(zone).toInstant().toEpochMilli()
            val spent = database.transactionDao().expenseTotalScoped(
                from,
                to,
                budget.categoryId.takeIf { budget.scope == BudgetScope.CATEGORY },
                budget.accountId.takeIf { budget.scope == BudgetScope.ACCOUNT },
                budget.payee.takeIf { budget.scope == BudgetScope.PAYEE },
            )
            val ratio = spent.toDouble() / budget.capPaise
            val threshold = when {
                ratio >= 1.0 && budget.alert100 -> 100
                ratio >= .9 && budget.alert90 -> 90
                ratio >= .75 && budget.alert75 -> 75
                ratio >= .5 && budget.alert50 -> 50
                else -> return@mapNotNull null
            }
            val key = "budget_alert_${startDate}_${budget.id}_$threshold"
            if (secureStore.getBoolean(key)) null else {
                secureStore.putBoolean(key, true)
                BudgetAlert(budget.id, month.toString(), budget.name, threshold, spent, budget.capPaise)
            }
        }
    }
}

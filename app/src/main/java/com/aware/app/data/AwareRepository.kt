package com.aware.app.data

import androidx.room.withTransaction
import com.aware.app.security.SecureStore
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
    val recurring = database.recurringDao().observeActive()
    val savingsGoals = database.savingsGoalDao().observeActive()

    fun monthlyPlan(month: YearMonth = YearMonth.now()): Flow<MonthlyPlanEntity?> =
        database.monthlyPlanDao().observe(month.toString())

    fun budgets(month: YearMonth = YearMonth.now()): Flow<List<BudgetBucketEntity>> =
        database.budgetDao().observeActive(month.toString(), System.currentTimeMillis())

    fun dashboard(month: YearMonth = YearMonth.now()): Flow<DashboardSummary> {
        val zone = ZoneId.systemDefault()
        val from = month.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val to = month.plusMonths(1).atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return combine(
            database.transactionDao().observeRange(from, to), categories, monthlyPlan(month), accounts, transactions,
        ) { values ->
            @Suppress("UNCHECKED_CAST")
            val items = values[0] as List<TransactionEntity>
            val currentCategories = values[1] as List<CategoryEntity>
            val plan = values[2] as MonthlyPlanEntity?
            val currentAccounts = values[3] as List<AccountEntity>
            val allTransactions = values[4] as List<TransactionEntity>
            val today = java.time.LocalDate.now(zone)
            MonthlyFinanceCalculator.calculate(
                monthTransactions = items,
                allTransactions = allTransactions,
                categories = currentCategories,
                accounts = currentAccounts,
                plan = plan,
                daysRemaining = month.lengthOfMonth() - today.dayOfMonth + 1,
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
            CategoryEntity(name = "Food delivery", emoji = "🛵", colorArgb = 0xFFD97762L, expenseNature = ExpenseNature.DISCRETIONARY),
            CategoryEntity(name = "Groceries", emoji = "🛒", colorArgb = 0xFF7B9E87L, expenseNature = ExpenseNature.ESSENTIAL),
            CategoryEntity(name = "Dining", emoji = "🍜", colorArgb = 0xFFD9A441L, expenseNature = ExpenseNature.DISCRETIONARY),
            CategoryEntity(name = "Travel", emoji = "🚇", colorArgb = 0xFF6E8FB3L, expenseNature = ExpenseNature.ESSENTIAL),
            CategoryEntity(name = "Family", emoji = "💜", colorArgb = 0xFFA67C91L, expenseNature = ExpenseNature.COMMITMENT),
            CategoryEntity(name = "Shopping", emoji = "🛍️", colorArgb = 0xFFC97B84L),
            CategoryEntity(name = "Subscriptions", emoji = "🔁", colorArgb = 0xFF7C74A8L, expenseNature = ExpenseNature.COMMITMENT),
            CategoryEntity(name = "Health", emoji = "💊", colorArgb = 0xFF82A39AL, expenseNature = ExpenseNature.ESSENTIAL),
            CategoryEntity(name = "Education", emoji = "🎓", colorArgb = 0xFF8796C7L, expenseNature = ExpenseNature.ONE_TIME),
            CategoryEntity(name = "Flexible", emoji = "✨", colorArgb = 0xFFC8A96BL),
            CategoryEntity(name = "Salary", emoji = "💸", colorArgb = 0xFF3E8E6AL, isIncome = true),
            CategoryEntity(name = "Other income", emoji = "➕", colorArgb = 0xFF5D94B8L, isIncome = true),
        )
        database.categoryDao().insertAll(defaults)
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
        val duplicate = database.categoryDao().byName(item.name, item.isIncome)
        require(duplicate == null || duplicate.id == item.id) { "A category named “${item.name}” already exists" }
        return if (item.id == 0L) {
            database.categoryDao().insert(item)
        } else {
            database.categoryDao().update(item)
            item.id
        }
    }
    suspend fun addBudget(item: BudgetBucketEntity) = database.budgetDao().upsert(item)
    suspend fun deleteBudget(item: BudgetBucketEntity) = database.budgetDao().delete(item)
    suspend fun addRecurring(item: RecurringRuleEntity) = database.recurringDao().insert(item)
    suspend fun saveRecurring(item: RecurringRuleEntity) = if (item.id == 0L) database.recurringDao().insert(item) else database.recurringDao().update(item)
    suspend fun deleteRecurring(item: RecurringRuleEntity) = database.recurringDao().delete(item)
    suspend fun saveMonthlyPlan(item: MonthlyPlanEntity) = database.monthlyPlanDao().upsert(item)
    suspend fun saveSavingsGoal(item: SavingsGoalEntity) = database.savingsGoalDao().upsert(item)
    suspend fun deleteSavingsGoal(item: SavingsGoalEntity) = database.savingsGoalDao().delete(item)

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
            monthlyPlans = database.monthlyPlanDao().allOnce(),
            savingsGoals = database.savingsGoalDao().allOnce(),
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
            database.monthlyPlanDao().clear()
            database.savingsGoalDao().clear()
            database.accountDao().restoreAll(payload.accounts)
            database.categoryDao().restoreAll(payload.categories)
            database.transactionDao().restoreAll(payload.transactions)
            database.budgetDao().restoreAll(payload.budgets)
            database.recurringDao().restoreAll(payload.recurring)
            database.merchantRuleDao().restoreAll(payload.merchantRules)
            database.monthlyPlanDao().restoreAll(payload.monthlyPlans)
            database.savingsGoalDao().restoreAll(payload.savingsGoals)
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
            val range = database.transactionDao().rangeOnce(from, to)
            val expenses = range.filter { transaction ->
                transaction.type == TransactionType.EXPENSE && when (budget.scope) {
                    BudgetScope.OVERALL -> true
                    BudgetScope.CATEGORY -> budget.categoryId == null || transaction.categoryId == budget.categoryId
                    BudgetScope.ACCOUNT -> budget.accountId == null || transaction.accountId == budget.accountId
                    BudgetScope.PAYEE -> budget.payee.isNullOrBlank() || transaction.merchant.equals(budget.payee, ignoreCase = true)
                }
            }
            val expenseIds = expenses.mapTo(mutableSetOf()) { it.id }
            val refunds = range.filter { it.type == TransactionType.REFUND && it.linkedTransactionId in expenseIds }.sumOf { it.amountPaise }
            val spent = (expenses.sumOf { it.amountPaise } - refunds).coerceAtLeast(0)
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

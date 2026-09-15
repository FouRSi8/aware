package com.aware.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Query("SELECT * FROM accounts ORDER BY isDefault DESC, createdAt") fun observeAll(): Flow<List<AccountEntity>>
    @Query("SELECT * FROM accounts WHERE isDefault = 1 LIMIT 1") suspend fun defaultAccount(): AccountEntity?
    @Query("SELECT * FROM accounts WHERE kind = 'CASH' LIMIT 1") suspend fun cashAccount(): AccountEntity?
    @Query("SELECT * FROM accounts ORDER BY id") suspend fun allOnce(): List<AccountEntity>
    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1") suspend fun byId(id: Long): AccountEntity?
    @Query("UPDATE accounts SET isDefault = 0") suspend fun clearDefault()
    @Insert suspend fun insert(account: AccountEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<AccountEntity>)
    @Query("DELETE FROM accounts") suspend fun clear()
    @Update suspend fun update(account: AccountEntity)
}

@Dao
abstract class CategoryDao {
    @Query(
        """SELECT id, name, emoji, colorArgb, 0 AS isIncome, isArchived, expenseNature FROM expense_categories WHERE isArchived = 0
           UNION ALL
           SELECT id, name, emoji, colorArgb, 1 AS isIncome, isArchived, 'DISCRETIONARY' AS expenseNature FROM income_categories WHERE isArchived = 0
           ORDER BY isIncome, name""",
    )
    abstract fun observeAll(): Flow<List<CategoryEntity>>

    @Query("SELECT id, name, emoji, colorArgb, 0 AS isIncome, isArchived, expenseNature FROM expense_categories WHERE lower(name) = lower(:name) LIMIT 1")
    protected abstract suspend fun expenseByName(name: String): CategoryEntity?

    @Query("SELECT id, name, emoji, colorArgb, 1 AS isIncome, isArchived, 'DISCRETIONARY' AS expenseNature FROM income_categories WHERE lower(name) = lower(:name) LIMIT 1")
    protected abstract suspend fun incomeByName(name: String): CategoryEntity?

    suspend fun byName(name: String, isIncome: Boolean): CategoryEntity? =
        if (isIncome) incomeByName(name) else expenseByName(name)

    @Query(
        """SELECT id, name, emoji, colorArgb, 0 AS isIncome, isArchived, expenseNature FROM expense_categories
           UNION ALL
           SELECT id, name, emoji, colorArgb, 1 AS isIncome, isArchived, 'DISCRETIONARY' AS expenseNature FROM income_categories
           ORDER BY id""",
    )
    abstract suspend fun allOnce(): List<CategoryEntity>

    @Query("SELECT MAX(id) FROM (SELECT id FROM expense_categories UNION ALL SELECT id FROM income_categories)")
    protected abstract suspend fun highestId(): Long?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertExpense(item: ExpenseCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIncome(item: IncomeCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun restoreExpense(item: ExpenseCategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun restoreIncome(item: IncomeCategoryEntity): Long

    @Query("UPDATE expense_categories SET name = :name, emoji = :emoji, colorArgb = :colorArgb, isArchived = :isArchived, expenseNature = :expenseNature WHERE id = :id")
    protected abstract suspend fun updateExpense(id: Long, name: String, emoji: String, colorArgb: Long, isArchived: Boolean, expenseNature: ExpenseNature): Int

    @Query("UPDATE income_categories SET name = :name, emoji = :emoji, colorArgb = :colorArgb, isArchived = :isArchived WHERE id = :id")
    protected abstract suspend fun updateIncome(id: Long, name: String, emoji: String, colorArgb: Long, isArchived: Boolean): Int

    @Query("DELETE FROM expense_categories")
    protected abstract suspend fun clearExpenses()

    @Query("DELETE FROM income_categories")
    protected abstract suspend fun clearIncome()

    @Transaction
    open suspend fun insert(item: CategoryEntity): Long {
        val id = item.id.takeIf { it > 0 } ?: ((highestId() ?: 0L) + 1L)
        val inserted = if (item.isIncome) {
            insertIncome(IncomeCategoryEntity(id, item.name, item.emoji, item.colorArgb, item.isArchived))
        } else {
            insertExpense(ExpenseCategoryEntity(id, item.name, item.emoji, item.colorArgb, item.isArchived, item.expenseNature))
        }
        return if (inserted == -1L) -1L else id
    }

    @Transaction
    open suspend fun insertAll(items: List<CategoryEntity>) {
        items.forEach { insert(it) }
    }

    @Transaction
    open suspend fun update(item: CategoryEntity) {
        val changed = if (item.isIncome) {
            updateIncome(item.id, item.name, item.emoji, item.colorArgb, item.isArchived)
        } else {
            updateExpense(item.id, item.name, item.emoji, item.colorArgb, item.isArchived, item.expenseNature)
        }
        require(changed == 1) { "Category no longer exists" }
    }

    @Transaction
    open suspend fun restoreAll(items: List<CategoryEntity>) {
        items.forEach { item ->
            if (item.isIncome) {
                restoreIncome(IncomeCategoryEntity(item.id, item.name, item.emoji, item.colorArgb, item.isArchived))
            } else {
                restoreExpense(ExpenseCategoryEntity(item.id, item.name, item.emoji, item.colorArgb, item.isArchived, item.expenseNature))
            }
        }
    }

    @Transaction
    open suspend fun clear() {
        clearExpenses()
        clearIncome()
    }
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY occurredAt DESC") fun observeAll(): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions WHERE occurredAt >= :from AND occurredAt < :to AND status = 'POSTED' ORDER BY occurredAt DESC") fun observeRange(from: Long, to: Long): Flow<List<TransactionEntity>>
    @Query("SELECT * FROM transactions WHERE occurredAt >= :from AND occurredAt < :to AND status = 'POSTED' ORDER BY occurredAt DESC") suspend fun rangeOnce(from: Long, to: Long): List<TransactionEntity>
    @Query("SELECT * FROM transactions WHERE id = :id") suspend fun byId(id: Long): TransactionEntity?
    @Query("SELECT * FROM transactions ORDER BY id") suspend fun allOnce(): List<TransactionEntity>
    @Query("SELECT * FROM transactions WHERE status = 'EXPECTED' AND type = :type AND occurredAt BETWEEN :from AND :to") suspend fun expectedNear(type: TransactionType, from: Long, to: Long): List<TransactionEntity>
    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM transactions WHERE status = 'POSTED' AND type = 'EXPENSE' AND occurredAt >= :from AND occurredAt < :to AND (:categoryId IS NULL OR categoryId = :categoryId)") suspend fun expenseTotal(from: Long, to: Long, categoryId: Long?): Long
    @Query("SELECT COALESCE(SUM(amountPaise), 0) FROM transactions WHERE status = 'POSTED' AND type = 'EXPENSE' AND occurredAt >= :from AND occurredAt < :to AND (:categoryId IS NULL OR categoryId = :categoryId) AND (:accountId IS NULL OR accountId = :accountId) AND (:payee IS NULL OR lower(merchant) = lower(:payee))") suspend fun expenseTotalScoped(from: Long, to: Long, categoryId: Long?, accountId: Long?, payee: String?): Long
    @Insert(onConflict = OnConflictStrategy.IGNORE) suspend fun insert(item: TransactionEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<TransactionEntity>)
    @Query("DELETE FROM transactions") suspend fun clear()
    @Update suspend fun update(item: TransactionEntity)
    @Delete suspend fun delete(item: TransactionEntity)
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_buckets WHERE monthKey = :monthKey ORDER BY name") fun observeMonth(monthKey: String): Flow<List<BudgetBucketEntity>>
    @Query("SELECT * FROM budget_buckets WHERE (isRecurring = 1 OR monthKey = :monthKey) AND (startAt = 0 OR startAt <= :now) AND (endAt IS NULL OR endAt >= :now) ORDER BY name") fun observeActive(monthKey: String, now: Long): Flow<List<BudgetBucketEntity>>
    @Query("SELECT * FROM budget_buckets ORDER BY id") suspend fun allOnce(): List<BudgetBucketEntity>
    @Query("SELECT * FROM budget_buckets WHERE monthKey = :monthKey ORDER BY id") suspend fun monthOnce(monthKey: String): List<BudgetBucketEntity>
    @Query("SELECT * FROM budget_buckets WHERE (isRecurring = 1 OR monthKey = :monthKey) AND (startAt = 0 OR startAt <= :now) AND (endAt IS NULL OR endAt >= :now) ORDER BY id") suspend fun activeOnce(monthKey: String, now: Long): List<BudgetBucketEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: BudgetBucketEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<BudgetBucketEntity>)
    @Query("DELETE FROM budget_buckets") suspend fun clear()
    @Delete suspend fun delete(item: BudgetBucketEntity)
}

@Dao
interface RecurringDao {
    @Query("SELECT * FROM recurring_rules WHERE isActive = 1 ORDER BY nextExpectedAt") fun observeActive(): Flow<List<RecurringRuleEntity>>
    @Query("SELECT * FROM recurring_rules WHERE isActive = 1 AND nextExpectedAt <= :now ORDER BY nextExpectedAt") suspend fun due(now: Long): List<RecurringRuleEntity>
    @Query("SELECT * FROM recurring_rules ORDER BY id") suspend fun allOnce(): List<RecurringRuleEntity>
    @Insert suspend fun insert(item: RecurringRuleEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<RecurringRuleEntity>)
    @Query("DELETE FROM recurring_rules") suspend fun clear()
    @Update suspend fun update(item: RecurringRuleEntity)
    @Delete suspend fun delete(item: RecurringRuleEntity)
}

@Dao
interface MerchantRuleDao {
    @Query("SELECT * FROM merchant_rules ORDER BY updatedAt DESC") fun observeAll(): Flow<List<MerchantRuleEntity>>
    @Query("SELECT * FROM merchant_rules ORDER BY id") suspend fun allOnce(): List<MerchantRuleEntity>
    @Query("SELECT * FROM merchant_rules WHERE normalizedMerchant = :merchant LIMIT 1") suspend fun find(merchant: String): MerchantRuleEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(rule: MerchantRuleEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<MerchantRuleEntity>)
    @Query("DELETE FROM merchant_rules") suspend fun clear()
}

@Dao
interface MonthlyPlanDao {
    @Query("SELECT * FROM monthly_plans WHERE monthKey = :monthKey LIMIT 1") fun observe(monthKey: String): Flow<MonthlyPlanEntity?>
    @Query("SELECT * FROM monthly_plans ORDER BY monthKey") suspend fun allOnce(): List<MonthlyPlanEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: MonthlyPlanEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<MonthlyPlanEntity>)
    @Query("DELETE FROM monthly_plans") suspend fun clear()
}

@Dao
interface SavingsGoalDao {
    @Query("SELECT * FROM savings_goals WHERE isArchived = 0 ORDER BY createdAt") fun observeActive(): Flow<List<SavingsGoalEntity>>
    @Query("SELECT * FROM savings_goals ORDER BY id") suspend fun allOnce(): List<SavingsGoalEntity>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(item: SavingsGoalEntity): Long
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun restoreAll(items: List<SavingsGoalEntity>)
    @Query("DELETE FROM savings_goals") suspend fun clear()
    @Delete suspend fun delete(item: SavingsGoalEntity)
}
